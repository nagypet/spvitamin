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
import hu.perit.spvitamin.core.thing.Thing;
import hu.perit.spvitamin.core.thing.Value;
import hu.perit.spvitamin.core.thing.ValueMap;
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SessionProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.rest.api.AuthApi;
import hu.perit.spvitamin.spring.security.SelectiveSessionSecurityContextRepositor;
import hu.perit.spvitamin.spring.security.auth.filter.Role2PermissionMapperFilter;
import hu.perit.spvitamin.spring.security.auth.filter.jwt.JwtAuthenticationFilter;
import hu.perit.spvitamin.spring.security.auth.filter.securitycontextremover.SecurityContextRemoverFilter;
import hu.perit.spvitamin.spring.security.auth.proxy.AuthorizationServerProxy;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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


    public SimpleHttpSecurityBuilder defaultCors()
    {
        http.cors(i -> i.configurationSource(corsConfigurationSource()));
        return this;
    }


    public SimpleHttpSecurityBuilder defaultCsrf()
    {
        http.csrf(i -> i.disable());
        return this;
    }


    public SimpleHttpSecurityBuilder exceptionHandler(AuthenticationEntryPoint authenticationEntryPoint,
                                                      AccessDeniedHandler accessDeniedHandler)
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


    public SimpleHttpSecurityBuilder allowAdditionalSecurityHeaders()
    {
        SecurityProperties securityProperties = SysConfig.getSecurityProperties();

        if (securityProperties.getAdditionalSecurityHeaders() != null)
        {
            for (Map.Entry<String, String> header : securityProperties.getAdditionalSecurityHeaders().entrySet())
            {
                /**
                 * security:
                 *   additional-security-headers:
                 *     Content-Security-Policy: "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; font-src 'self'; connect-src 'self'; object-src 'none'; frame-ancestors 'self'; base-uri 'self'; report-uri /api/spvitamin/admin/csp_violations;"
                 *     Permissions-Policy: "accelerometer=(), ambient-light-sensor=(), autoplay=(), battery=(), camera=(), display-capture=(), document-domain=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), usb=()"
                 */
                http.headers(i -> i.addHeaderWriter(new StaticHeadersWriter(header.getKey(), header.getValue())));
            }
        }

        return this;
    }


    // Creating a session only in case of Basic Authentication or OAuth2
    public SimpleHttpSecurityBuilder createSessionSelectively()
    {
        SessionAuthenticationStrategy authenticationStrategy = SpringContext.getBean(SessionAuthenticationStrategy.class);
        this.http
                .sessionManagement(i -> i
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .sessionAuthenticationStrategy(authenticationStrategy)
                )
                .securityContext(ctx -> ctx.securityContextRepository(new SelectiveSessionSecurityContextRepositor()));

        return this;
    }


    public SimpleHttpSecurityBuilder createSession()
    {
        SessionAuthenticationStrategy authenticationStrategy = SpringContext.getBean(SessionAuthenticationStrategy.class);
        this.http
                .sessionManagement(i -> i
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .sessionAuthenticationStrategy(authenticationStrategy)
                );

        return this;
    }


    public SimpleHttpSecurityBuilder basicAuth()
    {
        CustomAuthenticationEntryPoint authenticationEntryPoint = SpringContext.getBean(CustomAuthenticationEntryPoint.class);

        // Adding Authentication Manager
        AuthenticationManager authenticationManager = SpringContext.getBean(AuthenticationManager.class);
        this.http.authenticationManager(authenticationManager);

        this.http.httpBasic(i -> i.authenticationEntryPoint(authenticationEntryPoint));

        if (!isFilterAlreadyExists(Role2PermissionMapperFilter.class))
        {
            this.http.addFilterBefore(new Role2PermissionMapperFilter(), SessionManagementFilter.class);
        }

        return this;
    }


    public SimpleHttpSecurityBuilder jwtAuth()
    {
        // applying JWT Filter
        if (!isFilterAlreadyExists(JwtAuthenticationFilter.class))
        {
            this.http.addFilterAfter(new JwtAuthenticationFilter(), SecurityContextHolderFilter.class);
        }
        if (!isFilterAlreadyExists(Role2PermissionMapperFilter.class))
        {
            this.http.addFilterBefore(new Role2PermissionMapperFilter(), AuthorizationFilter.class);
        }

        return this;
    }


    public SimpleHttpSecurityBuilder apiKeyAuth(Filter apiKeyFilter)
    {
        // applying JWT Filter
        if (!isFilterAlreadyExists(apiKeyFilter.getClass()))
        {
            this.http.addFilterAfter(apiKeyFilter, SecurityContextHolderFilter.class);
        }
        if (!isFilterAlreadyExists(Role2PermissionMapperFilter.class))
        {
            this.http.addFilterBefore(new Role2PermissionMapperFilter(), AuthorizationFilter.class);
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


    public SimpleHttpSecurityBuilder logout(String logoutUrl) throws Exception
    {
        SecurityProperties securityProperties = SpringContext.getBean(SecurityProperties.class);
        SessionProperties sessionProperties = SpringContext.getBean(SessionProperties.class);
        SecurityProperties.AuthConfiguration auth = securityProperties.getAuth();
        List<String> cookieNames = new ArrayList<>();
        if (auth != null)
        {
            cookieNames.add(auth.getAccessTokenCookieName());
            cookieNames.add(auth.getRefreshTokenCookieName());
        }
        cookieNames.addAll(List.of("JSESSIONID", sessionProperties.getCookieName(), "IDP_SESSION"));
        this.http.logout(i -> i
                .logoutUrl(logoutUrl)
                .invalidateHttpSession(true)
                .deleteCookies(cookieNames.toArray(new String[0]))
                .clearAuthentication(true)
                .logoutSuccessHandler((request, response, authentication) -> {
                    try
                    {
                        AuthorizationServerProxy proxy = SpringContext.getBean(AuthorizationServerProxy.class);
                        proxy.logout(request);

                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        log.info("logout success (forwarded to auth-service)");
                    }
                    catch (NoSuchBeanDefinitionException e)
                    {
                        // Just do nothing
                    }
                    catch (Exception ex)
                    {
                        log.warn("logout forward to auth-service failed: {}", ex.getMessage());
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                })
        );

        return this;
    }


    public SimpleHttpSecurityBuilder allowFrames()
    {
        this.http.headers(i -> i.frameOptions(j -> j.sameOrigin()));
        return this;
    }


    public SimpleHttpSecurityBuilder h2() throws Exception
    {
        String h2ConsolePath = getH2ConsolePath().orElse(null);
        if (h2ConsolePath != null)
        {
            String serviceUrl = SysConfig.getServerProperties().getServiceUrl();
            log.info("H2 console available: {}{}", serviceUrl, h2ConsolePath);
            allowFrames();
            this.http.authorizeHttpRequests(r -> r.requestMatchers(PathRequest.toH2Console()).permitAll());
        }
        else
        {
            log.warn("*** H2 console is not available!");
        }
        return this;
    }


    public static Optional<String> getH2ConsolePath()
    {
        try
        {
            Class<?> h2PropsClass = Class.forName("org.springframework.boot.h2console.autoconfigure.H2ConsoleProperties");
            if (SpringContext.isBeanAvailable(h2PropsClass))
            {
                Thing h2ConsoleProperties = Thing.from(SpringContext.getBean(h2PropsClass));
                if (h2ConsoleProperties instanceof ValueMap valueMap)
                {
                    if (valueMap.getProperties().get("path") instanceof Value value)
                    {
                        return Optional.of(value.getValue().toString());
                    }
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            // H2ConsoleProperties class does not exist, so we don't care'
        }
        return Optional.empty();
    }


    public SimpleHttpSecurityBuilder ignorePersistedSecurity()
    {
        if (!isFilterAlreadyExists(SecurityContextRemoverFilter.class))
        {
            this.http.addFilterAfter(new SecurityContextRemoverFilter(), SecurityContextHolderFilter.class);
        }
        else
        {
            log.warn("{} has already been applied!", SecurityContextRemoverFilter.class.getName());
        }
        return this;
    }


    public SimpleHttpSecurityBuilder configureAuthorizatonServer() throws Exception
    {
        this
                .scope(AuthApi.BASE_URL_AUTHENTICATE + "/**")
                //.ignorePersistedSecurity() is commented out with reason: in case of OAuth2 authentication, the token is saved in the session
                .authorizeRequests(r -> r.anyRequest().authenticated())
                .basicAuth()
                .jwtAuth()
                .createSessionSelectively();

        return this;
    }


    public SimpleHttpSecurityBuilder configureResourceServer() throws Exception
    {
        this
                .scope(AuthApi.BASE_URL_AUTHENTICATE + "/**")
                .authorizeRequests(r -> r.anyRequest().permitAll())
                .createSessionSelectively();

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
        boolean productionMode = securityProperties.isProductionMode();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(getListFromArray(securityProperties.getAllowedOrigins(), productionMode));
        configuration.setAllowedHeaders(getListFromArray(securityProperties.getAllowedHeaders(), productionMode));
        configuration.setAllowedMethods(getListFromArray(securityProperties.getAllowedMethods(), productionMode));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    private static List<String> getListFromArray(String[] input, boolean productionMode)
    {
        if (input == null || input.length == 0)
        {
            if (productionMode)
            {
                log.warn("CORS configuration is missing in production mode — CORS requests will be blocked!");
                return List.of();
            }
            return List.of("*");
        }
        else
        {
            return List.of(input);
        }
    }
}
