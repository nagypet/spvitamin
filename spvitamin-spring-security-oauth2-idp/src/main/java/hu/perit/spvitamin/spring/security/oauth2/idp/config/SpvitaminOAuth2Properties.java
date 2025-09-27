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

package hu.perit.spvitamin.spring.security.oauth2.idp.config;

import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "spvitamin.oauth2")
@Slf4j
@Data
@DependsOn("securityProperties")
public class SpvitaminOAuth2Properties
{
    private final SecurityProperties securityProperties = SpringContext.getBean(SecurityProperties.class);

    private String issuer;
    private String basePath = "/api/spvitamin";
    private TokenTtl tokens = new TokenTtl();
    private List<ClientProps> clients;
    private List<String> grantTypes;


    @Data
    public static class TokenTtl
    {
        private Duration accessTtl = Duration.ofMinutes(5);
        private Duration refreshTtl = Duration.ofDays(30);
    }


    @Data
    public static class ClientProps
    {
        private String clientId;
        private String clientSecret;
        private Set<String> scopes;
        private Map<String, Object> extra;
        private boolean allowRefreshTokenInResponse = false;
    }


    @PostConstruct
    private void init()
    {
        log.debug("SpvitaminOAuth2Properties: {}", this);
        if (this.securityProperties.isProductionMode() && this.tokens.accessTtl.toMinutes() > 10)
        {
            log.warn("!!! WARNING !!!");
            log.warn("!!! The access token TTL is set to {} minutes in production mode. This is not recommended !!!", this.tokens.accessTtl.toMinutes());
            log.warn("!!! The access token TTL should not be greater then 10 minutes in production mode !!!");
        }
    }
}
