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

package hu.perit.spvitamin.spring.security.authservice.provider;

import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.jwt.JwtTokenProvider;
import hu.perit.spvitamin.spring.security.auth.jwt.TokenClaims;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AuthServiceAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
                throw new UnsupportedOperationException("Only UsernamePasswordAuthenticationToken supported!");
            }

            UsernamePasswordAuthenticationToken inputToken = (UsernamePasswordAuthenticationToken) authentication;

            AuthorizationToken token = this.getAuthorizationToken((String) inputToken.getPrincipal(), (String) inputToken.getCredentials());

            String jwt = token.getJwt();

            JwtTokenProvider tokenProvider = SpringContext.getBean(JwtTokenProvider.class);

            TokenClaims claims = new TokenClaims(tokenProvider.getClaims(jwt));
            Collection<? extends GrantedAuthority> authorities = claims.getAuthorities();

            AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                    .username(claims.getSubject())
                    .authorities(authorities)
                    .userId(claims.getUserId())
                    .build();

            UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authorities);
            newAuthentication.setDetails(token);

            return newAuthentication;
        }
        catch (RuntimeException ex) {
            log.debug("Authentication failed: " + ex.getMessage());
            throw new AuthServiceAuthenticationException("Authentication failed!", ex);
        }
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }

    protected abstract AuthorizationToken getAuthorizationToken(String userName, String password);
}
