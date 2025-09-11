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

package hu.perit.spvitamin.spring.session.local;

import hu.perit.spvitamin.spring.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ConditionalOnProperty(prefix = "spring.session", name = "store-type", havingValue = "none", matchIfMissing = true)
@Slf4j
@EnableSpringHttpSession
@RequiredArgsConstructor
@EnableScheduling
public class SessionRegistryConfigLocal
{
    private final JwtProperties jwtProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Bean
    public SessionRepository<MapSession> sessionRepository()
    {
        MapSessionRepository mapSessionRepository = new SpvitaminSessionRepository(new ConcurrentHashMap<>(), applicationEventPublisher);
        long expirationInMinutes = jwtProperties.getExpirationInMinutes() + 5;
        log.info("SpvitaminSessionRepository created, maxInactiveInterval: {} minutes", expirationInMinutes);
        mapSessionRepository.setDefaultMaxInactiveInterval(Duration.ofMinutes(expirationInMinutes));

        return mapSessionRepository;
    }


    @Bean
    public AdvancedSessionRegistry sessionRegistry(SessionRepository<?> sessionRepository)
    {
        log.info("SpvitaminSessionRegistry created");
        return new SpvitaminSessionRegistry(sessionRepository);
    }
}
