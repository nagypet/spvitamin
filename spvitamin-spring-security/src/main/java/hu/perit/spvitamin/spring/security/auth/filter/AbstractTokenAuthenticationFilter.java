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
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.info.CookieHelper;
import hu.perit.spvitamin.spring.info.RequestQuery;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.LdapAuthenticationToken;
import hu.perit.spvitamin.spring.security.auth.jwt.JwtTokenProvider;
import hu.perit.spvitamin.spring.security.auth.jwt.TokenClaims;
import hu.perit.spvitamin.spring.session.local.AdvancedSessionRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;


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

                if (StringUtils.isNotBlank(jwt))
                {
                    JwtTokenProvider tokenProvider = SpringContext.getBean(JwtTokenProvider.class);

                    TokenClaims claims = new TokenClaims(tokenProvider.getClaims(jwt));

                    // Checking sessionId
                    String sessionIdInToken = claims.getSessionId();
                    String sessionIdInRequest = Optional.ofNullable(request.getSession(false)).map(i -> i.getId()).orElse(null);
                    checkSessionValidity(sessionIdInToken, sessionIdInRequest, RequestQuery.isFromBrowser());

                    AuthenticatedUser authenticatedUser = AuthenticatedUser.fromClaims(claims);
                    log.debug(String.format("Authentication restored from JWT token: '%s'", authenticatedUser.toString()));

                    UsernamePasswordAuthenticationToken authentication;
                    Collection<? extends GrantedAuthority> privileges = claims.getAuthorities();
                    if (StringUtils.isNotBlank(authenticatedUser.getSource()))
                    {
                        authentication = new LdapAuthenticationToken(authenticatedUser, null, privileges, authenticatedUser.getSource(), claims.getPreferredUsername());
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
            CookieHelper.clearSessionCookie(request, response);
            SecurityContextHolder.clearContext();
            HandlerExceptionResolver resolver = SpringContext.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
            if (resolver.resolveException(request, response, null, ex) == null)
            {
                throw ex;
            }
        }
        catch (Exception ex)
        {
            CookieHelper.clearSessionCookie(request, response);
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


    private static void checkSessionValidity(String sessionIdInToken, String sessionIdInRequest, boolean fromBrowser)
    {
        SecurityProperties securityProperties = SpringContext.getBean(SecurityProperties.class);
        if (!securityProperties.isSessionValidationEnabled())
        {
            return;
        }

        // Checking session validity
        AdvancedSessionRegistry sessionRegistry = SpringContext.getBean(AdvancedSessionRegistry.class);
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionIdInToken);
        if (sessionInformation == null || sessionInformation.isExpired())
        {
            log.info("Session {} expired!", sessionIdInToken);
            throw new FilterAuthenticationException("Session expired!");
        }

        // Additionally, if the request comes from a browser, then the token must match with the request too
        if (fromBrowser && !StringUtils.equalsIgnoreCase(sessionIdInToken, sessionIdInRequest))
        {
            // The token has been issued for another session
            log.info("sessionIdInToken: {}, sessionIdInRequest: {}", sessionIdInToken, sessionIdInRequest);
            throw new FilterAuthenticationException("Invalid session id in JWT token!");
        }
    }
}
