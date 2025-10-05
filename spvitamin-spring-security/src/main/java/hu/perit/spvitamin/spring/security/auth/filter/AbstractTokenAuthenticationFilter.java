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
import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.exception.InvalidTokenException;
import hu.perit.spvitamin.spring.info.CookieHelper;
import hu.perit.spvitamin.spring.info.RequestQuery;
import hu.perit.spvitamin.spring.rest.api.AuthApi;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.LdapAuthenticationToken;
import hu.perit.spvitamin.spring.security.auth.jwt.JwtTokenProvider;
import hu.perit.spvitamin.spring.security.auth.jwt.TokenClaims;
import hu.perit.spvitamin.spring.session.registry.AdvancedSessionRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import java.text.MessageFormat;
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
            log.debug("{} called", this.getClass().getSimpleName());

            AbstractAuthorizationToken token = getJwtFromRequest(request);
            if (token != null)
            {
                String jwt = token.getJwt();

                if (StringUtils.isNotBlank(jwt))
                {
                    JwtTokenProvider tokenProvider = SpringContext.getBean(JwtTokenProvider.class);

                    TokenClaims claims = new TokenClaims(tokenProvider.getClaims(jwt));

                    // Checking token validity only in AUTHORIZATION_SERVER mode
                    SecurityProperties securityProperties = SpringContext.getBean(SecurityProperties.class);
                    if (securityProperties.getMode() == SecurityProperties.Mode.AUTHORIZATION_SERVER)
                    {
                        String sessionIdInToken = claims.getSessionId();
                        String sessionIdInRequest = Optional.ofNullable(request.getSession(false)).map(HttpSession::getId).orElse(null);
                        checkTokenValidity(sessionIdInToken, sessionIdInRequest, RequestQuery.isFromBrowser(), tokenProvider.getTokenType(jwt));
                    }

                    AuthenticatedUser authenticatedUser = AuthenticatedUser.fromClaims(claims);
                    log.debug(String.format("Authentication restored from JWT token: '%s'", authenticatedUser.toString()));

                    UsernamePasswordAuthenticationToken authentication;
                    Collection<? extends GrantedAuthority> privileges = authenticatedUser.getAuthorities();
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

            filterChain.doFilter(new BearerTokenMaskingRequestWrapper(request), response);
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
            if (resolver.resolveException(request, response, null, new InvalidTokenException("Authentication failed!", ex)) == null)
            {
                throw ex;
            }
        }
        finally
        {
            SecurityContextHolder.clearContext();
        }
    }


    private static void checkTokenValidity(String sessionIdInToken, String sessionIdInRequest, boolean fromBrowser, JwtTokenProvider.Type tokenType)
    {
        SecurityProperties securityProperties = SpringContext.getBean(SecurityProperties.class);

        // Checking session validity (not for access- and refresh tokens)
        if ((tokenType == JwtTokenProvider.Type.JWT && isAuthenticateEndpoint()))
        {
            AdvancedSessionRegistry sessionRegistry = SpringContext.getBean(AdvancedSessionRegistry.class);
            SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionIdInToken);
            if (sessionInformation == null || sessionInformation.isExpired())
            {
                log.info("Session {} expired!", sessionIdInToken);
                throw new InvalidTokenException(MessageFormat.format("Session {0} expired!", sessionIdInToken));
            }

            // Additionally, if the request comes from a browser, then the token must match with the request too
            if (fromBrowser && !StringUtils.equalsIgnoreCase(sessionIdInToken, sessionIdInRequest))
            {
                // The token has been issued for another session
                log.info("sessionIdInToken: {}, sessionIdInRequest: {}", sessionIdInToken, sessionIdInRequest);
                throw new InvalidTokenException("Invalid session id in JWT token!");
            }

            // If the token does not contain basic auth credentials, then the cookie must contain a valid refresh token.
            // Backend components are always calling with basic authentication
            HttpServletRequest httpServletRequest = RequestQuery.getHttpServletRequest();
            String authorizationHeader = Optional.ofNullable(httpServletRequest).map(i -> i.getHeader("Authorization")).orElse(null);
            if (authorizationHeader == null || !authorizationHeader.startsWith("Basic"))
            {
                String refreshJwt = CookieHelper.getCookieValue(securityProperties.getAuth().getRefreshTokenCookieName(), httpServletRequest);
                JwtTokenProvider jwtTokenProvider = SpringContext.getBean(JwtTokenProvider.class);
                AuthorizationToken refreshToken = jwtTokenProvider.getAuthorizationTokenFromJwt(refreshJwt);
                // Client-ID must match
                if (!StringUtils.equalsIgnoreCase(refreshToken.getClientId(), securityProperties.getAuth().getClientId()))
                {
                    log.warn("Client-ID mismatch in JWT token!");
                    throw new InvalidTokenException("Invalid refresh token!");
                }

                // Token type
                if (refreshToken.getType() != JwtTokenProvider.Type.REFRESH)
                {
                    log.warn("Token type mismatch in JWT token! Expected: {}, actual: {}", JwtTokenProvider.Type.REFRESH, refreshToken.getType());
                    throw new InvalidTokenException("Invalid refresh token!");
                }
            }
        }
    }


    private static boolean isAuthenticateEndpoint()
    {
        String servletPath = Optional.ofNullable(RequestQuery.getHttpServletRequest()).map(i -> i.getServletPath()).orElse(null);
        return StringUtils.equalsIgnoreCase(servletPath, AuthApi.BASE_URL_AUTHENTICATE);
    }
}
