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

package hu.perit.spvitamin.spring.auth;

import hu.perit.spvitamin.spring.auth.filter.jwt.JwtAuthenticationFilter;
import hu.perit.spvitamin.spring.auth.filter.securitycontextremover.SecurityContextRemoverFilter;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.rest.api.AdminApi;
import hu.perit.spvitamin.spring.rest.api.KeystoreApi;
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * #know-how:simple-httpsecurity-builder
 *
 * @author Peter Nagy
 */


public class SimpleHttpSecurityBuilder {

    private final HttpSecurity http;

    public static SimpleHttpSecurityBuilder newInstance(HttpSecurity http) {
        return new SimpleHttpSecurityBuilder(http);
    }

    private SimpleHttpSecurityBuilder(HttpSecurity http) {
        this.http = http;
    }

    public SimpleHttpSecurityBuilder defaultCors() throws Exception {
        http.cors().configurationSource(corsConfigurationSource());
        return this;
    }


    public SimpleHttpSecurityBuilder defaultCsrf() throws Exception {
        http.csrf().disable();
        return this;
    }

    public SimpleHttpSecurityBuilder exceptionHandler(AuthenticationEntryPoint exceptionHandler, AccessDeniedHandler accessDeniedHandler) throws Exception {
        http.exceptionHandling().authenticationEntryPoint(exceptionHandler).accessDeniedHandler(accessDeniedHandler);
        return this;
    }

    public SimpleHttpSecurityBuilder defaults() throws Exception {
        return this.defaultCors().defaultCsrf();
    }


    public SimpleHttpSecurityBuilder scope(String... antPatterns) {
        http
                .requestMatchers()
                .antMatchers(antPatterns);
        return this;
    }


    public void basicAuth() throws Exception {
        CustomAuthenticationEntryPoint authenticationEntryPoint = SpringContext.getBean(CustomAuthenticationEntryPoint.class);
        CustomAccessDeniedHandler accessDeniedHandler = SpringContext.getBean(CustomAccessDeniedHandler.class);

        this
                .defaults()
                .exceptionHandler(authenticationEntryPoint, accessDeniedHandler)
                .ignorePersistedSecurity().and()
                .authorizeRequests()
                .anyRequest().authenticated().and()
                .httpBasic().authenticationEntryPoint(authenticationEntryPoint);
    }


    public void basicAuth(String role) throws Exception {
        CustomAuthenticationEntryPoint authenticationEntryPoint = SpringContext.getBean(CustomAuthenticationEntryPoint.class);
        CustomAccessDeniedHandler accessDeniedHandler = SpringContext.getBean(CustomAccessDeniedHandler.class);

        this
                .defaults()
                .exceptionHandler(authenticationEntryPoint, accessDeniedHandler)
                .ignorePersistedSecurity().and()
                .authorizeRequests()
                .anyRequest().hasRole(role).and()
                .httpBasic().authenticationEntryPoint(authenticationEntryPoint);
    }


    public void jwtAuth() throws Exception {
        CustomAuthenticationEntryPoint authenticationEntryPoint = SpringContext.getBean(CustomAuthenticationEntryPoint.class);
        CustomAccessDeniedHandler accessDeniedHandler = SpringContext.getBean(CustomAccessDeniedHandler.class);

        this
                .defaults()
                .exceptionHandler(authenticationEntryPoint, accessDeniedHandler)
                .ignorePersistedSecurity().and()
                .authorizeRequests()
                .anyRequest().authenticated();

        // applying JWT Filter
        this.http.addFilterAfter(new JwtAuthenticationFilter(), SecurityContextPersistenceFilter.class);
    }


    public void permitAll() throws Exception {
        CustomAuthenticationEntryPoint authenticationEntryPoint = SpringContext.getBean(CustomAuthenticationEntryPoint.class);
        CustomAccessDeniedHandler accessDeniedHandler = SpringContext.getBean(CustomAccessDeniedHandler.class);

        this
                .defaults()
                .exceptionHandler(authenticationEntryPoint, accessDeniedHandler)
                .ignorePersistedSecurity().and()
                .authorizeRequests()
                .anyRequest().permitAll();
    }


