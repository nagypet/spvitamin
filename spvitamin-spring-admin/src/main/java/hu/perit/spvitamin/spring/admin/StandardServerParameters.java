/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.admin;

import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameter;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterList;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterListBuilder;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterListImpl;
import hu.perit.spvitamin.spring.config.AdminProperties;
import hu.perit.spvitamin.spring.config.CryptoProperties;
import hu.perit.spvitamin.spring.config.JwtProperties;
import hu.perit.spvitamin.spring.config.LocalUserProperties;
import hu.perit.spvitamin.spring.config.MetricsProperties;
import hu.perit.spvitamin.spring.config.MicroserviceCollectionProperties;
import hu.perit.spvitamin.spring.config.MicroserviceProperties;
import hu.perit.spvitamin.spring.config.Role2PermissionMappingProperties;
import hu.perit.spvitamin.spring.config.RoleMappingProperties;
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.ServerProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.config.SwaggerProperties;
import hu.perit.spvitamin.spring.config.SystemProperties;
import hu.perit.spvitamin.spring.environment.SpringEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Peter Nagy
 */


@Component
@RequiredArgsConstructor
@Slf4j
class StandardServerParameters
{
    public static final String LINKS = "1-Links";

    private final CryptoProperties cryptoProperties;
    private final JwtProperties jwtProperties;
    private final MetricsProperties metricsProperties;
    private final SystemProperties systemProperties;
    private final SecurityProperties securityProperties;
    private final ServerProperties serverProperties;
    private final JacksonProperties jacksonProperties;
    private final MicroserviceCollectionProperties microserviceCollectionProperties;
    private final SwaggerProperties swaggerProperties;
    private final AdminProperties adminProperties;
    private final LocalUserProperties localUserProperties;
    private final RoleMappingProperties roleMappingProperties;
    private final Role2PermissionMappingProperties role2PermissionMappingProperties;

    @Bean(name = "StandardServerParameters")
    public ServerParameterList getParameterList()
    {
        ServerParameterList params = new ServerParameterListImpl();

        params.add(LINKS, new ServerParameter("(1) Swagger UI", this.getSwaggerUrl(), true));
        params.add(LINKS, new ServerParameter("(2) Swagger API Docs", this.getApiDocsUrl(), true));
        params.add(LINKS, new ServerParameter("(3) Actuator", this.getActuatorUrl(), true));
        H2ConsoleProperties h2ConsoleProperties = getH2Properties();
        if (h2ConsoleProperties != null && h2ConsoleProperties.getEnabled())
        {
            params.add(LINKS, new ServerParameter("H2 console", this.getH2ConsoleUrl(h2ConsoleProperties), true));
        }

        params.add(ServerParameterListBuilder.of(this.cryptoProperties));
        params.add(ServerParameterListBuilder.of(this.jwtProperties));
        params.add(ServerParameterListBuilder.of(this.metricsProperties));
        params.add(ServerParameterListBuilder.of(this.systemProperties));
        params.add(ServerParameterListBuilder.of(this.securityProperties));
        params.add(ServerParameterListBuilder.of(this.serverProperties));
        params.add(ServerParameterListBuilder.of(this.jacksonProperties));
        params.add(ServerParameterListBuilder.of(this.adminProperties));
        params.add(ServerParameterListBuilder.of(this.role2PermissionMappingProperties));

        for (Map.Entry<String, LocalUserProperties.User> entry : this.localUserProperties.getLocaluser().entrySet())
        {
            params.add("Local users", new ServerParameter(entry.getKey(), "", false));
        }

        for (Map.Entry<String, RoleMappingProperties.RoleMapping> entry : this.roleMappingProperties.getRoles().entrySet())
        {
            params.add(getRoleMappingGroupName(entry.getKey()), ServerParameterListBuilder.of(entry.getValue()));
        }

        if (h2ConsoleProperties != null && h2ConsoleProperties.getEnabled())
        {
            params.add(ServerParameterListBuilder.of(h2ConsoleProperties));
        }

        for (Map.Entry<String, MicroserviceProperties> entry : this.microserviceCollectionProperties.getMicroservices().entrySet())
        {
            params.add(ServerParameterListBuilder.of(entry.getValue(), "microservices." + entry.getKey()));
        }

        try
        {
            Environment env = SpringEnvironment.get();

            MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
            for (PropertySource<?> propertySource : propSrcs.stream().filter(OriginTrackedMapPropertySource.class::isInstance).toList())
            {
                Arrays.stream(((EnumerablePropertySource<?>) propertySource).getPropertyNames())
                        .forEach(propName -> {
                            String propValue = env.getProperty(propName);
                            if (isSecret(propName))
                            {
                                propValue = "*** [hidden]";
                            }
                            params.add("2-" + propertySource.getName(), new ServerParameter(propName, propValue, false));
                        });
            }
        }
        catch (RuntimeException e)
        {
            log.warn(e.toString());
        }

        return params;
    }

    private H2ConsoleProperties getH2Properties()
    {
        try
        {
            return SpringContext.getBean(H2ConsoleProperties.class);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }

    private static String getRoleMappingGroupName(String role)
    {
        return MessageFormat.format("{0}: {1}", RoleMappingProperties.RoleMapping.class.getSimpleName(), role);
    }

    private static boolean isSecret(String propName)
    {
        return StringUtils.containsAny(StringUtils.toRootLowerCase(propName), "password", "secret");
    }

    private String getActuatorUrl()
    {
        return this.serverProperties.getServiceUrl() + "/actuator";
    }

    private String getH2ConsoleUrl(H2ConsoleProperties h2ConsoleProperties)
    {
        return this.serverProperties.getServiceUrl() + h2ConsoleProperties.getPath();
    }

    private String getApiDocsUrl()
    {
        return this.serverProperties.getServiceUrl() + this.swaggerProperties.getApiDocs().getPath();
    }

    private String getSwaggerUrl()
    {
        String path = swaggerProperties.getSwaggerUi().getPath();
        return this.serverProperties.getServiceUrl() + path + "/index.html";
    }
}
