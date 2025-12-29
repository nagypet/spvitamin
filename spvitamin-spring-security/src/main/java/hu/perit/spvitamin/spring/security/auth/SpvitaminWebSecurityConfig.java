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

package hu.perit.spvitamin.spring.security.auth;

import hu.perit.spvitamin.spring.config.AdminProperties;
import hu.perit.spvitamin.spring.config.EnableSpvitaminOAuth2Idp;
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.config.SwaggerProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.rest.api.AuthenticationRepositoryApi;
import hu.perit.spvitamin.spring.security.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * @author Peter Nagy
 */

@EnableWebSecurity
@Configuration
@Slf4j
public class SpvitaminWebSecurityConfig
{
    @ConditionalOnBean(annotation = EnableSpvitaminOAuth2Idp.class)
    @Bean
    @Order(1)
    public SecurityFilterChain configureOAuth2Idp1(HttpSecurity http) throws Exception
    {
        // http://localhost:8410/api/spvitamin/oauth2/token

        SimpleHttpSecurityBuilder.newInstance(http)
                .scope("/api/spvitamin/oauth2/token")
                .authorizeRequests(r -> r.anyRequest().permitAll())
                .createSession();

        return http.build();
    }


    @ConditionalOnBean(annotation = EnableSpvitaminOAuth2Idp.class)
    @Bean
    @Order(1)
    public SecurityFilterChain configureOAuth2Idp2(HttpSecurity http) throws Exception
    {
        // http://localhost:8410/api/spvitamin/oauth2/refresh
        // http://localhost:8410/.well-known/openid-configuration
        // http://localhost:8410/.well-known/jwks.json

        SimpleHttpSecurityBuilder.newInstance(http)
                .scope(
                        "/api/spvitamin/oauth2/refresh",
                        "/.well-known/**")
                .authorizeRequests(r -> r.anyRequest().permitAll());

        return http.build();
    }


    @ConditionalOnBean(annotation = EnableSpvitaminOAuth2Idp.class)
    @Bean
    @Order(1)
    public SecurityFilterChain configureOAuth2Idp3(HttpSecurity http) throws Exception
    {
        // http://localhost:8410/.well-known/userinfo

        SimpleHttpSecurityBuilder.newInstance(http)
                .scope("/api/spvitamin/oauth2/userinfo")
                .authorizeRequests(r -> r.anyRequest().authenticated())
                .jwtAuth();

        return http.build();
    }


    /*
     * ============== Config for the logout endpoint ===================================================================
     */
    @Bean
    @Order(997)
    @DependsOn(value = "serverProperties")
    public SecurityFilterChain configureLogoutRestEndpoint(HttpSecurity http) throws Exception
    {
        String serviceUrl = SysConfig.getServerProperties().getServiceUrl();
        final String logoutUrl = "/api/spvitamin/logout";
        log.info("logout URL: POST {}{}", serviceUrl, logoutUrl);

        SimpleHttpSecurityBuilder.newInstance(http)
                .scope(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, logoutUrl))
                .authorizeRequests(i -> i.anyRequest().permitAll())
                .logout(logoutUrl);

