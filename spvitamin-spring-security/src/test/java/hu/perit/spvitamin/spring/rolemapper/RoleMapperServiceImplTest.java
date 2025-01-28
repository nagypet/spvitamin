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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RoleMapperServiceImplTest
{
    private RoleMappingProperties roleMappingProperties;


    @BeforeEach
    void setup()
    {
        this.roleMappingProperties = new RoleMappingProperties();
        this.roleMappingProperties.setRoles(getRoles());
    }


    @Test
    void testUserNamedAdmin()
    {

        RoleMapperService roleMapperService = new RoleMapperServiceImpl(roleMappingProperties);
        AuthenticatedUser authenticatedUser = roleMapperService.mapGrantedAuthorities(getUserNamedAdmin());
        log.info(authenticatedUser.toString());
        assertThat(authenticatedUser.getAuthorities()).hasSize(2);
        assertThat(authenticatedUser.getAuthorities().stream().map(i -> i.getAuthority()).toList()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }


    @Test
    void testUserInAdminGroup()
    {

        RoleMapperService roleMapperService = new RoleMapperServiceImpl(roleMappingProperties);
        AuthenticatedUser authenticatedUser = roleMapperService.mapGrantedAuthorities(getUserInAdminGroup());
        log.info(authenticatedUser.toString());
        assertThat(authenticatedUser.getAuthorities()).hasSize(2);
        assertThat(authenticatedUser.getAuthorities().stream().map(i -> i.getAuthority()).toList()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }


    @Test
    void testUserNamedUser()
    {

        RoleMapperService roleMapperService = new RoleMapperServiceImpl(roleMappingProperties);
        AuthenticatedUser authenticatedUser = roleMapperService.mapGrantedAuthorities(getUserNamedUser());
        log.info(authenticatedUser.toString());
        assertThat(authenticatedUser.getAuthorities()).hasSize(1);
        assertThat(authenticatedUser.getAuthorities().stream().map(i -> i.getAuthority()).toList()).containsExactlyInAnyOrder("ROLE_USER");
    }


    @Test
    void testUserInUserGroup()
    {
        RoleMapperService roleMapperService = new RoleMapperServiceImpl(roleMappingProperties);
        AuthenticatedUser authenticatedUser = roleMapperService.mapGrantedAuthorities(getUserInUserGroup());
        log.info(authenticatedUser.toString());
        assertThat(authenticatedUser.getAuthorities()).hasSize(1);
        assertThat(authenticatedUser.getAuthorities().stream().map(i -> i.getAuthority()).toList()).containsExactlyInAnyOrder("ROLE_USER");
    }


    @Test
    void testUserHasRole()
    {
        RoleMapperService roleMapperService = new RoleMapperServiceImpl(roleMappingProperties);
        assertThat(roleMapperService.userHasRole("admin", "ROLE_ADMIN")).isTrue();
        assertThat(roleMapperService.userHasRole("admin", "ROLE_USER")).isTrue();
        assertThat(roleMapperService.userHasRole("user", "ROLE_ADMIN")).isFalse();
        assertThat(roleMapperService.userHasRole("user", "ROLE_USER")).isTrue();
    }


    private AuthenticatedUser getUserNamedAdmin()
    {
        return AuthenticatedUser.builder()
                .username("admin")
                .authorities(Collections.emptyList())
                .build();
    }

    private AuthenticatedUser getUserNamedUser()
    {
        return AuthenticatedUser.builder()
                .username("user")
                .authorities(Collections.emptyList())
                .build();
    }

    private AuthenticatedUser getUserInAdminGroup()
    {
        return AuthenticatedUser.builder()
                .username("admin-user")
                .authorities(List.of(new SimpleGrantedAuthority("admin-group")))
                .build();
    }

    private AuthenticatedUser getUserInUserGroup()
    {
        return AuthenticatedUser.builder()
                .username("user-user")
                .authorities(List.of(new SimpleGrantedAuthority("user-group")))
                .build();
    }

    private Map<String, RoleMappingProperties.RoleMapping> getRoles()
    {
        Map<String, RoleMappingProperties.RoleMapping> roles = new HashMap<>();
        RoleMappingProperties.RoleMapping adminRole = new RoleMappingProperties.RoleMapping();
        adminRole.setUsers(List.of("admin"));
        adminRole.setGroups(List.of("admin-group"));
        adminRole.setIncludes(List.of("ROLE_USER"));
        roles.put("ROLE_ADMIN", adminRole);

        RoleMappingProperties.RoleMapping userRole = new RoleMappingProperties.RoleMapping();
        userRole.setUsers(List.of("user"));
        userRole.setGroups(List.of("user-group"));
        roles.put("ROLE_USER", userRole);

        return roles;
    }
}
