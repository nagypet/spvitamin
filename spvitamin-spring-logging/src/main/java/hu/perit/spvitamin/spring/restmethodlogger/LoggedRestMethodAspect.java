/*
 * Copyright 2020-2023 the original author or authors.
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

package hu.perit.spvitamin.spring.restmethodlogger;

import hu.perit.spvitamin.core.took.Took;
import hu.perit.spvitamin.spring.httplogging.LoggingHelper;
import hu.perit.spvitamin.spring.logging.LogEvent;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggedRestMethodAspect
{
    private final AuthorizationService authorizationService;
    private final HttpServletRequest httpRequest;
    private final ApplicationEventPublisher publisher;


    @Pointcut("@annotation(hu.perit.spvitamin.spring.restmethodlogger.LoggedRestMethod)")
    public void loggedRestMethod()
    {
    }

    @Around("loggedRestMethod()")
    public Object logRestMethod(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        final MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        final Method method = signature.getMethod();
        List<String> argNames = Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.toList());

        LoggedRestMethod annotation = method.getAnnotation(LoggedRestMethod.class);

        AuthenticatedUser user = this.authorizationService.getAuthenticatedUser();

        try (Took took = new Took(method, !annotation.muted()))
        {
            callIn(null, annotation.subsystem(), user.getUsername(), method.getName(), annotation.eventId(), argNames, proceedingJoinPoint.getArgs(), annotation.muted());
            Object retval = proceedingJoinPoint.proceed();
            return retval;
        }
        catch (Throwable ex)
        {
            callOut(null, annotation.subsystem(), user.getUsername(), method.getName(), annotation.eventId(), ex, annotation.muted());
            throw ex;
        }
    }


    private void callIn(String traceId, String subsystem, String username, String method, int eventId, List<String> argNames, Object[] args, boolean muted)
    {
        LogEvent logEvent = createAndPublishLogEvent(traceId, subsystem, username, eventId, method, LoggingHelper.getSubject(argNames, args), true);
        if (!muted)
        {
            log.debug(logEvent.toString());
        }
    }


    private void callOut(String traceId, String subsystem, String username, String method, int eventId, Throwable ex, boolean muted)
    {
        LogEvent logEvent = createAndPublishLogEvent(traceId, subsystem, username, eventId, method, ex.toString(), false);
        if (!muted)
        {
            log.debug(logEvent.toString());
        }
    }


    protected LogEvent createAndPublishLogEvent(String traceId, String subsystem, String username, int eventID, String eventText, String subject, boolean isDirectionIn)
    {
        LogEvent logEvent = LogEvent.of(traceId, subsystem, LoggingHelper.getClientIpAddr(this.httpRequest), LoggingHelper.getHostName(), username, eventID, eventText, subject, isDirectionIn);

        // Place to forward the event log entry to a log server
        this.publisher.publishEvent(logEvent);

        return logEvent;
    }
}
