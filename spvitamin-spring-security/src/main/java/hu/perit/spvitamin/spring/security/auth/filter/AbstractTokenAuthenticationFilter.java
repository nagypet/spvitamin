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

import hu.perit.spvitamin.spring.auth.AbstractAuthorizationToken;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.LdapAuthenticationToken;
import hu.perit.spvitamin.spring.security.auth.jwt.JwtTokenProvider;
import hu.perit.spvitamin.spring.security.auth.jwt.TokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collection;

@Slf4j
public abstract class AbstractTokenAuthenticationFilter extends OncePerRequestFilter
{

    protected abstract AbstractAuthorizationToken getJwtFromRequest(HttpServletRequest request);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        try
        {
            log.debug("AbstractTokenAuthenticationFilter called.");

            AbstractAuthorizationToken token = getJwtFromRequest(request);
            if (token != null)
            {
                String jwt = token.getJwt();

                if (StringUtils.hasText(jwt))
                {
                    JwtTokenProvider tokenProvider = SpringContext.getBean(JwtTokenProvider.class);

                    TokenClaims claims = new TokenClaims(tokenProvider.getClaims(jwt));
                    Collection<? extends GrantedAuthority> privileges = claims.getAuthorities();

                    AuthenticatedUser authenticatedUser =
                            AuthenticatedUser.builder()
                                    .username(claims.getSubject())
                                    .authorities(privileges)
                                    .userId(claims.getUserId())
                                    .anonymous(false)
                                    .build();

                    log.debug(String.format("Authentication restored from JWT token: '%s'", authenticatedUser.toString()));

                    UsernamePasswordAuthenticationToken authentication;
                    if (StringUtils.hasText(authenticatedUser.getLdapUrl()))
                    {
                        authentication = new LdapAuthenticationToken(authenticatedUser, null, privileges, authenticatedUser.getLdapUrl());
                    }
                    else
                    {
                        authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null, privileges);
                    }
                    authentication.setDetails(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
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
            if (resolver.resolveException(request, response, null, new FilterAuthenticationException("Authentication failed!", ex)) == null)
            {
                throw ex;
            }
        }
        finally
        {
            SecurityContextHolder.clearContext();
        }
    }
}
