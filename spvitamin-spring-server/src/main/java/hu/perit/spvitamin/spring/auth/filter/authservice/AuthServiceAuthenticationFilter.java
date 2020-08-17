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

package hu.perit.spvitamin.spring.auth.filter.authservice;

import hu.perit.spvitamin.spring.auth.provider.authservice.AuthServiceAuthenticationProviderWithFeign;
import hu.perit.spvitamin.spring.auth.filter.AbstractTokenAuthenticationFilter;
import hu.perit.spvitamin.spring.feignclients.ForwardingAuthRequestInterceptor;
import hu.perit.spvitamin.spring.rest.client.TemplateAuthClient;
import hu.perit.spvitamin.spring.feignclients.SimpleFeignClientBuilder;
import hu.perit.spvitamin.spring.rest.model.AuthorizationToken;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;

/**
 * An alternative way of using AuthServiceAuthentication
 *
 *     @Override
 *     protected void configure(HttpSecurity http) throws Exception {
 *         SimpleHttpSecurityBuilder.newInstance(http)
 *                 .scope(
 *                         AuthApi.BASE_URL_AUTHENTICATE
 *                 )
 *                 .defaults()
 *                 .exceptionHandler(this.authenticationEntryPoint, this.accessDeniedHandler)
 *                 .ignorePersistedSecurity().and()
 *                 .authorizeRequests()
 *                 .anyRequest().authenticated();
 *
 *         http.addFilterAfter(new AuthServiceAuthenticationFilter(), SecurityContextPersistenceFilter.class);
 *     }
 */
public class AuthServiceAuthenticationFilter extends AbstractTokenAuthenticationFilter {

    @Override
    protected AuthorizationToken getJwtFromRequest(HttpServletRequest request) {
        TemplateAuthClient templateAuthClient = SimpleFeignClientBuilder.newInstance()
                .requestInterceptor(new ForwardingAuthRequestInterceptor(request.getHeader(HttpHeaders.AUTHORIZATION)))
                .build(TemplateAuthClient.class, AuthServiceAuthenticationProviderWithFeign.getServiceUrl());

        return templateAuthClient.authenticate(null);
    }
}