        return http.build();
    }


    /*
     * ============== Config for the admin endpoints ===================================================================
     */
    @Bean
    @Order(998)
    public SecurityFilterChain configureAdminRestEndpoints(HttpSecurity http) throws Exception
    {
        SimpleHttpSecurityBuilder.newInstance(http)
                .scope(
                        Constants.BASE_URL_ADMIN + "/**",
                        Constants.BASE_URL_KEYSTORE + "/**",
                        Constants.BASE_URL_TRUSTSTORE + "/**"
                )
                // /admin/** endpoints
                .authorizeRequests(i -> authAdminRestEndpoints(i))
                // any other requests
                .authorizeRequests(i -> i.anyRequest().authenticated())
                .ignorePersistedSecurity()
                .jwtAuth();

        return http.build();
    }


    private void authAdminRestEndpoints(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry)
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        // Admin REST API
        registry
                .requestMatchers(
                        PathPatternRequestMatcher.pathPattern(Constants.BASE_URL_ADMIN + "/version"),
                        PathPatternRequestMatcher.pathPattern(Constants.BASE_URL_ADMIN + "/csp_violations")
                ).permitAll();

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl adminUrls = registry.requestMatchers(
                PathPatternRequestMatcher.pathPattern(Constants.BASE_URL_ADMIN + "/settings"),
                PathPatternRequestMatcher.pathPattern(Constants.BASE_URL_ADMIN + "/shutdown"),
                PathPatternRequestMatcher.pathPattern(Constants.BASE_URL_KEYSTORE + "/**"),
                PathPatternRequestMatcher.pathPattern(Constants.BASE_URL_TRUSTSTORE + "/**")
        );

        if ("*".equals(securityProperties.getAdminEndpointsAccess()))
        {
            adminUrls.permitAll();
        }
        else
        {
            adminUrls.hasRole(securityProperties.getAdminEndpointsAccess());
        }
    }


    /*
     * ============== Config for the rest with persisted security ======================================================
     */
    @Bean
    @Order(999)
    public SecurityFilterChain configureAllOthers(HttpSecurity http) throws Exception
    {
        SimpleHttpSecurityBuilder.newInstance(http)
                .defaults()
                // h2 console uses frames
                .allowFrames()
                .authorizeRequests(i -> i.requestMatchers(AuthenticationRepositoryApi.BASE_URL + "/**").permitAll())
                .authorizeRequests(i -> authorizeSwagger(i))
                .authorizeRequests(i -> authorizeActuator(i))
                .authorizeRequests(i -> authorizeAdminGui(i))
                .authorizeRequests(i -> permitEndpoints(i))
                // any other requests
                .authorizeRequests(i -> i.anyRequest().authenticated());

        return http.build();
    }


    private void permitEndpoints(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry)
    {
        registry
                .requestMatchers(
                        // Login
                        PathPatternRequestMatcher.pathPattern("/login/**"),
                        // OAuth2
                        PathPatternRequestMatcher.pathPattern("/oauth2/authorization/*"),
                        PathPatternRequestMatcher.pathPattern("/api/spvitamin/oauth2/authorization"),
                        // error
                        PathPatternRequestMatcher.pathPattern("/error"),
                        // Logout endpoint
                        PathPatternRequestMatcher.pathPattern("/logout")
                ).permitAll()
        // H2 console must be enabled within the application
        //.requestMatchers(PathRequest.toH2Console()).permitAll()
        ;
    }


    private void authorizeAdminGui(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry)
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        AdminProperties adminProperties = SysConfig.getAdminProperties();

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl adminGuiUrls;
        if (adminProperties.getAdminGuiUrl().isBlank())
        {
            adminGuiUrls = registry
                    .requestMatchers(
                            // Admin GUI controller
                            PathPatternRequestMatcher.pathPattern("/"),
                            PathPatternRequestMatcher.pathPattern("/*.*"),
                            PathPatternRequestMatcher.pathPattern("/css/**"),
                            PathPatternRequestMatcher.pathPattern("/assets/**")
                    );
        }
        else
        {
            adminGuiUrls = registry
                    .requestMatchers(
                            // Admin GUI controller
                            PathPatternRequestMatcher.pathPattern("/"),
                            PathPatternRequestMatcher.pathPattern(String.format("%s/**", adminProperties.getAdminGuiUrl()))
                    );
        }

        if ("*".equals(securityProperties.getAdminGuiAccess()))
        {
            adminGuiUrls.permitAll();
        }
        else
        {
            adminGuiUrls.hasRole(securityProperties.getAdminGuiAccess());
        }
    }


    private void authorizeActuator(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry)
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        registry.requestMatchers(
                        // Health and Prometheus endpoint
                        PathPatternRequestMatcher.pathPattern("/actuator/health/**"),
                        PathPatternRequestMatcher.pathPattern("/actuator/prometheus"))
                .permitAll();

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl actuatorUrls = registry
                .requestMatchers(PathPatternRequestMatcher.pathPattern("/actuator/**"));

        if (securityProperties.isProductionMode())
        {
            actuatorUrls.denyAll();
        }

        if ("*".equals(securityProperties.getManagementEndpointsAccess()))
        {
            actuatorUrls.permitAll();
        }
        else
        {
            actuatorUrls.hasRole(securityProperties.getManagementEndpointsAccess());
        }
    }


    private void authorizeSwagger(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry)
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();
        SwaggerProperties swaggerProperties = SpringContext.getBean(SwaggerProperties.class);
        String swaggerUiPath = swaggerProperties.getSwaggerUi().getPath();
        String apiDocsPath = swaggerProperties.getApiDocs().getPath();
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl swaggerUrls = registry
                .requestMatchers(
                        // Swagger 3
                        PathPatternRequestMatcher.pathPattern(swaggerUiPath + "/**"),

                        // api-docs
                        PathPatternRequestMatcher.pathPattern(apiDocsPath + "/**"),
                        PathPatternRequestMatcher.pathPattern(apiDocsPath + ".yaml")
                );

        if ("*".equals(securityProperties.getSwaggerAccess()))
        {
            swaggerUrls.permitAll();
        }
        else
        {
            swaggerUrls.hasRole(securityProperties.getSwaggerAccess());
        }

    }
}
