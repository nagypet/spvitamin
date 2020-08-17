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

package hu.perit.spvitamin.spring.auth.filter;

import hu.perit.spvitamin.spring.auth.jwt.JwtTokenProvider;
import hu.perit.spvitamin.spring.auth.jwt.TokenClaims;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.rest.model.AbstractAuthorizationToken;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public abstract class AbstractTokenAuthenticationFilter extends OncePerRequestFilter {

    protected abstract AbstractAuthorizationToken getJwtFromRequest(HttpServletRequest request);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            AbstractAuthorizationToken token = getJwtFromRequest(request);
            if (token != null) {
                String jwt = token.getJwt();

                if (StringUtils.hasText(jwt)) {

                    JwtTokenProvider tokenProvider = SpringContext.getBean(JwtTokenProvider.class);

                    TokenClaims claims = new TokenClaims(tokenProvider.getClaims(jwt));
                    Collection<? extends GrantedAuthority> authorities = claims.getAuthorities();

                    AuthenticatedUser authenticatedUser =
                            AuthenticatedUser.builder()
                                    .username(claims.getSubject())
                                    .authorities(authorities)
                                    .userId(claims.getUserId())
                                    .build();

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authorities);
                    authentication.setDetails(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        }
        catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            HandlerExceptionResolver resolver = SpringContext.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
            if (resolver.resolveException(request, response, null, ex) == null) {
                throw ex;
            }
        }
        catch (Exception ex) {
            SecurityContextHolder.clearContext();
            HandlerExceptionResolver resolver = SpringContext.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
            if (resolver.resolveException(request, response, null, new FilterAuthenticationException("Authentication failed!", ex)) == null) {
                throw ex;
            }
        }
    }
}
