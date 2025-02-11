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

package hu.perit.spvitamin.spring.feignclients;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import hu.perit.spvitamin.spring.security.auth.filter.JwtString;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ForwardingAuthRequestInterceptor implements RequestInterceptor
{
    private String authorization;

    public ForwardingAuthRequestInterceptor()
    {
        this.authorization = null;
    }

    public ForwardingAuthRequestInterceptor(String authorization)
    {
        this.authorization = authorization;
    }

    @Override
    public void apply(RequestTemplate template)
    {
        if (this.authorization == null)
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object details = authentication.getDetails();
            if (details instanceof JwtString jwtString)
            {
                this.authorization = "Bearer " + jwtString.getJwt();
            }
        }
        template.header("Authorization", this.authorization);
        template.header("Content-Type", "application/json");
    }
}