    public HttpSecurity and() {
        return this.http;
    }


    public SimpleHttpSecurityBuilder authorizeAdminRestEndpoints() throws Exception {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry =
                http.authorizeRequests()
                        .antMatchers(
                                // Admin REST API
                                String.format("%s/version", AdminApi.BASE_URL_ADMIN),
                                String.format("%s/csp_violations", AdminApi.BASE_URL_ADMIN)
                        ).permitAll();

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl adminUrls = urlRegistry
                .antMatchers(
                        String.format("%s/settings", AdminApi.BASE_URL_ADMIN),
                        KeystoreApi.BASE_URL_KEYSTORE + "/**",
                        KeystoreApi.BASE_URL_TRUSTSTORE + "/**"
                );

        if ("*".equals(securityProperties.getAdminEndpointsAccess())) {
            adminUrls.permitAll();
        }
        else {
            adminUrls.hasRole(securityProperties.getAdminEndpointsAccess());
        }

        return this;
    }


    public SimpleHttpSecurityBuilder authorizeSwagger() throws Exception {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl swaggerUrls = http
                .authorizeRequests()
                .antMatchers(
                        //Swagger UI related endpoints and resources
                        "/swagger-ui.html",
                        "/swagger-ui.html/**",
                        "/swagger-resources/**",
                        "/api-docs/**",
                        "/v2/api-docs/**",
                        "/webjars/springfox-swagger-ui/**"
                );

        if ("*".equals(securityProperties.getSwaggerAccess())) {
            swaggerUrls.permitAll();
        }
        else {
            swaggerUrls.hasRole(securityProperties.getSwaggerAccess());
        }
        return this;
    }


    public SimpleHttpSecurityBuilder authorizeActuator() throws Exception {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl actuatorUrls = http
                .authorizeRequests()
                .antMatchers("/actuator/**");

        if ("*".equals(securityProperties.getManagementEndpointsAccess())) {
            actuatorUrls.permitAll();
        }
        else {
            actuatorUrls.hasRole(securityProperties.getManagementEndpointsAccess());
        }
        return this;
    }


    public SimpleHttpSecurityBuilder authorizeAdminGui() throws Exception {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl adminGuiUrls = http
                .authorizeRequests()
                .antMatchers(
                        // Admin GUI controller
                        "/",
                        "/*.*",
                        "/css/**",
                        "/assets/**"
                );

        if ("*".equals(securityProperties.getAdminGuiAccess())) {
            adminGuiUrls.permitAll();
        }
        else {
            adminGuiUrls.hasRole(securityProperties.getAdminGuiAccess());
        }
        return this;
    }


    public SimpleHttpSecurityBuilder logout() throws Exception {
        this.http.logout().invalidateHttpSession(true).deleteCookies("JSESSIONID").clearAuthentication(true).logoutSuccessUrl("/");
        return this;
    }


    public SimpleHttpSecurityBuilder allowFrames() throws Exception {
        this.http.headers().frameOptions().sameOrigin();
        return this;
    }


    public SimpleHttpSecurityBuilder ignorePersistedSecurity() {
        this.http.addFilterAfter(new SecurityContextRemoverFilter(), SecurityContextPersistenceFilter.class);
        return this;
    }


    private static CorsConfigurationSource corsConfigurationSource() {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(getListFromArray(securityProperties.getAllowedOrigins()));
        configuration.setAllowedHeaders(getListFromArray(securityProperties.getAllowedHeaders()));
        configuration.setAllowedMethods(getListFromArray(securityProperties.getAllowedMethods()));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static List<String> getListFromArray(String[] input) {
        if (input == null || input.length == 0) {
            return List.of("*");
        }
        else {
            return List.of(input);
        }
    }
}
