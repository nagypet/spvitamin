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

package hu.perit.spvitamin.spring.security.auth;

import hu.perit.spvitamin.core.domainuser.DomainUser;
import hu.perit.spvitamin.spring.exception.AuthorizationException;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Peter Nagy
 */


@Service(value = "SpvitaminAuthorizationService")
public class AuthorizationService
{
    private final List<AuthenticatedUserFactory> authenticatedUserFactories = new ArrayList<>();


    public void registerAuthenticatedUserFactory(AuthenticatedUserFactory authenticatedUserFactory)
    {
        authenticatedUserFactories.add(authenticatedUserFactory);
    }


    public void setAuthenticatedUser(AuthenticatedUser authenticatedUser)
    {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (authenticatedUser != null)
        {
            LdapAuthenticationToken newAuthentication = new LdapAuthenticationToken(
                    authenticatedUser,
                    null,
                    authenticatedUser.getAuthorities(),
                    authenticatedUser.getSource(),
                    authenticatedUser.getDisplayName());

            Authentication authentication = securityContext.getAuthentication();
            if (authentication != null)
            {
                newAuthentication.setDetails(authentication.getDetails());
            }
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);
        }
        else
        {
            SecurityContextHolder.clearContext();
        }
    }


    public AuthenticatedUser getAuthenticatedUser()
    {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
        {
            return AuthenticatedUser.builder()
                    .username("anonymousUser")
                    .authorities(Collections.emptyList())
                    .userId(-1)
                    .anonymous(true)
                    .build();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser authenticatedUser)
        {
            return authenticatedUser;
        }
        else if (principal instanceof UserDetails userDetails)
        {
            if (authentication instanceof LdapAuthenticationToken ldapAuthenticationToken)
            {
                return AuthenticatedUser.builder()
                        .username(ldapAuthenticationToken.getName())
                        .displayName(ldapAuthenticationToken.getUserCN())
                        .authorities(ldapAuthenticationToken.getAuthorities())
                        .userId(-1)
                        .anonymous(false)
                        .source(ldapAuthenticationToken.getUrl())
                        .build();
            }
            return AuthenticatedUser.builder()
                    .username(DomainUser.newInstance(userDetails.getUsername()).getCanonicalName())
                    .authorities(userDetails.getAuthorities())
                    .userId(-1)
                    .anonymous(false)
                    .build();
        }
        else if (principal instanceof String username)
        {
            return AuthenticatedUser.builder()
                    .username(DomainUser.newInstance(username).getCanonicalName())
                    .authorities(Collections.emptyList())
                    .userId(-1)
                    .anonymous(false)
                    .build();
        }
        // So that we do not need the keycloak dependency 
        else if ("org.keycloak.KeycloakPrincipal".equals(principal.getClass().getName()))
        {
            return AuthenticatedUser.builder()
                    .username(((Principal) principal).getName())
                    .authorities(authentication.getAuthorities())
                    .userId(-1)
                    .anonymous(false).build();
        }

        // Trying specific factories
        for (AuthenticatedUserFactory authenticatedUserFactory : this.authenticatedUserFactories)
        {
            if (authenticatedUserFactory.canHandle(principal))
            {
                return authenticatedUserFactory.createAuthenticatedUser(principal);
            }
        }

        throw new AuthorizationException(
                String.format("Unknown principal type '%s'!", principal != null ? principal.getClass().getName() : "null"));
    }
}
