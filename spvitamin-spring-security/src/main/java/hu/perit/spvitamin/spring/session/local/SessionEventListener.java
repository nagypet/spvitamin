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
import org.springframework.context.event.EventListener;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionEventListener
{
    private final JwtProperties jwtProperties;
    private final AdvancedSessionRegistry sessionRegistry;


    @EventListener
    public void onSessionCreated(SessionCreatedEvent event)
    {
        long expirationInMinutes = jwtProperties.getExpirationInMinutes() + 5;
        sessionRegistry.setMaxInactiveInterval(event.getSessionId(), Duration.ofMinutes(expirationInMinutes));

        log.debug("Session created: {}, maxInactiveInterval: {} minutes", event.getSessionId(), expirationInMinutes);
    }


    @EventListener
    public void onSessionDeleted(SessionDeletedEvent event)
    {
        log.debug("Session deleted: {}", event.getSessionId());
    }


    @EventListener
    public void onSessionExpired(SessionExpiredEvent event)
    {
        log.debug("Session expired: {}", event.getSessionId());
    }
}
