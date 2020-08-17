/*
 * Copyright 2020-2020 the original author or authors.
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

package hu.perit.spvitamin.spring.admin.serverparameter;

import hu.perit.spvitamin.spring.config.*;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Peter Nagy
 */


@Component
@AllArgsConstructor
class StandardServerParameters
{

    private final SystemProperties systemProperties;
    private final JwtProperties jwtProperties;
    private final MetricsProperties metricsProperties;
    private final ServerProperties serverProperties;
    private final SecurityProperties securityProperties;
    private final JacksonProperties jacksonProperties;

    @Bean(name = "StandardServerParameters")
    public ServerParameterList getParameterList()
    {
        ServerParameterList params = new ServerParameterList();
        params.add(new ServerParameter("(1) Swagger UI", this.getSwaggerUrl(), true));
        params.add(new ServerParameter("(2) Swagger API Docs", this.getApiDocsUrl(), true));
        params.add(new ServerParameter("(3) Actuator", this.getActuatorUrl(), true));

        params.add(ServerParameterList.of(this.systemProperties));
        params.add(ServerParameterList.of(this.jwtProperties));
        params.add(ServerParameterList.of(this.metricsProperties));
        params.add(ServerParameterList.of(this.serverProperties));
        params.add(ServerParameterList.of(this.securityProperties));
        params.add(ServerParameterList.of(this.jacksonProperties));

        return params;
    }

    private String getActuatorUrl() {
        return this.serverProperties.getServiceUrl() + "/actuator";
    }

    private String getApiDocsUrl() {
        return this.serverProperties.getServiceUrl() + "/v2/api-docs";
    }

    private String getSwaggerUrl() {
        return this.serverProperties.getServiceUrl() + "/swagger-ui.html";
    }
}
