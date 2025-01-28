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

package hu.perit.spvitamin.spring.eureka.admin;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.EurekaServerConfig;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterList;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterListBuilder;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterListImpl;
import hu.perit.spvitamin.spring.config.SpringContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("SpvitaminSpringContext")
class EurekaServerParameters
{

    @Bean(name = "EurekaServerParameters")
    public ServerParameterList getParameterList()
    {
        ServerParameterList params = new ServerParameterListImpl();

        if (SpringContext.isBeanAvailable(EurekaInstanceConfig.class)) {
            EurekaInstanceConfig eurekaInstanceConfig = SpringContext.getBean(EurekaInstanceConfig.class);
            params.add(ServerParameterListBuilder.of(eurekaInstanceConfig));
        }

        if (SpringContext.isBeanAvailable(EurekaClientConfig.class)) {
            EurekaClientConfig eurekaClientConfig = SpringContext.getBean(EurekaClientConfig.class);
            params.add(ServerParameterListBuilder.of(eurekaClientConfig));
        }

        if (SpringContext.isBeanAvailable(EurekaServerConfig.class)) {
            EurekaServerConfig eurekaServerConfig = SpringContext.getBean(EurekaServerConfig.class);
            params.add(ServerParameterListBuilder.of(eurekaServerConfig));
        }

        return params;
    }
}
