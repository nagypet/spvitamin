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
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
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
    
    /**
     * Example configuration
     * security.additional-security-headers.FP=Feature-Policy=accelerometer 'none'; ambient-light-sensor 'none'; autoplay 'none'; battery 'none'; camera 'none'; display-capture 'none'; document-domain 'none'; encrypted-media 'none'; geolocation 'none'; gyroscope 'none'; magnetometer 'none'; microphone 'none'; midi 'none'; payment 'none'; usb 'none'
     */
    private Map<String, String> additionalSecurityHeaders = new HashMap<>();
}
