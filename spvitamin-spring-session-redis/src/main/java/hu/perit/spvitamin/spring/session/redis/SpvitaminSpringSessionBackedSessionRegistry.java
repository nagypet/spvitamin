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

import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.Constants;
import hu.perit.spvitamin.spring.security.utils.PrincipalUtils;
import hu.perit.spvitamin.spring.session.SessionUtils;
import hu.perit.spvitamin.spring.session.registry.AdvancedSessionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SpvitaminSpringSessionBackedSessionRegistry<S extends Session> extends SpringSessionBackedSessionRegistry<S> implements AdvancedSessionRegistry
{
    private final FindByIndexNameSessionRepository<S> sessionRepository;
    private final StringRedisTemplate redisTemplate;
    private static final Pattern SESSION_KEY_PATTERN = Pattern.compile("^spring:session:sessions:([0-9a-fA-F-]+)$");


    public SpvitaminSpringSessionBackedSessionRegistry(FindByIndexNameSessionRepository<S> sessionRepository, StringRedisTemplate redisTemplate)
    {
        super(sessionRepository);
        this.sessionRepository = sessionRepository;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public SessionInformation getSessionInformation(String sessionId)
    {
        S session = this.sessionRepository.findById(sessionId);
        if (session != null)
        {
            return new SpvitaminSpringSessionBackedSessionInformation<>(session, this.sessionRepository);
        }
        return null;
    }


    @Override
    public List<Object> getAllPrincipals()
    {
        Set<AuthenticatedUser> retval = new HashSet<>();
        Set<String> keys = redisTemplate.keys("spring:session:sessions:*");
        List<String> sessionKeys = extractSessionKeys(keys);
        for (String sessionKey : sessionKeys)
        {
            S session = this.sessionRepository.findById(sessionKey);
            if (session != null)
            {
                SecurityContext securityContext = session.getAttribute(Constants.SPRING_SECURITY_CONTEXT);
                if (securityContext != null)
                {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)
                    {
                        retval.add(authenticatedUser);
                    }
                }
            }
        }
        return retval.stream().map(i -> (Object) i).toList();
    }


    @Override
    public List<SessionInformation> getAllSessions(Object principal, boolean includeExpiredSessions)
    {
        Collection<S> sessions = this.sessionRepository.findByPrincipalName(name(principal)).values();
        List<SessionInformation> retval = new ArrayList<>();
        for (S session : sessions)
        {
            if (includeExpiredSessions
                    || !Boolean.TRUE.equals(session.getAttribute(SpvitaminSpringSessionBackedSessionInformation.EXPIRED_ATTR)))
            {
                SpvitaminSpringSessionBackedSessionInformation<S> sessionInformation = new SpvitaminSpringSessionBackedSessionInformation<>(session, this.sessionRepository);
                retval.add(sessionInformation);
            }
        }
        return retval;
    }


    private static List<String> extractSessionKeys(Set<String> keys)
    {
        if (keys == null || keys.isEmpty())
        {
            return Collections.emptyList();
        }
        return keys.stream()
                .map(SESSION_KEY_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(1)) // csak a <kulcs> rész kell
                .toList();
    }


    @Override
    public void setMaxInactiveInterval(String sessionId, Duration duration)
    {
        S session = sessionRepository.findById(sessionId);
        if (session != null)
        {
            session.setMaxInactiveInterval(duration);
            sessionRepository.save(session);
        }
    }


    @Override
    public boolean updatePrincipal(String sessionId, AuthenticatedUser principal, Runnable actionBeforeUpdate)
    {
        S session = sessionRepository.findById(sessionId);
        if (SessionUtils.isSessionAuthenticated(session))
        {
            Object sessionPrincipal = SessionUtils.resolvePrincipal(session);
            String sessionPrincipalName = PrincipalUtils.getPrincipalName(sessionPrincipal);
            if (!principalNamesEqual(sessionPrincipalName, principal))
            {
                log.debug("updatePrincipal: sessionId={}, {} => {}", sessionId, sessionPrincipalName, principal.getUsername());
                if (actionBeforeUpdate != null)
                {
                    actionBeforeUpdate.run();
                }
                session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, principal.getUsername());
                session.setAttribute(Constants.SPRING_SECURITY_CONTEXT, SecurityContextHolder.getContext());
                sessionRepository.save(session);
                return true;
            }
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
}
