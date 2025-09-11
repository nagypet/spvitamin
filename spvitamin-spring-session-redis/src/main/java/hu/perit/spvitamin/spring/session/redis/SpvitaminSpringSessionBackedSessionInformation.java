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

import hu.perit.spvitamin.spring.session.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import java.io.Serial;
import java.util.Date;

@Slf4j
class SpvitaminSpringSessionBackedSessionInformation<S extends Session> extends SessionInformation
{
    @Serial
    private static final long serialVersionUID = 3883329052076853578L;

    static final String EXPIRED_ATTR = SpvitaminSpringSessionBackedSessionInformation.class.getName() + ".EXPIRED";

    private final SessionRepository<S> sessionRepository;


    SpvitaminSpringSessionBackedSessionInformation(S session, SessionRepository<S> sessionRepository)
    {
        super(SessionUtils.resolvePrincipal(session), session.getId(), Date.from(session.getLastAccessedTime()));
        this.sessionRepository = sessionRepository;
        Boolean expired = session.getAttribute(EXPIRED_ATTR);
        if (Boolean.TRUE.equals(expired))
        {
            super.expireNow();
        }
    }


    @Override
    public void expireNow()
    {
        log.debug("Expiring session " + getSessionId() + " for user '" + getPrincipal()
                + "', presumably because maximum allowed concurrent " + "sessions was exceeded");
        super.expireNow();
        S session = this.sessionRepository.findById(getSessionId());
        if (session != null)
        {
            session.setAttribute(EXPIRED_ATTR, Boolean.TRUE);
            this.sessionRepository.save(session);
        }
        else
        {
            log.info("Could not find Session with id " + getSessionId() + " to mark as expired");
        }
    }

}
