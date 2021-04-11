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

package hu.perit.spvitamin.spring.security.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.security.Constants;
import hu.perit.spvitamin.spring.security.auth.filter.jwt.JwtAuthenticationFilter;

/**
 * @author Peter Nagy
 */

@EnableWebSecurity
public class SpvitaminWebSecurityConfig
{
    /*
     * ============== Config for the endpoints with JWT auth ===========================================================
     */
    @Configuration
    @Order(98)
    public static class WebSecurityConfigurationAdapterForJwt extends WebSecurityConfigurerAdapter
    {

        private final CustomAuthenticationEntryPoint authenticationEntryPoint;
        private final CustomAccessDeniedHandler accessDeniedHandler;

        public WebSecurityConfigurationAdapterForJwt(SecurityProperties securityProperties,
            CustomAuthenticationEntryPoint authenticationEntryPoint, CustomAccessDeniedHandler accessDeniedHandler)
        {
            this.authenticationEntryPoint = authenticationEntryPoint;
            this.accessDeniedHandler = accessDeniedHandler;
        }


        @Override
        protected void configure(HttpSecurity http) throws Exception
        {
            SimpleHttpSecurityBuilder.newInstance(http) //
                .scope( //
                    Constants.BASE_URL_ADMIN + "/**", //
                    Constants.BASE_URL_KEYSTORE + "/**", //
                    Constants.BASE_URL_TRUSTSTORE + "/**").defaults() //
                .exceptionHandler(this.authenticationEntryPoint, this.accessDeniedHandler) //
                .ignorePersistedSecurity()
                // /admin/** endpoints
                .authorizeAdminRestEndpoints().and()
                // any other requests
                .authorizeRequests().anyRequest().authenticated();

            // applying JWT Filter
            http.addFilterAfter(new JwtAuthenticationFilter(), SecurityContextPersistenceFilter.class);
        }
    }

    /*
     * ============== Config for the rest ==============================================================================
     */
    @Configuration(proxyBeanMethods = false)
    @Order(99)
    public class DefaultWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter
    {

        private final CustomAuthenticationEntryPoint authenticationEntryPoint;
        private final CustomAccessDeniedHandler accessDeniedHandler;

        public DefaultWebSecurityConfigurationAdapter(CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler)
        {
            this.authenticationEntryPoint = authenticationEntryPoint;
            this.accessDeniedHandler = accessDeniedHandler;
        }


        @Override
        protected void configure(HttpSecurity http) throws Exception
        {
            SimpleHttpSecurityBuilder.newInstance(http).defaults().logout()
                // h2 console uses frames
                .allowFrames() //
                .authorizeSwagger() //
                .authorizeActuator() //
                .authorizeAdminGui().and() //
                .authorizeRequests() //
                .antMatchers(
                    // H2
                    "/h2/**",

                    // Health endpoint
                    "/actuator/health", "/actuator/prometheus",

                    // Logout endpoint
                    "/logout").permitAll()
                // any other requests
                .anyRequest().authenticated();
        }
    }
}
