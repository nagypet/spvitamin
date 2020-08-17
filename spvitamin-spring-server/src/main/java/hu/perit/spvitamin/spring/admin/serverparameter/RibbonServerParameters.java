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

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import hu.perit.spvitamin.spring.config.SpringContext;
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

        DefaultClientConfigImpl clientConfigWithDefaultValues = DefaultClientConfigImpl.getClientConfigWithDefaultValues();
        if (clientConfigWithDefaultValues != null) {
            params.add(ServerParameterList.of(clientConfigWithDefaultValues, "Ribbon"));
        }

        return params;
    }
}
