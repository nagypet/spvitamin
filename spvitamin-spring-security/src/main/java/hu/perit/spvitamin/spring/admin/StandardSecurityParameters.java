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
import hu.perit.spvitamin.spring.config.LocalUserProperties;
import hu.perit.spvitamin.spring.config.Role2PermissionMappingProperties;
import hu.perit.spvitamin.spring.config.RoleMappingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Map;

/**
 * @author Peter Nagy
 */


@Component
@RequiredArgsConstructor
class StandardSecurityParameters
{
    private final LocalUserProperties localUserProperties;
    private final RoleMappingProperties roleMappingProperties;
    private final Role2PermissionMappingProperties role2PermissionMappingProperties;


    @Bean(name = "StandardSecurityParameters")
    public ServerParameterList getParameterList()
    {
        ServerParameterList params = new ServerParameterListImpl();

        params.add(ServerParameterListBuilder.of(this.role2PermissionMappingProperties));

        for (Map.Entry<String, LocalUserProperties.User> entry : this.localUserProperties.getLocaluser().entrySet())
        {
            params.add("Local users", new ServerParameter(entry.getKey(), "", false));
        }

        for (Map.Entry<String, RoleMappingProperties.RoleMapping> entry : this.roleMappingProperties.getRoles().entrySet())
        {
            params.add(getRoleMappingGroupName(entry.getKey()), ServerParameterListBuilder.of(entry.getValue()));
        }

        return params;
    }


    private static String getRoleMappingGroupName(String role)
    {
        return MessageFormat.format("{0}: {1}", RoleMappingProperties.RoleMapping.class.getSimpleName(), role);
    }
}
