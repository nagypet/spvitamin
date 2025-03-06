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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RoleMapperServiceImplTest
{
    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_EMPTY = "ROLE_EMPTY";
    // Users
    public static final String ADMIN = "admin";
    public static final String USER = "user";
    public static final String MICROSOFT_ENTRA = "Microsoft Entra";
    public static final String ADMIN_USER = "admin-user";
    public static final String USER_USER = "user-user";
    // Groups
    public static final String ADMIN_GROUP = "admin-group";
    public static final String USER_GROUP = "user-group";

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
        assertThat(authenticatedUser.getAuthorities()).hasSize(3);
        assertThat(authenticatedUser.getAuthorities().stream().map(i -> i.getAuthority()).toList()).containsExactlyInAnyOrder(ROLE_USER, ROLE_ADMIN, ROLE_EMPTY);
    }


    @Test
    void testUserInAdminGroup()
    {

        RoleMapperService roleMapperService = new RoleMapperServiceImpl(roleMappingProperties);
        AuthenticatedUser authenticatedUser = roleMapperService.mapGrantedAuthorities(getUserInAdminGroup());
        log.info(authenticatedUser.toString());
        assertThat(authenticatedUser.getAuthorities()).hasSize(3);
        assertThat(authenticatedUser.getAuthorities().stream().map(i -> i.getAuthority()).toList()).containsExactlyInAnyOrder(ROLE_USER, ROLE_ADMIN, ROLE_EMPTY);
    }


    @Test
    void testUserNamedUser()
    {

        RoleMapperService roleMapperService = new RoleMapperServiceImpl(roleMappingProperties);
        AuthenticatedUser authenticatedUser = roleMapperService.mapGrantedAuthorities(getUserNamedUser());
        log.info(authenticatedUser.toString());
        assertThat(authenticatedUser.getAuthorities()).hasSize(2);
        assertThat(authenticatedUser.getAuthorities().stream().map(i -> i.getAuthority()).toList()).containsExactlyInAnyOrder(ROLE_USER, ROLE_EMPTY);
    }


    @Test
    void testUserNamedMicrosoftEntra()
    {

        RoleMapperService roleMapperService = new RoleMapperServiceImpl(roleMappingProperties);
        AuthenticatedUser authenticatedUser = roleMapperService.mapGrantedAuthorities(getUserNamedMicrosoftEntra());
        log.info(authenticatedUser.toString());
        assertThat(authenticatedUser.getAuthorities()).hasSize(2);
        assertThat(authenticatedUser.getAuthorities().stream().map(i -> i.getAuthority()).toList()).containsExactlyInAnyOrder(ROLE_EMPTY, ROLE_USER);
    }


    @Test
    void testUserInUserGroup()
    {
        RoleMapperService roleMapperService = new RoleMapperServiceImpl(roleMappingProperties);
        AuthenticatedUser authenticatedUser = roleMapperService.mapGrantedAuthorities(getUserInUserGroup());
        log.info(authenticatedUser.toString());
        assertThat(authenticatedUser.getAuthorities()).hasSize(2);
        assertThat(authenticatedUser.getAuthorities().stream().map(i -> i.getAuthority()).toList()).containsExactlyInAnyOrder(ROLE_USER, ROLE_EMPTY);
    }


    private AuthenticatedUser getUserNamedAdmin()
    {
        return AuthenticatedUser.builder()
                .username(ADMIN)
                .authorities(List.of(new SimpleGrantedAuthority(ROLE_EMPTY)))
                .build();
    }

    private AuthenticatedUser getUserNamedUser()
    {
        return AuthenticatedUser.builder()
                .username(USER)
                .authorities(List.of(new SimpleGrantedAuthority(ROLE_EMPTY)))
                .build();
    }

    private AuthenticatedUser getUserNamedMicrosoftEntra()
    {
        return AuthenticatedUser.builder()
                .username(MICROSOFT_ENTRA)
                .authorities(List.of(new SimpleGrantedAuthority(ROLE_EMPTY)))
                .build();
    }

    private AuthenticatedUser getUserInAdminGroup()
    {
        return AuthenticatedUser.builder()
                .username(ADMIN_USER)
                .authorities(List.of(new SimpleGrantedAuthority(ADMIN_GROUP), new SimpleGrantedAuthority(ROLE_EMPTY)))
                .build();
    }

    private AuthenticatedUser getUserInUserGroup()
    {
        return AuthenticatedUser.builder()
                .username(USER_USER)
                .authorities(List.of(new SimpleGrantedAuthority(USER_GROUP), new SimpleGrantedAuthority(ROLE_EMPTY)))
                .build();
    }

    private Map<String, RoleMappingProperties.RoleMapping> getRoles()
    {
        Map<String, RoleMappingProperties.RoleMapping> roles = new HashMap<>();
        RoleMappingProperties.RoleMapping adminRole = new RoleMappingProperties.RoleMapping();
        adminRole.setUsers(List.of(ADMIN));
        adminRole.setGroups(List.of(ADMIN_GROUP));
        adminRole.setIncludes(List.of(ROLE_USER));
        roles.put(ROLE_ADMIN, adminRole);

        RoleMappingProperties.RoleMapping userRole = new RoleMappingProperties.RoleMapping();
        userRole.setUsers(List.of(USER));
        userRole.setGroups(List.of(USER_GROUP));
        roles.put(ROLE_USER, userRole);

        RoleMappingProperties.RoleMapping emptyRole = new RoleMappingProperties.RoleMapping();
        emptyRole.setIncludes(List.of(ROLE_USER));
        roles.put(ROLE_EMPTY, emptyRole);

        return roles;
    }
}
