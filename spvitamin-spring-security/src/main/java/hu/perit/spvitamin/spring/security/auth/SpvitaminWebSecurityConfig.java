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

import hu.perit.spvitamin.spring.config.*;
import hu.perit.spvitamin.spring.rest.api.AuthenticationRepositoryApi;
import hu.perit.spvitamin.spring.security.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * @author Peter Nagy
 */

@EnableWebSecurity
@Configuration
@Slf4j
public class SpvitaminWebSecurityConfig
{
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
                .scope(new AntPathRequestMatcher(logoutUrl, "POST"))
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
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector)
    {
        return new MvcRequestMatcher.Builder(introspector);
    }


    @Bean
    @Order(998)
    public SecurityFilterChain configureAdminRestEndpoints(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception
    {
        SimpleHttpSecurityBuilder.newInstance(http)
                .scope(
                        Constants.BASE_URL_ADMIN + "/**",
                        Constants.BASE_URL_KEYSTORE + "/**",
                        Constants.BASE_URL_TRUSTSTORE + "/**",
                        AuthenticationRepositoryApi.BASE_URL + "/**"
                )
                .authorizeRequests(i -> i.requestMatchers(AuthenticationRepositoryApi.BASE_URL + "/**").permitAll())
                // /admin/** endpoints
                .authorizeRequests(i -> authAdminRestEndpoints(i, mvc))
                // any other requests
                .authorizeRequests(i -> i.anyRequest().authenticated())
                .ignorePersistedSecurity()
                .jwtAuth();

        return http.build();
    }


    private void authAdminRestEndpoints(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry,
                                        MvcRequestMatcher.Builder mvc)
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        // Admin REST API
        registry
                .requestMatchers(
                        mvc.pattern(Constants.BASE_URL_ADMIN + "/version"),
                        mvc.pattern(Constants.BASE_URL_ADMIN + "/csp_violations")
                ).permitAll();

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl adminUrls = registry.requestMatchers(
                mvc.pattern(Constants.BASE_URL_ADMIN + "/settings"),
                mvc.pattern(Constants.BASE_URL_ADMIN + "/shutdown"),
                mvc.pattern(Constants.BASE_URL_KEYSTORE + "/**"),
                mvc.pattern(Constants.BASE_URL_TRUSTSTORE + "/**")
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
    public SecurityFilterChain configureAllOthers(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception
    {
        SimpleHttpSecurityBuilder.newInstance(http)
                .defaults()
                .logout()
                // h2 console uses frames
                .allowFrames()
                .authorizeRequests(i -> authorizeSwagger(i, mvc))
                .authorizeRequests(i -> authorizeActuator(i, mvc))
                .authorizeRequests(i -> authorizeAdminGui(i, mvc))
                .authorizeRequests(i -> permitEndpoints(i, mvc))
                // any other requests
                .authorizeRequests(i -> i.anyRequest().authenticated());

        return http.build();
    }


    private void permitEndpoints(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry,
                                 MvcRequestMatcher.Builder mvc)
    {
        registry
                .requestMatchers(
                        // Login
                        mvc.pattern("/login/**"),
                        // OAuth2
                        mvc.pattern("/oauth2/authorization/*"),
                        mvc.pattern("/api/spvitamin/oauth2/authorization"),
                        // error
                        mvc.pattern("/error"),
                        // Logout endpoint
                        mvc.pattern("/logout")
                ).permitAll()
        // H2 console must be enabled within the application
        //.requestMatchers(PathRequest.toH2Console()).permitAll()
        ;
    }


    private void authorizeAdminGui(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry,
                                   MvcRequestMatcher.Builder mvc)
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        AdminProperties adminProperties = SysConfig.getAdminProperties();

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl adminGuiUrls;
        if (adminProperties.getAdminGuiUrl().isBlank())
        {
            adminGuiUrls = registry
                    .requestMatchers(
                            // Admin GUI controller
                            mvc.pattern("/"),
                            mvc.pattern("/*.*"),
                            mvc.pattern("/css/**"),
                            mvc.pattern("/assets/**")
                    );
        }
        else
        {
            adminGuiUrls = registry
                    .requestMatchers(
                            // Admin GUI controller
                            mvc.pattern("/"),
                            mvc.pattern(String.format("%s/**", adminProperties.getAdminGuiUrl()))
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


    private void authorizeActuator(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry,
                                   MvcRequestMatcher.Builder mvc)
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        registry.requestMatchers(
                        // Health and Prometheus endpoint
                        mvc.pattern("/actuator/health/**"),
                        mvc.pattern("/actuator/prometheus"))
                .permitAll();

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl actuatorUrls = registry
                .requestMatchers(mvc.pattern("/actuator/**"));

        if ("*".equals(securityProperties.getManagementEndpointsAccess()))
        {
            actuatorUrls.permitAll();
        }
        else
        {
            actuatorUrls.hasRole(securityProperties.getManagementEndpointsAccess());
        }
    }


    private void authorizeSwagger(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry,
                                  MvcRequestMatcher.Builder mvc)
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();
        SwaggerProperties swaggerProperties = SpringContext.getBean(SwaggerProperties.class);
        String swaggerUiPath = swaggerProperties.getSwaggerUi().getPath();
        String apiDocsPath = swaggerProperties.getApiDocs().getPath();
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl swaggerUrls = registry
                .requestMatchers(
                        // Swagger 3
                        mvc.pattern(swaggerUiPath + "/**"),

                        // api-docs
                        mvc.pattern(apiDocsPath + "/**")
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
