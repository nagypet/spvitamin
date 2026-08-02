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
import hu.perit.spvitamin.spring.security.utils.PrincipalUtils;
import hu.perit.spvitamin.spring.session.registry.AdvancedSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionMetricProvider
{
    private final AdvancedSessionRegistry sessionRegistry;


    public Double getCountAllSessions()
    {
        Map<Object, List<SessionInformation>> sessionMap = new HashMap<>();

        sessionRegistry.getAllPrincipals().forEach(principal -> sessionMap.put(principal, sessionRegistry.getAllSessions(principal, false)));

        dump("All sessions", sessionMap);
        return (double) sessionMap.values().stream().mapToInt(List::size).sum();
    }


    public Double getCountNamedUserSessions()
    {
        Map<Object, List<SessionInformation>> sessionMap = new HashMap<>();

        sessionRegistry.getAllPrincipals().forEach(principal -> {
            if (!PrincipalUtils.isTechnicalUser(principal))
            {
                sessionMap.put(principal, sessionRegistry.getAllSessions(principal, false));
            }
        });

        dump("Named-user sessions", sessionMap);
        return (double) sessionMap.values().stream().mapToInt(List::size).sum();
    }


    private static void dump(String name, Map<Object, List<SessionInformation>> sessionMap)
    {
        StringJoiner sj = new StringJoiner("\n");
        for (Map.Entry<Object, List<SessionInformation>> entry : sessionMap.entrySet())
        {
            sj.add(MessageFormat.format("{0}: {1} session(s)", getPrincipalName(entry.getKey()), entry.getValue().size()));
        }
        log.debug("{}:\n{}", name, sj);
    }


    private static String getPrincipalName(Object principal)
    {
        return switch (principal)
        {
            case null -> "";
            case AuthenticatedUser authenticatedUser -> authenticatedUser.getUsername();
            case User user -> user.getUsername();
            default -> principal.toString();
        };
    }
}
