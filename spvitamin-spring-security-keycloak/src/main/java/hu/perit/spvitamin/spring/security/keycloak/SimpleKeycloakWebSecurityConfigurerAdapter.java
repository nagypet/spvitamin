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

import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.security.auth.CustomAccessDeniedHandler;
import hu.perit.spvitamin.spring.security.auth.SimpleHttpSecurityBuilder;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Peter Nagy
 */

public class SimpleKeycloakWebSecurityConfigurerAdapter extends KeycloakWebSecurityConfigurerAdapter
{

    @Override
    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy()
    {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }


    public HttpSecurity scope(HttpSecurity http, String... antPatterns)
    {
        return http.requestMatchers().antMatchers(antPatterns).and();
    }


    @Override
    protected AuthenticationEntryPoint authenticationEntryPoint() throws Exception
    {
        return new CustomKeycloakAuthenticationEntryPoint(adapterDeploymentContext());
    }


    protected void configureKeycloak(HttpSecurity http) throws Exception
    {
        CustomAccessDeniedHandler accessDeniedHandler = SpringContext.getBean(CustomAccessDeniedHandler.class);

        SimpleHttpSecurityBuilder.newInstance(http) //
                .allowAdditionalSecurityHeaders();

        http //
                .csrf().requireCsrfProtectionMatcher(keycloakCsrfRequestMatcher()) //
                .and() //
                .cors().configurationSource(SimpleHttpSecurityBuilder.corsConfigurationSource())
                .and()
                //.sessionManagement() //
                //.sessionAuthenticationStrategy(sessionAuthenticationStrategy()) //
                //.and() //
                .addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class) //
                .addFilterBefore(keycloakAuthenticationProcessingFilter(), LogoutFilter.class) //
                .addFilterAfter(keycloakSecurityContextRequestFilter(), SecurityContextHolderAwareRequestFilter.class) //
                .addFilterAfter(keycloakAuthenticatedActionsRequestFilter(), KeycloakSecurityContextRequestFilter.class) //
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).accessDeniedHandler(accessDeniedHandler).and() //
                .logout() //
                .addLogoutHandler(keycloakLogoutHandler()) //
                .logoutUrl("/sso/logout").permitAll() //
                .logoutSuccessUrl("/");
    }


    @Bean
    @Override
    @ConditionalOnMissingBean(HttpSessionManager.class)
    protected HttpSessionManager httpSessionManager()
    {
        return new HttpSessionManager();
    }


    /*
     * By Default, the Spring Security Adapter looks for a keycloak.json configuration file. You can make sure it
     * looks at the configuration provided by the Spring Boot Adapter
     */
    @Bean
    public KeycloakConfigResolver keycloakConfigResolver()
    {
        return new KeycloakSpringBootConfigResolver();
    }


    @Override
    protected KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter() throws Exception
    {
        KeycloakAuthenticationProcessingFilter filter = new CustomKeycloakAuthenticationProcessingFilter(authenticationManagerBean());
        filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy());
        return filter;
    }


    // Needed because Spring Boot eagerly registers filter beans to the web application context. This prevents them being registered twice.
    @Bean
    public FilterRegistrationBean<KeycloakAuthenticationProcessingFilter> keycloakAuthenticationProcessingFilterRegistrationBean(
            KeycloakAuthenticationProcessingFilter filter)
    {
        FilterRegistrationBean<KeycloakAuthenticationProcessingFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    // Needed because Spring Boot eagerly registers filter beans to the web application context. This prevents them being registered twice.
    @Bean
    public FilterRegistrationBean<KeycloakPreAuthActionsFilter> keycloakPreAuthActionsFilterRegistrationBean(
            KeycloakPreAuthActionsFilter filter)
    {
        FilterRegistrationBean<KeycloakPreAuthActionsFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    // Needed because Spring Boot eagerly registers filter beans to the web application context. This prevents them being registered twice.
    @Bean
    public FilterRegistrationBean<KeycloakAuthenticatedActionsFilter> keycloakAuthenticatedActionsFilterBean(
            KeycloakAuthenticatedActionsFilter filter)
    {
        FilterRegistrationBean<KeycloakAuthenticatedActionsFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    // Needed because Spring Boot eagerly registers filter beans to the web application context. This prevents them being registered twice.
    @Bean
    public FilterRegistrationBean<KeycloakSecurityContextRequestFilter> keycloakSecurityContextRequestFilterBean(
            KeycloakSecurityContextRequestFilter filter)
    {
        FilterRegistrationBean<KeycloakSecurityContextRequestFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
