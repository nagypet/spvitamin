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

package hu.perit.spvitamin.spring.security.auth;

import hu.perit.spvitamin.spring.config.AdminProperties;
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.config.SwaggerProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.security.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Peter Nagy
 */

@EnableWebSecurity
@Configuration
@Slf4j
public class SpvitaminWebSecurityConfig
{
    /*
     * ============== Config for the admin endpoints ===================================================================
     */
    @Bean
    @Order(997)
    public SecurityFilterChain configureLogoutRestEndpoint(HttpSecurity http) throws Exception
    {
        final String logoutUrl = "/api/spvitamin/logout";
        log.info("logout URL: {}", logoutUrl);

        SimpleHttpSecurityBuilder.newInstance(http)
                .scope(logoutUrl)
                .authorizeRequests(i -> i.anyRequest().permitAll()).and()
                .logout(logout -> logout
                        .logoutUrl(logoutUrl)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> log.info("logout success"))
                );

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
                        Constants.BASE_URL_ADMIN + "/shutdown",
                        Constants.BASE_URL_KEYSTORE + "/**",
                        Constants.BASE_URL_TRUSTSTORE + "/**")
                // /admin/** endpoints
                .authorizeRequests(this::authAdminRestEndpoints)
                // any other requests
                .authorizeRequests(i -> i.anyRequest().authenticated())
                .ignorePersistedSecurity()
                .jwtAuth();

        return http.build();
    }

    private void authAdminRestEndpoints(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry)
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        registry.requestMatchers(
                // Admin REST API
                String.format("%s/version", Constants.BASE_URL_ADMIN),
                String.format("%s/csp_violations", Constants.BASE_URL_ADMIN)).permitAll();

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl adminUrls = registry.requestMatchers(
                Constants.BASE_URL_ADMIN + "/shutdown",
                Constants.BASE_URL_KEYSTORE + "/**",
                Constants.BASE_URL_TRUSTSTORE + "/**");

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
                .logout()
                // h2 console uses frames
                .allowFrames()
                .authorizeRequests(this::authorizeSwagger)
                .authorizeRequests(this::authorizeActuator)
                .authorizeRequests(this::authorizeAdminGui)
                .authorizeRequests(this::permitEndpoints)
                // any other requests
                .authorizeRequests(i -> i.anyRequest().authenticated());

        return http.build();
    }

    private void permitEndpoints(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry)
    {
        registry.requestMatchers(
                // H2
                "/h2/**",

                // error
                "/error",

                // Logout endpoint
                "/logout").permitAll();
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
                            "/", "/*.*", "/css/**", "/assets/**");
        }
        else
        {
            adminGuiUrls = registry
                    .requestMatchers(
                            // Admin GUI controller
                            "/",
                            String.format("%s/**", adminProperties.getAdminGuiUrl()));
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
                        "/actuator/health/**",
                        "/actuator/prometheus")
                .permitAll();

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl actuatorUrls = registry
                .requestMatchers("/actuator/**");

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
                        swaggerUiPath + "/**",

                        // api-docs
                        apiDocsPath + "/**"
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
