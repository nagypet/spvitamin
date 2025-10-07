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

package hu.perit.spvitamin.spring.security.auth.filter.jwt;

import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.info.CookieHelper;
import hu.perit.spvitamin.spring.security.auth.filter.AbstractTokenAuthenticationFilter;
import hu.perit.spvitamin.spring.security.auth.filter.JwtString;
import hu.perit.spvitamin.spring.security.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

/**
 * This class is intentionally no container. It should be invoked only for Jwt endpoints.
 *
 * @author Peter Nagy
 */

@Slf4j
public class JwtAuthenticationFilter extends AbstractTokenAuthenticationFilter
{
    /**
     * Extracts a JWT (JSON Web Token) from the provided HTTP request by checking various possible sources,
     * including the Authorization header and cookies.
     *
     * @param request the HttpServletRequest object from which the JWT is to be extracted.
     * @return a JwtString object containing the extracted JWT if available; otherwise, returns null.
     */
    @Override
    protected JwtString getJwtFromRequest(HttpServletRequest request)
    {
        // If there is a Basic auth header, we do nothing
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotBlank(authorization) && authorization.startsWith("Basic "))
        {
            return null;
        }

        // Then, try to get the token from the authorization header
        if (StringUtils.isNotBlank(authorization) && authorization.startsWith("Bearer ") && authorization.length() > 7)
        {
            String tokenInHeader = authorization.substring(7);
            boolean dummyToken = StringUtils.equals(tokenInHeader, JwtTokenProvider.HIDDEN);
            if (!dummyToken)
            {
                return new JwtString(tokenInHeader);
            }
        }

        // After that, check if there is an access-token cookie
        SecurityProperties securityProperties = SpringContext.getBean(SecurityProperties.class);
        if (securityProperties.getAuth() != null)
        {
            String accessTokenInCookie = CookieHelper.getCookieValue(securityProperties.getAuth().getAccessTokenCookieName(), request);
            if (StringUtils.isNotBlank(accessTokenInCookie))
            {
                // Returning only if valid
                JwtTokenProvider tokenProvider = SpringContext.getBean(JwtTokenProvider.class);
                if (!tokenProvider.isExpired(accessTokenInCookie))
                {
                    return new JwtString(accessTokenInCookie);
                }
            }
        }

        // Finally, check if there is a refresh-token cookie
        if (securityProperties.getAuth() != null)
        {
            String refreshTokenInCookie = CookieHelper.getCookieValue(securityProperties.getAuth().getRefreshTokenCookieName(), request);
            if (StringUtils.isNotBlank(refreshTokenInCookie))
            {
                return new JwtString(refreshTokenInCookie);
            }
        }
        return null;
    }
}
