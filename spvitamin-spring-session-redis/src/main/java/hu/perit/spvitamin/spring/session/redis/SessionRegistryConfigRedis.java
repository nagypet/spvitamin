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

import hu.perit.spvitamin.spring.session.local.AdvancedSessionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;

@Configuration
@EnableRedisIndexedHttpSession
@ConditionalOnProperty(prefix = "spring.session", name = "store-type", havingValue = "redis", matchIfMissing = false)
@Slf4j
public class SessionRegistryConfigRedis
{
    @Bean
    public AdvancedSessionRegistry sessionRegistry(
            FindByIndexNameSessionRepository<? extends Session> sessionRepository,
            StringRedisTemplate redisTemplate
    )
    {
        log.info("SpvitaminSpringSessionBackedSessionRegistry created");
        return new SpvitaminSpringSessionBackedSessionRegistry<>(sessionRepository, redisTemplate);
    }


    @Bean
    HttpSessionEventPublisher sessionEventPublisher()
    {
        return new HttpSessionEventPublisher();
    }
}
