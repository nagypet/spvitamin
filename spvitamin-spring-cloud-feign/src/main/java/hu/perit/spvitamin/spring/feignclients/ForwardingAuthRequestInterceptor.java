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
import hu.perit.spvitamin.spring.auth.AbstractAuthorizationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.function.Supplier;

public class ForwardingAuthRequestInterceptor implements RequestInterceptor
{
    private final Supplier<String> authorizationSupplier;


    public ForwardingAuthRequestInterceptor()
    {
        this.authorizationSupplier = this::getTokenFromSecurityContext;
    }


    public ForwardingAuthRequestInterceptor(Supplier<String> authorizationSupplier)
    {
        Objects.requireNonNull(authorizationSupplier);
        this.authorizationSupplier = authorizationSupplier;
    }


    @Override
    public void apply(RequestTemplate template)
    {
        String authorizationHeader = this.authorizationSupplier.get();
        if (authorizationHeader != null)
        {
            template.header("Authorization", authorizationHeader);
            template.header("Content-Type", "application/json");
        }
    }


    private String getTokenFromSecurityContext()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object details = authentication.getDetails();
        if (details instanceof AbstractAuthorizationToken authorizationToken)
        {
            return "Bearer " + authorizationToken.getJwt();
        }
        return null;
    }
}
