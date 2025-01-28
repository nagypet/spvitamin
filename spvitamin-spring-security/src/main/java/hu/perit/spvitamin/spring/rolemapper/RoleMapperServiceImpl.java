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

package hu.perit.spvitamin.spring.rolemapper;

import hu.perit.spvitamin.spring.config.RoleMappingProperties;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps AD groups and users to roles
 *
 * @author Peter Nagy
 */

@Service
public class RoleMapperServiceImpl implements RoleMapperService
{
    public static final String ROLE_PREFIX = "ROLE_";

    private final RoleMappingProperties roleMappingProperties;

    public RoleMapperServiceImpl(RoleMappingProperties roleMappingProperties)
    {
        this.roleMappingProperties = roleMappingProperties;
    }


    @Override
    public boolean userHasRole(String username, String role)
    {
        Collection<GrantedAuthority> roles = this.mapAdGroupsAndUsers(username, Collections.emptySet());
        return roles.stream().map(i -> i.getAuthority()).anyMatch(i -> StringUtils.equalsIgnoreCase(i, role));
    }


    @Override
    public AuthenticatedUser mapGrantedAuthorities(AuthenticatedUser authenticatedUser)
    {
        Collection<? extends GrantedAuthority> adGroups = authenticatedUser.getAuthorities();

        Collection<GrantedAuthority> roles = this.mapAdGroupsAndUsers(authenticatedUser.getUsername(), adGroups);

        return AuthenticatedUser.builder() //
                .username(authenticatedUser.getUsername()) //
                .authorities(roles) //
                .userId(authenticatedUser.getUserId()) //
                .anonymous(authenticatedUser.isAnonymous())
                .ldapUrl(authenticatedUser.getLdapUrl())
                .build();
    }


    private Collection<GrantedAuthority> mapAdGroupsAndUsers(String username, Collection<? extends GrantedAuthority> adGroups)
    {
        Set<String> roles = this.roleMappingProperties.getUserRoles(username);
        for (GrantedAuthority adGroup : adGroups)
        {
            if (adGroup.getAuthority().startsWith(ROLE_PREFIX))
            {
                roles.add(adGroup.getAuthority());
            }
            else
            {
                roles.addAll(this.roleMappingProperties.getGroupRoles(adGroup.getAuthority()));
            }
        }

        return roles.stream().map(role -> new SimpleGrantedAuthority(prefixWithRole(role.toUpperCase()))).collect(Collectors.toSet());
    }


    private String prefixWithRole(String roleName)
    {
        if (roleName == null)
        {
            return "";
        }

        return roleName.startsWith(ROLE_PREFIX) ? roleName : ROLE_PREFIX + roleName;
    }
}
