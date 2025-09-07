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

package hu.perit.spvitamin.spring.session.metrics;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.session.local.AdvancedSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.StringJoiner;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionMetricProvider
{
    private final AdvancedSessionRegistry sessionRegistry;


    public Double getCountAllSessions()
    {
        List<SessionInformation> allSessions = sessionRegistry.getAllPrincipals().stream()
                .flatMap(u -> sessionRegistry.getAllSessions(u, false).stream())
                .toList();

        dump("All sessions", allSessions);
        return (double) allSessions.size();
    }


    public Double getCountNamedUserSessions()
    {
        List<SessionInformation> allSessions = sessionRegistry.getAllPrincipals().stream()
                .flatMap(u -> sessionRegistry.getAllSessions(u, false).stream())
                .toList();

        List<SessionInformation> filteredSessions = allSessions.stream()
                .filter(i -> !i.isExpired() && !isTechnicalUser(i.getPrincipal()))
                .toList();

        dump("Named-user sessions", filteredSessions);
        return (double) filteredSessions.size();
    }


    private static void dump(String name, List<SessionInformation> sessions)
    {
        StringJoiner sj = new StringJoiner("\n");
        for (SessionInformation si : sessions)
        {
            sj.add(MessageFormat.format("{0}, {1}, expired: {2}, lastRequest: {3}", si.getSessionId(), getPrincipalName(si.getPrincipal()), si.isExpired(), si.getLastRequest()));
        }
        log.debug("{}:\n{}", name, sj);
    }


    private static String getPrincipalName(Object principal)
    {
        if (principal == null)
        {
            return "";
        }
        if (principal instanceof AuthenticatedUser authenticatedUser)
        {
            return authenticatedUser.getUsername();
        }
        if (principal instanceof User user)
        {
            return user.getUsername();
        }
        return principal.toString();
    }


    private static boolean isTechnicalUser(Object principal)
    {
        if (principal == null)
        {
            return false;
        }
        if (principal instanceof UserDetails userDetails)
        {
            return userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_TECHNICAL_USER"::equals);
        }
        return false;
    }
}
