/*
 * Copyright 2020-2021 the original author or authors.
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
import hu.perit.spvitamin.spring.config.CryptoProperties;
import hu.perit.spvitamin.spring.config.JwtProperties;
import hu.perit.spvitamin.spring.config.MetricsProperties;
import hu.perit.spvitamin.spring.config.MicroserviceCollectionProperties;
import hu.perit.spvitamin.spring.config.MicroserviceProperties;
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.ServerProperties;
import hu.perit.spvitamin.spring.config.SwaggerProperties;
import hu.perit.spvitamin.spring.config.SystemProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import springfox.boot.starter.autoconfigure.SpringfoxConfigurationProperties;

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
    private final SpringfoxConfigurationProperties springfoxConfigurationProperties;

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

        /*
        Environment env = SpringEnvironment.getInstance().get();

        List<ServerParameter> parameter = params.getParameter();
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::<String>stream)
                .forEach(propName -> parameter.add(new ServerParameter("Environment." + propName, env.getProperty(propName), false)));
        */

        return params;
    }

    private String getActuatorUrl()
    {
        return this.serverProperties.getServiceUrl() + "/actuator";
    }

    private String getApiDocsUrl()
    {
        return this.serverProperties.getServiceUrl() + this.swaggerProperties.getSwagger().getV2().getPath();
    }

    private String getSwaggerUrl()
    {
        String baseUrl = swaggerProperties.getSwaggerUi().getBaseUrl();
        return this.serverProperties.getServiceUrl() + baseUrl + "/swagger-ui/index.html";
    }
}
