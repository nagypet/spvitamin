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

package hu.perit.spvitamin.spring.logging;

import hu.perit.spvitamin.core.event.Event;
import hu.perit.spvitamin.spring.httplogging.LoggingHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Please start the Java VM with -Djava.net.preferIPv4Stack=true

/**
 * @author Peter Nagy
 */

/**
 * Use @LoggedRestMethod annotation instead
 */
@Slf4j
@Deprecated
public abstract class AbstractInterfaceLogger
{
    public static final Event<LogEvent> LOG_EVENT = new Event<>();

    protected HttpServletRequest httpRequest;

    protected AbstractInterfaceLogger(HttpServletRequest httpRequest)
    {
        this.httpRequest = httpRequest;
    }

    protected abstract String getSubsystemName();

    public void traceIn(String traceId, String user, Method method, Object... args)
    {
        log.debug(iptrace(traceId, user, getEventLogId(method), method.getName(), getSubject(args), true));
    }

    public void traceIn(String traceId, String user, String methodName, int eventId, Object... args)
    {
        log.debug(iptrace(traceId, user, eventId, methodName, getSubject(args), true));
    }

    public void traceOut(String traceId, String user, Method method, Throwable ex)
    {
        log.debug(iptrace(traceId, user, getEventLogId(method), method.getName(), ex.toString(), false));
    }

    public void traceOut(String traceId, String user, Method method)
    {
        log.debug(iptrace(traceId, user, getEventLogId(method), method.getName(), "SUCCESS", false));
    }

    public void traceOut(String traceId, String user, String methodName, int eventId, Throwable ex)
    {
        log.debug(iptrace(traceId, user, eventId, methodName, ex.toString(), false));
    }

    public void traceOut(String traceId, String user, String methodName, int eventId)
    {
        log.debug(iptrace(traceId, user, eventId, methodName, "SUCCESS", false));
    }

    private int getEventLogId(Method method)
    {
        Objects.requireNonNull(method);
        EventLogId annotation = method.getAnnotation(EventLogId.class);
        if (annotation != null)
        {
            return annotation.eventId();
        }

        log.warn(String.format("Method '%s' has no @EventLogId annotation!", method.getName()));
        return 0;
    }


    private static String getSubject(Object[] args)
    {
        if (args == null || args.length == 0)
        {
            return "";
        }
        else
        {
            return Arrays.stream(args).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(", "));
        }
    }


    protected String iptrace(String traceId, String user, int eventID, String eventText, String subject, boolean isDirectionIn)
    {
        return iptrace(traceId, user, eventID, eventText, subject, isDirectionIn, null);
    }


    protected String iptrace(String traceId, String user, int eventID, String eventText, String subject, boolean isDirectionIn, Map<String, Object> options)
    {
        ThreadContext.put(hu.perit.spvitamin.spring.logging.Constants.EXTERNAL_TRACE_ID, traceId);
        LogEvent logEvent = LogEvent.of(this, traceId, this.getSubsystemName(), getClientIpAddr(), LoggingHelper.getHostName(), user, eventID, eventText, subject, isDirectionIn, options);

        // Place to forward the event log entry to a log server
        LOG_EVENT.fire(logEvent);

        return logEvent.toString();
    }


    public String getHostName()
    {
        return LoggingHelper.getHostName();
    }


    public String getClientIpAddr()
    {
        return LoggingHelper.getClientIpAddr(this.httpRequest);
    }


    public String getMyMethodName()
    {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        return stackTraceElement.getMethodName();
    }
}
