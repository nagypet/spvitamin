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

import hu.perit.spvitamin.spring.config.Role2PermissionMappingProperties;
import hu.perit.spvitamin.spring.config.RoleMappingProperties;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps AD groups and users to roles
 *
 * @author Peter Nagy
 */

@Service
@RequiredArgsConstructor
public class RoleMapperServiceImpl implements RoleMapperService
{
    public static final String ROLE_PREFIX = "ROLE_";

    private final RoleMappingProperties roleMappingProperties;
    private final Role2PermissionMappingProperties role2PermissionMappingProperties;


    @Override
    public AuthenticatedUser mapGrantedAuthorities(AuthenticatedUser authenticatedUser)
    {
        Collection<? extends GrantedAuthority> groups = authenticatedUser.getAuthorities();

        Collection<GrantedAuthority> roles = this.mapUsernameAndGroupToRoles(authenticatedUser.getUsername(), groups);

        // Mapping roles => privileges
        Collection<? extends GrantedAuthority> privileges = mapRolesToPrivileges(roles);

        AuthenticatedUser clone = authenticatedUser.clone();
        clone.setAuthorities(privileges);
        return clone;
    }


    @Override
    public Set<GrantedAuthority> mapUsernameAndGroupToRoles(String username, Collection<? extends GrantedAuthority> groups)
    {
        Set<String> roles = this.roleMappingProperties.getUserRoles(username);
        if (groups != null)
        {
            for (GrantedAuthority group : groups)
            {
                if (group.getAuthority().startsWith(ROLE_PREFIX))
                {
                    // This is a ROLE_
                    String role = group.getAuthority();
                    roles.add(role);
                    roles.addAll(roleMappingProperties.getIncludedRoles(role));
                }
                else
                {
                    // This is an AD group
                    roles.addAll(this.roleMappingProperties.getGroupRoles(group.getAuthority()));
                }
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


    @Override
    public Set<GrantedAuthority> mapRolesToPrivileges(Collection<? extends GrantedAuthority> authorities)
    {
        Set<GrantedAuthority> permissions = new HashSet<>();

        // filtering only ROLEs
        List<? extends GrantedAuthority> roles = authorities.stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_"))
                .toList();
        for (GrantedAuthority role : roles)
        {
            // Adding the role itself
            permissions.add(role);

            if (role2PermissionMappingProperties.getRolemap().containsKey(role.getAuthority()))
            {
                List<String> permissionList = role2PermissionMappingProperties.getRolemap().get(role.getAuthority());
                permissions.addAll(permissionList.stream().map(p -> new SimpleGrantedAuthority(p)).toList());
            }
        }

        return permissions;
    }
}
