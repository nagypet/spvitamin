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

package hu.perit.spvitamin.spring.ribbon.admin;

import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameter;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("springContext")
public class RibbonServerParameters {

    @Bean(name = "RibbonServerParameters")
    public ServerParameterList getParameterList()
    {
        ServerParameterList params = new ServerParameterList();

        //DefaultClientConfigImpl clientConfigWithDefaultValues = DefaultClientConfigImpl.getClientConfigWithDefaultValues();
        /*
        ClientConfigFactory clientConfigFactory = ClientConfigFactory.DEFAULT;
        IClientConfig iClientConfig = clientConfigFactory.newConfig();
        if (iClientConfig != null) {
            params.add(ServerParameterList.of(iClientConfig, iClientConfig.getClientName()));
        }
        */
        params.add(new ServerParameter("Ribbon", "Config not available: TODO: find out how to get actual config", false));

        return params;
    }
}
