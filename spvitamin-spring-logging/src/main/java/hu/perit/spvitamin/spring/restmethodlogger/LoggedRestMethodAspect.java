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

package hu.perit.spvitamin.spring.restmethodlogger;

import hu.perit.spvitamin.core.took.Took;
import hu.perit.spvitamin.spring.httplogging.LoggingHelper;
import hu.perit.spvitamin.spring.logging.Constants;
import hu.perit.spvitamin.spring.logging.LogEvent;
import hu.perit.spvitamin.spring.logging.RequestLogger;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

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

        // Creating list of parameters with names
        List<String> argNames = Arrays.stream(method.getParameters()).map(Parameter::getName).toList();
        Object[] args = proceedingJoinPoint.getArgs();
        Arguments arguments = Arguments.create(argNames, args);

        LoggedRestMethod annotation = method.getAnnotation(LoggedRestMethod.class);

        // Putting the externalTraceId into the ThreadContext for logging
        if (StringUtils.isNotBlank(annotation.externalTraceId()))
        {
            ThreadContext.put(Constants.EXTERNAL_TRACE_ID, arguments.getString(annotation.externalTraceId()));
        }

        // Putting arguments into the ThreadContext for logging
        for (String threadContextArgument : annotation.ctx())
        {
            Object arg = arguments.get(threadContextArgument);
            if (arg != null)
            {
                ThreadContext.put(threadContextArgument, arg.toString());
            }
        }

        String username = getUsername(annotation, arguments);

        try (Took took = new Took(method, !annotation.muted()))
        {
            callIn(arguments.getString(annotation.externalTraceId()), annotation.subsystem(), username, method.getName(), annotation.eventId(), arguments, annotation.muted());
            return proceedingJoinPoint.proceed();
        }
        catch (Throwable ex)
        {
            callOut(arguments.getString(annotation.externalTraceId()), annotation.subsystem(), username, method.getName(), annotation.eventId(), ex, annotation.muted());
            throw ex;
        }
    }


    private String getUsername(LoggedRestMethod annotation, Arguments arguments)
    {
        if (StringUtils.isNotBlank(annotation.user()))
        {
            return arguments.getString(annotation.user());
        }

        return this.authorizationService.getAuthenticatedUser().getUsername();
    }

    private void callIn(String traceId, String subsystem, String username, String method, int eventId, Arguments arguments, boolean muted)
    {
        LogEvent logEvent = createAndPublishLogEvent(traceId, subsystem, username, eventId, method, RequestLogger.toSubject(arguments), true, arguments);
        if (!muted)
        {
            log.debug(logEvent.toString());
        }
    }


    private void callOut(String traceId, String subsystem, String username, String method, int eventId, Throwable ex, boolean muted)
    {
        LogEvent logEvent = createAndPublishLogEvent(traceId, subsystem, username, eventId, method, ex.toString(), false, (Arguments) null);
        if (!muted)
        {
            log.debug(logEvent.toString());
        }
    }


    protected LogEvent createAndPublishLogEvent(String traceId, String subsystem, String username, int eventID, String eventText, String subject, boolean isDirectionIn, Arguments arguments)
    {
        LogEvent logEvent = LogEvent.of(this, traceId, subsystem, LoggingHelper.getClientIpAddr(this.httpRequest), LoggingHelper.getHostName(), username, eventID, eventText, subject, isDirectionIn, arguments);

        // Place to forward the event log entry to a log server
        this.publisher.publishEvent(logEvent);

        return logEvent;
    }
}
