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

package hu.perit.spvitamin.spring.security.bruteforce;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for brute force login protection.
 * <p>
 * Active by default. Disable with {@code security.brute-force.enabled=false}.
 * <p>
 * To use a custom (e.g. Redis-backed) implementation, declare your own
 * {@link LoginAttemptService} bean — the default {@link InMemoryLoginAttemptService} will not be registered.
 */
@AutoConfiguration
@Slf4j
@ConditionalOnProperty(name = "security.brute-force.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BruteForceProperties.class)
public class BruteForceAutoConfiguration
{
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.session", name = "store-type", havingValue = "none", matchIfMissing = true)
    public LoginAttemptService loginAttemptService(BruteForceProperties properties)
    {
        log.info("Initializing {}", InMemoryLoginAttemptService.class.getSimpleName());
        return new InMemoryLoginAttemptService(properties);
    }


    @Bean
    public AuthenticationEventListener authenticationEventListener(LoginAttemptService loginAttemptService)
    {
        log.info("Initializing {}", AuthenticationEventListener.class.getSimpleName());
        return new AuthenticationEventListener(loginAttemptService);
    }
}
