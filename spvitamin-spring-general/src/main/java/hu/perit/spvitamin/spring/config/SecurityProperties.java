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

package hu.perit.spvitamin.spring.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author Peter Nagy
 */


@Data
@Component
@ConfigurationProperties("security")
public class SecurityProperties
{
    private String[] allowedOrigins;
    private String[] allowedHeaders;
    private String[] allowedMethods;
    private String swaggerAccess = "*";
    private String managementEndpointsAccess = "*";
    private String adminGuiAccess = "*";
    private String adminEndpointsAccess = "*";

    @NestedConfigurationProperty
    private OAuth2Configuration oauth2;

    /**
     * Example configuration
     * security.additional-security-headers.FP=Feature-Policy=accelerometer 'none'; ambient-light-sensor 'none'; autoplay 'none'; battery 'none'; camera 'none'; display-capture 'none'; document-domain 'none'; encrypted-media 'none'; geolocation 'none'; gyroscope 'none'; magnetometer 'none'; microphone 'none'; midi 'none'; payment 'none'; usb 'none'
     */
    private Map<String, String> additionalSecurityHeaders = new HashMap<>();

    @Data
    public static class OAuth2Configuration
    {
        private Map<String, OAuth2Provider> providers = new HashMap<>();
        private Map<String, WellKnownEndpoints> wellKnownEndpoints = new HashMap<>();
    }

    @Data
    public static class OAuth2Provider
    {
        @NotNull
        private String displayName;
        private String issuerUri;
        @NotNull
        private String clientId;
        @NotNull
        private String clientSecret;
        private List<String> scopes = List.of("openid", "profile", "email");
    }

    @Data
    public static class WellKnownEndpoints
    {
        private String authorizationUri;
        private String tokenUri;
        private String jwkSetUri;
        private String userInfoUri;
        private String userNameAttributeName;
    }
}
