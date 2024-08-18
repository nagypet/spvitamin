/*
 * Copyright 2020-2024 the original author or authors.
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
import hu.perit.spvitamin.spring.config.*;
import hu.perit.spvitamin.spring.environment.SpringEnvironment;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Peter Nagy
 */


@Component
@AllArgsConstructor
class StandardServerParameters
{
    private final CryptoProperties cryptoProperties;
    private final JwtProperties jwtProperties;
    private final MetricsProperties metricsProperties;
    private final SystemProperties systemProperties;
    private final SecurityProperties securityProperties;
    private final ServerProperties serverProperties;
    private final JacksonProperties jacksonProperties;
    private final MicroserviceCollectionProperties microserviceCollectionProperties;
    private final SwaggerProperties swaggerProperties;

    @Bean(name = "StandardServerParameters")
    public ServerParameterList getParameterList()
    {
        ServerParameterList params = new ServerParameterList();

        params.add(new ServerParameter("(1) Swagger UI", this.getSwaggerUrl(), true));
        params.add(new ServerParameter("(2) Swagger API Docs", this.getApiDocsUrl(), true));
        params.add(new ServerParameter("(3) Actuator", this.getActuatorUrl(), true));

        params.add(ServerParameterList.of(this.cryptoProperties));
        params.add(ServerParameterList.of(this.jwtProperties));
        params.add(ServerParameterList.of(this.metricsProperties));
        params.add(ServerParameterList.of(this.systemProperties));
        params.add(ServerParameterList.of(this.securityProperties));
        params.add(ServerParameterList.of(this.serverProperties));
        params.add(ServerParameterList.of(this.jacksonProperties));

        for (Map.Entry<String, MicroserviceProperties> entry : this.microserviceCollectionProperties.getMicroservices().entrySet())
        {
            params.add(ServerParameterList.of(entry.getValue(), "microservices." + entry.getKey()));
        }

        Environment env = SpringEnvironment.getInstance().get();

        List<ServerParameter> parameter = params.getParameters();
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        for (PropertySource<?> propertySource : propSrcs.stream().filter(OriginTrackedMapPropertySource.class::isInstance).toList())
        {
            Arrays.stream(((EnumerablePropertySource<?>) propertySource).getPropertyNames())
                    .forEach(propName -> parameter.add(new ServerParameter(propertySource.getName() + "." + propName, env.getProperty(propName), false)));
        }

        return params;
    }

    private String getActuatorUrl()
    {
        return this.serverProperties.getServiceUrl() + "/actuator";
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
