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

package hu.perit.spvitamin.spring.session.registry;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.info.RequestQuery;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.Constants;
import hu.perit.spvitamin.spring.security.utils.PrincipalUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.events.SessionDestroyedEvent;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * - Mi ez: Spring Security komponens, ami felhasználók és session-azonosítók közti kapcsolatot tartja nyilván.
 * - Mit csinál: nyilvántartja, hogy egy felhasználónak mely sessionjei aktívak, utolsó hozzáférés ideje, “expired” jelző; támogatja a konkurens munkamenet-korlátozást és a “kijelentetés” (kickout) funkciót.
 * - Tipikusan node-lokális, és a security réteg használja.
 */

@Slf4j
@RequiredArgsConstructor
public class SpvitaminSessionRegistry<S extends Session> extends SessionRegistryImpl implements AdvancedSessionRegistry
{
    private final SessionRepository<S> sessionRepository;


    @Override
    public void setMaxInactiveInterval(String sessionId, Duration duration)
    {
        HttpSession httpSession = RequestQuery.getSession();
        if (httpSession != null && httpSession.getMaxInactiveInterval() != duration.toSeconds())
        {
            log.debug("Updating session timeout for session '{}' to {} seconds", sessionId, duration.toSeconds());
            httpSession.setMaxInactiveInterval((int) duration.toSeconds());
        }
    }


    @Override
    public void refreshLastRequest(String sessionId)
    {
        Assert.hasText(sessionId, "SessionId required as per interface contract");
        SessionInformation info = getSessionInformation(sessionId);
        if (info != null)
        {
            log.debug("Session touched: {}", sessionId);
            info.refreshLastRequest();
        }
    }


    @Override
    public boolean updatePrincipal(String sessionId, AuthenticatedUser principal, Runnable actionBeforeUpdate)
    {
        try
        {
            S session = sessionRepository.findById(sessionId);
            SessionInformation sessionInformation = this.getSessionInformation(sessionId);
            if (session != null && sessionInformation != null && sessionInformation.getPrincipal() != null)
            {
                String sessionPrincipalName = PrincipalUtils.getPrincipalName(sessionInformation.getPrincipal());
                if (!principalNamesEqual(sessionPrincipalName, principal))
                {
                    log.debug("updatePrincipal: sessionId={}, {} => {}", sessionId, sessionPrincipalName, principal.getUsername());
                    if (actionBeforeUpdate != null)
                    {
                        actionBeforeUpdate.run();
                    }
                    // Refresh HttpSession
                    super.removeSessionInformation(sessionId);
                    registerNewSession(sessionId, principal);

                    // Refresh repository
                    session.setAttribute(Constants.SPRING_SECURITY_CONTEXT, SecurityContextHolder.getContext());
                    sessionRepository.save(session);

                    return true;
                }
            }
        }
        catch (Exception e)
        {
            log.error(StackTracer.toString(e));
        }
        return false;
    }


    private static boolean principalNamesEqual(String sessionPrincipalName, AuthenticatedUser authenticatedUser)
    {
        return Strings.CI.equalsAny(sessionPrincipalName, authenticatedUser.getUsername());
    }


    @Override
    public void removeSessionInformation(String sessionId)
    {
        super.removeSessionInformation(sessionId);
        sessionRepository.deleteById(sessionId);
    }


    @EventListener
    public void onSessionDestroyedEvent(SessionDestroyedEvent sessionDestroyedEvent)
    {
        String sessionId = sessionDestroyedEvent.getSessionId();
        super.removeSessionInformation(sessionId);
    }
}
