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

package hu.perit.spvitamin.spring.session;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.Constants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.Session;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionUtils
{
    public static boolean isSessionAuthenticated(Session session)
    {
        if (session == null)
        {
            return false;
        }
        SecurityContext securityContext = session.getAttribute(Constants.SPRING_SECURITY_CONTEXT);
        return securityContext != null && securityContext.getAuthentication() != null;
    }


    /**
     * Tries to determine the principal from the given Session.
     *
     * @param session the session
     * @return the principal, or "" if it couldn't be determined
     */
    public static Object resolvePrincipal(Session session)
    {
        if (session == null)
        {
            return "";
        }
        SecurityContext securityContext = session.getAttribute(Constants.SPRING_SECURITY_CONTEXT);
        if (securityContext != null && securityContext.getAuthentication() != null)
        {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (principal instanceof AuthenticatedUser authenticatedUser)
            {
                return authenticatedUser;
            }
            return principal.toString();
        }
        return "";
    }
}
