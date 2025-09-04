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
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionEventListener implements HttpSessionListener
{
    private final JwtProperties jwtProperties;
    private final AdvancedSessionRegistry sessionRegistry;


    @Override
    public void sessionCreated(HttpSessionEvent se)
    {
        long expirationInMinutes = jwtProperties.getExpirationInMinutes() + 5;
        sessionRegistry.setMaxInactiveInterval(se.getSession(), Duration.ofMinutes(expirationInMinutes));

        log.debug("Session created: {}, maxInactiveInterval: {} minutes", se.getSession().getId(), expirationInMinutes);
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent se)
    {
        log.debug("Session destroyed: {}", se.getSession().getId());
    }
}
