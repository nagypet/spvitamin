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

package hu.perit.spvitamin.spring.session.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionIdGenerator;
import org.springframework.session.UuidSessionIdGenerator;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * - Mi ez: az alkalmazás HTTP munkameneteinek (HttpSession) általános tárolója/abstrakciója.
 * - Mit csinál: session létrehozás, betöltés, mentés, törlés; lejárat (TTL) kezelése; események küldése (létrejött, lejárt).
 * - Hol hasznos: megosztott/tartós tárolás (pl. Redis/JDBC) és klaszteres környezet.
 */

@Slf4j
public class SpvitaminSessionRepository extends MapSessionRepository
{
    private final Map<String, Session> sessions;
    private final ApplicationEventPublisher eventPublisher;
    private final SessionIdGenerator sessionIdGenerator = UuidSessionIdGenerator.getInstance();


    public SpvitaminSessionRepository(Map<String, Session> sessions, ApplicationEventPublisher eventPublisher)
    {
        super(sessions);
        this.sessions = sessions;
        this.eventPublisher = eventPublisher;
    }


    @Override
    public MapSession createSession()
    {
        MapSession session = super.createSession();
        eventPublisher.publishEvent(new SessionCreatedEvent(this, session));
        return session;
    }


    @Override
    public MapSession findById(String id)
    {
        Session saved = this.sessions.get(id);
        if (saved == null)
        {
            return null;
        }
        if (saved.isExpired())
        {
            return null;
        }
        MapSession result = new MapSession(saved);
        result.setSessionIdGenerator(this.sessionIdGenerator);
        return result;
    }


    @Override
    public void deleteById(String id)
    {
        MapSession session = findById(id);
        if (session != null)
        {
            super.deleteById(id);
            eventPublisher.publishEvent(new SessionDeletedEvent(this, session));
        }
    }


    @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 1)
    public void cleanupExpiredSessions()
    {
        for (Map.Entry<String, Session> entry : sessions.entrySet())
        {
            String id = entry.getKey();
            Session session = entry.getValue();
            if (session.isExpired())
            {
                super.deleteById(id);
                eventPublisher.publishEvent(new SessionExpiredEvent(this, session));
            }
        }
    }
}
