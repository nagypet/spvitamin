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

package hu.perit.spvitamin.spring.security.ldap.admin;

import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterList;
import hu.perit.spvitamin.spring.security.ldap.config.LdapCollectionProperties;
import hu.perit.spvitamin.spring.security.ldap.config.RoleMappingProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LdapServerProperties {

    private final LdapCollectionProperties ldapCollectionProperties;
    private final RoleMappingProperties roleMappingProperties;

    @Autowired
    public LdapServerProperties(LdapCollectionProperties ldapCollectionProperties, RoleMappingProperties roleMappingProperties) {
        this.ldapCollectionProperties = ldapCollectionProperties;
        this.roleMappingProperties = roleMappingProperties;
    }

    @Bean(name = "LdapServerParameters")
    public ServerParameterList getParameterList()
    {
        ServerParameterList params = new ServerParameterList();

        for (Map.Entry<String, LdapCollectionProperties.LdapProperties> entry : this.ldapCollectionProperties.getLdaps().entrySet()) {
            params.add(ServerParameterList.of(entry.getValue(), "ldaps." + entry.getKey()));
        }

        for (Map.Entry<String, RoleMappingProperties.RoleMapping> entry : this.roleMappingProperties.getRoles().entrySet()) {
            params.add(ServerParameterList.of(entry.getValue(), "role." + entry.getKey()));
        }

        return params;
    }
}
