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

package hu.perit.spvitamin.spring.security.auth.filter;

import hu.perit.spvitamin.spring.config.Role2PermissionMappingProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.rolemapper.RoleMapperService;
import hu.perit.spvitamin.spring.rolemapper.RoleMapperServiceImpl;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Peter Nagy
 */

@Slf4j
public class Role2PermissionMapperFilter extends OncePerRequestFilter
{

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        AuthorizationService authorizationService = SpringContext.getBean(AuthorizationService.class);

        try
        {
            log.debug("{} called", this.getClass().getName());

            AuthenticatedUser authenticatedUser = authorizationService.getAuthenticatedUser();

            if (!authenticatedUser.isAnonymous())
            {

                RoleMapperService mapperService = SpringContext.getBean(RoleMapperServiceImpl.class);
                AuthenticatedUser authenticatedUserWithRoles = mapperService.mapGrantedAuthorities(authenticatedUser);

                log.debug(String.format("Mapping roles => privileges for authenticated user: '%s'", authenticatedUserWithRoles.toString()));

                // Mapping roles => privileges
                Collection<? extends GrantedAuthority> privileges = mapPrivileges2Roles(authenticatedUserWithRoles.getAuthorities());
                AuthenticatedUser authenticatedUserWithPrivileges = AuthenticatedUser.builder() //
                        .username(authenticatedUserWithRoles.getUsername()) //
                        .authorities(privileges) //
                        .anonymous(authenticatedUserWithRoles.isAnonymous())
                        .userId(authenticatedUserWithRoles.getUserId())
                        .ldapUrl(authenticatedUserWithRoles.getLdapUrl())
                        .build();
                log.debug(String.format("Granted privileges: '%s'", privileges.toString()));

                authorizationService.setAuthenticatedUser(authenticatedUserWithPrivileges);
            }

            filterChain.doFilter(request, response);
        }
        catch (AuthenticationException ex)
        {
            SecurityContextHolder.clearContext();
            HandlerExceptionResolver resolver = SpringContext.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
            if (resolver.resolveException(request, response, null, ex) == null)
            {
                throw ex;
            }
        }
        catch (Exception ex)
        {
            SecurityContextHolder.clearContext();
            HandlerExceptionResolver resolver = SpringContext.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
            if (resolver.resolveException(request, response, null,
                    new FilterAuthenticationException("Authentication failed!", ex)) == null)
            {
                throw ex;
            }
        }
        finally
        {
            authorizationService.setAuthenticatedUser(null);
        }
    }


    private Collection<? extends GrantedAuthority> mapPrivileges2Roles(Collection<? extends GrantedAuthority> authorities)
    {

        Role2PermissionMappingProperties role2PermissionMapping = SpringContext.getBean(Role2PermissionMappingProperties.class);

        Set<GrantedAuthority> permissions = new HashSet<>();

        // filtering only ROLEs
        List<? extends GrantedAuthority> roles = authorities.stream().filter(a -> a.getAuthority().startsWith("ROLE_"))
                .collect(Collectors.toList());
        for (GrantedAuthority role : roles)
        {
            // Adding the role itself
            permissions.add(role);

            if (role2PermissionMapping.getRolemap().containsKey(role.getAuthority()))
            {
                List<String> permissionList = role2PermissionMapping.getRolemap().get(role.getAuthority());
                permissions.addAll(permissionList.stream().map(p -> new SimpleGrantedAuthority(p)).collect(Collectors.toList()));
            }
        }

        return permissions;
    }
}
