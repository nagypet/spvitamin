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

package hu.perit.spvitamin.spring.session.redis;

import hu.perit.spvitamin.spring.security.bruteforce.BruteForceAutoConfiguration;
import hu.perit.spvitamin.spring.security.bruteforce.BruteForceProperties;
import hu.perit.spvitamin.spring.security.bruteforce.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Auto-configuration that registers a Redis-backed {@link LoginAttemptService}
 * when Redis is available and brute force protection is enabled.
 */
@AutoConfiguration
@Slf4j
@AutoConfigureBefore(BruteForceAutoConfiguration.class)
@ConditionalOnProperty(name = "security.brute-force.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BruteForceProperties.class)
public class RedisBruteForceAutoConfiguration
{
    @Bean
    @ConditionalOnMissingBean(LoginAttemptService.class)
    @ConditionalOnProperty(prefix = "spring.session", name = "store-type", havingValue = "redis", matchIfMissing = false)
    public LoginAttemptService loginAttemptService(StringRedisTemplate redisTemplate, BruteForceProperties properties)
    {
        log.info("Initializing {}", RedisLoginAttemptService.class.getSimpleName());
        return new RedisLoginAttemptService(redisTemplate, properties);
    }
}
