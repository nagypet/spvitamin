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

package hu.perit.spvitamin.spring.auth;

import hu.perit.spvitamin.spring.exception.AuthorizationException;
import hu.perit.spvitamin.spring.rest.model.AbstractAuthorizationToken;
import hu.perit.spvitamin.spring.rest.model.AuthorizationToken;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author Peter Nagy
 */


@Service
public class AuthorizationService {

    public AbstractAuthorizationToken getToken() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object details = authentication.getDetails();
        if (details instanceof AuthorizationToken) {
            return (AuthorizationToken) details;
        }

        return null;
    }


    public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
        newAuthentication.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }


    public AuthenticatedUser getAuthenticatedUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            return AuthenticatedUser.builder()
                    .username("No authentication")
                    .authorities(Collections.emptyList())
                    .userId(-1)
                    .build();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser) {
            return (AuthenticatedUser) principal;
        }
        else if (principal instanceof UserDetails) {
            return AuthenticatedUser.builder()
                    .username(((UserDetails) principal).getUsername())
                    .authorities(((UserDetails) principal).getAuthorities())
                    .userId(-1)
                    .build();
        }
        else if (principal instanceof String) {
            String username = (String) principal;
            return AuthenticatedUser.builder()
                    .username(username)
                    .authorities(Collections.emptyList())
                    .userId(-1)
                    .build();
        }

        throw new AuthorizationException(String.format("Unknown principal type '%s'!", principal != null ? principal.getClass().getName() : "null"));
    }
}
