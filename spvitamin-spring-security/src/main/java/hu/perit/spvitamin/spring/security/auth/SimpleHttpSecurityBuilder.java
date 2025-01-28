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

import hu.perit.spvitamin.core.reflection.Property;
import hu.perit.spvitamin.core.reflection.ReflectionUtils;
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.security.auth.filter.Role2PermissionMapperFilter;
import hu.perit.spvitamin.spring.security.auth.filter.jwt.JwtAuthenticationFilter;
import hu.perit.spvitamin.spring.security.auth.filter.securitycontextremover.SecurityContextRemoverFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * #know-how:simple-httpsecurity-builder
 *
 * @author Peter Nagy
 */

@Slf4j
public class SimpleHttpSecurityBuilder
{

    private final HttpSecurity http;


    public static SimpleHttpSecurityBuilder newInstance(HttpSecurity http)
    {
        return new SimpleHttpSecurityBuilder(http);
    }


    private SimpleHttpSecurityBuilder(HttpSecurity http)
    {
        this.http = http;
    }


    public SimpleHttpSecurityBuilder defaultCors() throws Exception
    {
        http.cors(i -> i.configurationSource(corsConfigurationSource()));
        return this;
    }


    public SimpleHttpSecurityBuilder defaultCsrf() throws Exception
    {
        http.csrf(i -> i.disable());
        return this;
    }


    public SimpleHttpSecurityBuilder exceptionHandler(AuthenticationEntryPoint authenticationEntryPoint,
        AccessDeniedHandler accessDeniedHandler) throws Exception
    {
        http.exceptionHandling(i -> i.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler));
        return this;
    }


    public SimpleHttpSecurityBuilder defaults() throws Exception
    {
        CustomAuthenticationEntryPoint authenticationEntryPoint = SpringContext.getBean(CustomAuthenticationEntryPoint.class);
        CustomAccessDeniedHandler accessDeniedHandler = SpringContext.getBean(CustomAccessDeniedHandler.class);

        return this
            .defaultCors()
            .defaultCsrf()
            .allowAdditionalSecurityHeaders()
            .exceptionHandler(authenticationEntryPoint, accessDeniedHandler);
    }


    public SimpleHttpSecurityBuilder scope(String... antPatterns) throws Exception
    {
        defaults();
        this.http.securityMatcher(antPatterns);
        return this;
    }


    public SimpleHttpSecurityBuilder scope(RequestMatcher requestMatcher) throws Exception
    {
        defaults();
        this.http.securityMatcher(requestMatcher);
        return this;
    }


    public SimpleHttpSecurityBuilder allowAdditionalSecurityHeaders() throws Exception
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        if (securityProperties.getAdditionalSecurityHeaders() != null)
        {
            for (String header : securityProperties.getAdditionalSecurityHeaders().values())
            {
                // pl: X-Content-Security-Policy=default-src 'self'
                String[] headerParts = header.split("=");
                http.headers(i -> i.addHeaderWriter(new StaticHeadersWriter(headerParts[0], headerParts[1])));
            }
        }

        return this;
    }


    public SimpleHttpSecurityBuilder createSession() throws Exception
    {
        this.http.sessionManagement(i -> i.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

        return this;
    }


    public SimpleHttpSecurityBuilder basicAuth() throws Exception
    {
        CustomAuthenticationEntryPoint authenticationEntryPoint = SpringContext.getBean(CustomAuthenticationEntryPoint.class);

        this.http.httpBasic(i -> i.authenticationEntryPoint(authenticationEntryPoint));

        if (!isFilterAlreadyExists(Role2PermissionMapperFilter.class))
        {
            this.http.addFilterAfter(new Role2PermissionMapperFilter(), SessionManagementFilter.class);
        }

        return this;
    }


    public SimpleHttpSecurityBuilder jwtAuth() throws Exception
    {
        // applying JWT Filter
        if (!isFilterAlreadyExists(JwtAuthenticationFilter.class))
        {
            this.http.addFilterAfter(new JwtAuthenticationFilter(), SecurityContextPersistenceFilter.class);
        }
        if (!isFilterAlreadyExists(Role2PermissionMapperFilter.class))
        {
            this.http.addFilterAfter(new Role2PermissionMapperFilter(), SessionManagementFilter.class);
        }

        return this;
    }


    public SimpleHttpSecurityBuilder authorizeRequests(
        Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequestsCustomizer) throws Exception
    {
        this.http.authorizeHttpRequests(authorizeHttpRequestsCustomizer);

        return this;
    }


    public HttpSecurity and()
    {
        return this.http;
    }


    public SimpleHttpSecurityBuilder logout() throws Exception
    {
        this.http.logout(i -> i.invalidateHttpSession(true).deleteCookies("JSESSIONID").clearAuthentication(true));
        return this;
    }


    public SimpleHttpSecurityBuilder allowFrames() throws Exception
    {
        this.http.headers(i -> i.frameOptions(j -> j.sameOrigin()));
        return this;
    }


    public SimpleHttpSecurityBuilder h2() throws Exception
    {
        allowFrames();
        this.http.authorizeHttpRequests(r -> r.requestMatchers(PathRequest.toH2Console()).permitAll());
        return this;
    }


    public SimpleHttpSecurityBuilder ignorePersistedSecurity()
    {
        if (!isFilterAlreadyExists(SecurityContextRemoverFilter.class))
        {
            this.http.addFilterAfter(new SecurityContextRemoverFilter(), SecurityContextPersistenceFilter.class);
        }
        else
        {
            log.warn("{} has already been applied!", SecurityContextRemoverFilter.class.getName());
        }
        return this;
    }


    private boolean isFilterAlreadyExists(Class<?> filterClass)
    {
        List<Property> fields = ReflectionUtils.allPropertiesOf(HttpSecurity.class, true);
        Property filters = fields.stream().filter(i -> i.getName().equalsIgnoreCase("filters")).findAny().orElse(null);
        if (filters != null)
        {
            filters.setAccessible(true);
            try
            {
                List<?> f = (List) filters.get(this.http);
                return f.stream().anyMatch(i -> i.toString().contains(filterClass.getName()));
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                // Just do nothing
            }
        }
        return false;
    }


    public static CorsConfigurationSource corsConfigurationSource()
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(getListFromArray(securityProperties.getAllowedOrigins()));
        configuration.setAllowedHeaders(getListFromArray(securityProperties.getAllowedHeaders()));
        configuration.setAllowedMethods(getListFromArray(securityProperties.getAllowedMethods()));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    private static List<String> getListFromArray(String[] input)
    {
        if (input == null || input.length == 0)
        {
            return List.of("*");
        }
        else
        {
            return List.of(input);
        }
    }
}
