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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.AdapterTokenStore;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RequestAuthenticator;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.KeycloakAuthenticationException;
import org.keycloak.adapters.springsecurity.authentication.RequestAuthenticatorFactory;
import org.keycloak.adapters.springsecurity.facade.SimpleHttpFacade;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.token.AdapterTokenStoreFactory;
import org.keycloak.adapters.springsecurity.token.SpringSecurityAdapterTokenStoreFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import hu.perit.spvitamin.spring.config.SpringContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomKeycloakAuthenticationProcessingFilter extends KeycloakAuthenticationProcessingFilter
{
    private final AuthenticationManager authenticationManager;
    private AdapterDeploymentContext adapterDeploymentContext;
    private final AdapterTokenStoreFactory adapterTokenStoreFactory = new SpringSecurityAdapterTokenStoreFactory();
    private final RequestAuthenticatorFactory requestAuthenticatorFactory = new CustomSpringSecurityRequestAuthenticatorFactory();


    public CustomKeycloakAuthenticationProcessingFilter(AuthenticationManager authenticationManager,
        RequestMatcher requiresAuthenticationRequestMatcher)
    {
        super(authenticationManager, requiresAuthenticationRequestMatcher);
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void afterPropertiesSet()
    {
        adapterDeploymentContext = SpringContext.getBean(AdapterDeploymentContext.class);
        super.afterPropertiesSet();
    }

    public CustomKeycloakAuthenticationProcessingFilter(AuthenticationManager authenticationManager)
    {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException, ServletException
    {
        log.debug("Attempting Keycloak authentication");

        HttpFacade facade = new SimpleHttpFacade(request, response);
        KeycloakDeployment deployment = adapterDeploymentContext.resolveDeployment(facade);

        // using Spring authenticationFailureHandler
        deployment.setDelegateBearerErrorResponseSending(true);

        AdapterTokenStore tokenStore = adapterTokenStoreFactory.createAdapterTokenStore(deployment, request, response);
        RequestAuthenticator authenticator = requestAuthenticatorFactory.createRequestAuthenticator(facade, request, deployment, tokenStore,
            -1);

        AuthOutcome result = authenticator.authenticate();
        log.debug("Auth outcome: {}", result);

        if (AuthOutcome.FAILED.equals(result))
        {
            AuthChallenge challenge = authenticator.getChallenge();
            if (challenge != null)
            {
                challenge.challenge(facade);
            }
            throw new KeycloakAuthenticationException("Invalid authorization header, see WWW-Authenticate header for details");
        }

        if (AuthOutcome.NOT_ATTEMPTED.equals(result))
        {
            AuthChallenge challenge = authenticator.getChallenge();
            if (challenge != null)
            {
                challenge.challenge(facade);
            }
            if (deployment.isBearerOnly())
            {
                // no redirection in this mode, throwing exception for the spring handler
                throw new KeycloakAuthenticationException("Authorization header not found,  see WWW-Authenticate header");
            }
            else
            {
                // let continue if challenged, it may redirect
                return null;
            }
        }

        else if (AuthOutcome.AUTHENTICATED.equals(result))
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Assert.notNull(authentication, "Authentication SecurityContextHolder was null");
            return authenticationManager.authenticate(authentication);
        }
        else
        {
            AuthChallenge challenge = authenticator.getChallenge();
            if (challenge != null)
            {
                challenge.challenge(facade);
            }
            return null;
        }
    }

}
