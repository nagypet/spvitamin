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

package hu.perit.spvitamin.spring.security.ldap;

import hu.perit.spvitamin.spring.config.RoleMappingProperties;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import lombok.extern.log4j.Log4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps AD groups and users to roles
 *
 * @author Peter Nagy
 */

@Component
@Log4j
public class AdGroupRoleMapper {

    private final RoleMappingProperties roleMappingProperties;

    public AdGroupRoleMapper(RoleMappingProperties roleMappingProperties) {
        this.roleMappingProperties = roleMappingProperties;
    }

    public AuthenticatedUser mapGrantedAuthorities(AuthenticatedUser authenticatedUser) {
        Collection<? extends GrantedAuthority> adGroups = authenticatedUser.getAuthorities();

        Collection<GrantedAuthority> roles = this.mapAdGroupsAndUsers(authenticatedUser.getUsername(), adGroups);

        return AuthenticatedUser.builder()
                .username(authenticatedUser.getUsername())
                .authorities(roles)
                .userId(authenticatedUser.getUserId())
                .build();
    }


    private Collection<GrantedAuthority> mapAdGroupsAndUsers(String username, Collection<? extends GrantedAuthority> adGroups) {
        Set<String> roles = this.roleMappingProperties.getUserRoles(username);
        for (GrantedAuthority adGroup : adGroups) {
            if (adGroup.getAuthority().startsWith("ROLE_")) {
                roles.add(adGroup.getAuthority().substring(5));
            }
            else {
                roles.addAll(this.roleMappingProperties.getGroupRoles(adGroup.getAuthority()));
            }
        }

        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())).collect(Collectors.toSet());
    }
}
