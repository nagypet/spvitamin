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

import hu.perit.spvitamin.spring.config.SessionProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import java.util.Comparator;
import java.util.List;

public class PerUserTypeConcurrentSessionControlStrategy extends ConcurrentSessionControlAuthenticationStrategy
{
    private final SessionProperties sessionProperties = SpringContext.getBean(SessionProperties.class);


    public PerUserTypeConcurrentSessionControlStrategy(AdvancedSessionRegistry sessionRegistry)
    {
        super(sessionRegistry);
        // default is unlimited
        super.setMaximumSessions(-1);
        // drop the oldest session
        super.setExceptionIfMaximumExceeded(false);
    }


    @Override
    protected int getMaximumSessionsForThisUser(Authentication authentication)
    {
        // Role-based differentiation of technical users
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return this.sessionProperties.getSessionLimit(roles);
    }


    @Override
    protected void allowableSessionsExceeded(List<SessionInformation> sessions, int allowableSessions, SessionRegistry registry) throws SessionAuthenticationException
    {
        // Determine the least recently used sessions and mark them for invalidation
        sessions.sort(Comparator.comparing(SessionInformation::getLastRequest));
        int maximumSessionsExceededBy = sessions.size() - allowableSessions + 1;
        List<SessionInformation> sessionsToBeExpired = sessions.subList(0, maximumSessionsExceededBy);
        for (SessionInformation session : sessionsToBeExpired)
        {
            session.expireNow();
            registry.removeSessionInformation(session.getSessionId());
        }
    }
}
