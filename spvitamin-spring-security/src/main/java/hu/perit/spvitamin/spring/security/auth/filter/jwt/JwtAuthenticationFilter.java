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

import hu.perit.spvitamin.spring.security.auth.filter.AbstractTokenAuthenticationFilter;
import hu.perit.spvitamin.spring.security.auth.filter.JwtString;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

/**
 * This class is intentionally no container. It should be invoked only for Jwt endpoints.
 *
 * @author Peter Nagy
 */

public class JwtAuthenticationFilter extends AbstractTokenAuthenticationFilter
{
    @Override
    protected JwtString getJwtFromRequest(HttpServletRequest request)
    {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer "))
        {
            return new JwtString(bearerToken.substring(7));
        }
        return null;
    }
}
