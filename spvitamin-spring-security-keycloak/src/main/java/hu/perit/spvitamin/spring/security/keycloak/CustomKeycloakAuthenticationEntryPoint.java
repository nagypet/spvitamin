/*
 * Copyright 2020-2023 the original author or authors.
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

package hu.perit.spvitamin.spring.security.keycloak;

import hu.perit.spvitamin.spring.config.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CustomKeycloakAuthenticationEntryPoint extends KeycloakAuthenticationEntryPoint
{

    public CustomKeycloakAuthenticationEntryPoint(AdapterDeploymentContext adapterDeploymentContext, RequestMatcher apiRequestMatcher)
    {
        super(adapterDeploymentContext, apiRequestMatcher);
    }

    public CustomKeycloakAuthenticationEntryPoint(AdapterDeploymentContext adapterDeploymentContext)
    {
        super(adapterDeploymentContext);
    }


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException
    {
        super.commence(request, response, authException);
        HandlerExceptionResolver resolver = SpringContext.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
        if (resolver != null)
        {
            resolver.resolveException(request, response, null, authException);
        }
    }


    /**
     * Login redirect is disabled, because of CORS restrictions the Angular frontend is not able to redirect to the
     * Keycloak login page if the redirect comes from the backend.
     * After one and a half day desperate research I could not fix the CORS issue.
     *
     * Access to XMLHttpRequest at 'http://keycloak:8180/auth/realms/perit/protocol/openid-connect/auth?response_type=code
     * &client_id=keycloak-study-fe&redirect_uri=http%3A%2F%2Fbackend%3A9400%2Fsso%2Flogin&state=b654066f-a683-47ae-8ff4-c7a30dc95fd2&login=true&scope=openid'
     * (redirected from 'http://backend:9400/books') from origin 'http://backend:9400' has been blocked by CORS policy:
     * No 'Access-Control-Allow-Origin' header is present on the requested resource.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    protected void commenceLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        log.warn("Login redirect is disabled, because of CORS restrictions!");
    }
}
