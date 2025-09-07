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

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;

import java.time.Duration;
import java.util.Objects;

@Slf4j
public class SpvitaminSessionRegistry extends SessionRegistryImpl implements AdvancedSessionRegistry
{
    @Override
    public void setMaxInactiveInterval(HttpSession httpSession, Duration duration)
    {
        httpSession.setMaxInactiveInterval((int) duration.toSeconds());
    }


    @Override
    public void updatePrincipal(String sessionId, AuthenticatedUser principal)
    {
        try
        {
            SessionInformation sessionInformation = this.getSessionInformation(sessionId);
            if (sessionInformation == null || !Objects.equals(sessionInformation.getPrincipal(), principal))
            {
                log.debug("updatePrincipal: sessionId={}, principal={}", sessionId, principal);
                removeSessionInformation(sessionId);
                registerNewSession(sessionId, principal);
            }
        }
        catch (Exception e)
        {
            log.error(StackTracer.toString(e));
        }
    }
}
