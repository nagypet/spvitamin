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

import hu.perit.spvitamin.spring.restmethodlogger.Arguments;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class LogEvent
{
    private Object source;
    private LocalDateTime eventTime;
    private boolean directionInput;
    private String clientIpAddr;
    private String externalTraceId;
    private String user;
    private String hostName;
    private String subsystemName;
    private int eventId;
    private String eventText;
    private String parameters;
    private Map<String, Object> options;
    private Arguments arguments;

    public static LogEvent of(Object source, String externalTraceId, String subsystem, String ipAddress, String hostname, String username, int eventID, String eventText, String subject, boolean isDirectionIn)
    {
        return of(source, externalTraceId, subsystem, ipAddress, hostname, username, eventID, eventText, subject, isDirectionIn, null, null);
    }

    public static LogEvent of(Object source, String externalTraceId, String subsystem, String ipAddress, String hostname, String username, int eventID, String eventText, String subject, boolean isDirectionIn, Arguments arguments)
    {
        return of(source, externalTraceId, subsystem, ipAddress, hostname, username, eventID, eventText, subject, isDirectionIn, null, arguments);
    }

    public static LogEvent of(Object source, String externalTraceId, String subsystem, String ipAddress, String hostname, String username, int eventID, String eventText, String subject, boolean isDirectionIn, Map<String, Object> options)
    {
        return of(source, externalTraceId, subsystem, ipAddress, hostname, username, eventID, eventText, subject, isDirectionIn, options, null);
    }

    public static LogEvent of(Object source, String externalTraceId, String subsystem, String ipAddress, String hostname, String username, int eventID, String eventText, String subject, boolean isDirectionIn, Map<String, Object> options, Arguments arguments)
    {
        LogEvent logEvent = new LogEvent();
        logEvent.setSource(source);
        logEvent.setEventTime(LocalDateTime.now());
        logEvent.setDirectionInput(isDirectionIn);
        logEvent.setClientIpAddr(ipAddress);
        logEvent.setExternalTraceId(externalTraceId);
        logEvent.setUser(username);
        logEvent.setHostName(hostname);
        logEvent.setSubsystemName(subsystem);
        logEvent.setEventId(eventID);
        logEvent.setEventText(eventText);
        logEvent.setParameters(subject);
        logEvent.setOptions(options);
        logEvent.setArguments(arguments);

        return logEvent;
    }

    @Override
    public String toString()
    {
        return String.format("%s | %s | %s | user: %s | host: %s | system: %s | eventId: %d | event: %s | %s ",
                this.directionInput ? ">>>" : "<<<",
                this.clientIpAddr,
                StringUtils.defaultIfBlank(this.externalTraceId, "null"),
                StringUtils.defaultIfBlank(this.user, "null"),
                StringUtils.defaultIfBlank(this.hostName, "null"),
                StringUtils.defaultIfBlank(this.subsystemName, "null"),
                this.eventId,
                StringUtils.defaultIfBlank(this.eventText, "null"),
                StringUtils.defaultIfBlank(this.parameters, "null"));
    }
}
