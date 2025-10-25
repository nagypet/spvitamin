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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author Peter Nagy
 */


@Data
@Component
@ConfigurationProperties(prefix = "jwt")
@Valid
@DependsOn(value = "SpvitaminSpringContext")
@Slf4j
public class JwtProperties
{
    private final SecurityProperties securityProperties = SpringContext.getBean(SecurityProperties.class);

    @NotNull
    private String privateKeyAlias;
    @NotNull
    @ConfigProperty(hidden = true)
    private String privateKeyEncryptedPassword;
    @NotNull
    private String publicKeyAlias;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    // Kept for backward compatibility
    private long expirationInMinutes = 5;
    private Duration expiration;
    private Duration refreshExpiration = Duration.ofHours(24);


    @PostConstruct
    private void init()
    {
        log.debug("JwtProperties: {}", this);
        if (securityProperties.getMode() == SecurityProperties.Mode.AUTHORIZATION_SERVER
                && securityProperties.isProductionMode()
                && getExpiration().compareTo(Duration.ofMinutes(10)) > 0
        )
        {
            log.warn("!!! WARNING !!!");
            log.warn("!!! The expiration is set to {} in production mode. This is not recommended !!!", getExpiration());
            log.warn("!!! The expiration should not be greater then 10 minutes in production mode !!!");
        }
    }


    public Duration getExpiration()
    {
        if (this.expiration != null)
        {
            return this.expiration;
        }
        return Duration.ofMinutes(this.expirationInMinutes);
    }
}
