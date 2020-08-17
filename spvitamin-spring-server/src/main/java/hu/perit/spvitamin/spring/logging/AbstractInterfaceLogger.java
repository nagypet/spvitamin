/*
 * Copyright 2020-2020 the original author or authors.
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

import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

// Please start the Java VM with -Djava.net.preferIPv4Stack=true
/**
 * @author Peter Nagy
 */

@Log4j
public abstract class AbstractInterfaceLogger {

    private static final List<String> IP_HEADERS = Arrays.asList("X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR");
    private static final Set<String> headerNamesToHide = Set.of("password", "authorization");

    protected HttpServletRequest httpRequest;

    protected AbstractInterfaceLogger(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    protected abstract String getSubsystemName();

    public void traceIn(String processId, String user, Method method, Object... args) {
        log.debug(iptrace(processId, user, getEventLogId(method), method.getName(), getSubject(args), true));
    }

    public void traceIn(String processId, String user, String methodName, int eventId, Object... args) {
        log.debug(iptrace(processId, user, eventId, methodName, getSubject(args), true));
    }

    public void traceOut(String processId, String user, Method method, Throwable ex) {
        log.debug(iptrace(processId, user, getEventLogId(method), method.getName(), ex.toString(), false));
    }

    public void traceOut(String processId, String user, Method method) {
        log.debug(iptrace(processId, user, getEventLogId(method), method.getName(), "SUCCESS", false));
    }

    public void traceOut(String processId, String user, String methodName, int eventId, Throwable ex) {
        log.debug(iptrace(processId, user, eventId, methodName, ex.toString(), false));
    }

    public void traceOut(String processId, String user, String methodName, int eventId) {
        log.debug(iptrace(processId, user, eventId, methodName, "SUCCESS", false));
    }

    private int getEventLogId(Method method) {
        Objects.requireNonNull(method);
        EventLogId annotation = method.getAnnotation(EventLogId.class);
        if (annotation != null) {
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


    protected String iptrace(String processID, String user, int eventID, String eventText, String subject, boolean isDirectionIn) {
        this.logRequestHeaders();

        String eventLogString = String.format("%s | %s | %s | user: %s | host: %s | system: %s | eventId: %d | event: %s | %s ",
                isDirectionIn ? ">>>" : "<<<",
                this.getClientIpAddr(),
                StringUtils.defaultIfBlank(processID, "null"),
                StringUtils.defaultIfBlank(user, "null"),
                StringUtils.defaultString(this.getHostName(), "null"),
                StringUtils.defaultIfBlank(this.getSubsystemName(), "null"),
                eventID,
                StringUtils.defaultIfBlank(eventText, "null"),
                StringUtils.defaultIfBlank(subject, "null"));

        // Place to forward the event log entry to a log server

        return eventLogString;
    }


    public String getHostName() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null) return hostname;

        String computerName = System.getenv("COMPUTERNAME");
        if (computerName != null) return computerName;

        return null;
    }


    public String getClientIpAddr() {
        try {
            for (String ipHeader : IP_HEADERS) {
                String ip = this.httpRequest.getHeader(ipHeader);
                if (ip != null && !ip.isEmpty() && !ip.equalsIgnoreCase("unknown")) {
                    if (ipHeader.equalsIgnoreCase("X-Forwarded-For")) {
                        // X-Forwarded-For: <client>, <proxy1>, <proxy2>
                        // If a request goes through multiple proxies, the IP addresses of each successive proxy is listed.
                        // This means, the right-most IP address is the IP address of the most recent proxy and
                        // the left-most IP address is the IP address of the originating client.
                        String[] clients = ip.split(",");
                        if (clients != null && clients.length > 0) {
                            return clients[0];
                        }
                    }
                    return ip;
                }
            }
            return this.httpRequest.getRemoteAddr();
        }
        catch (IllegalStateException ex) {
            return "";
        }
    }

    private void logRequestHeaders() {
        try {
            Iterator<String> iterator = this.httpRequest.getHeaderNames().asIterator();
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP headers: ");
            while (iterator.hasNext()) {
                String headerName = iterator.next();
                sb.append(String.format("%s=%s;", headerName, this.hidePasswords(headerName)));
            }
            log.debug(sb);
        }
        catch (IllegalStateException ex) {
            log.warn("HTTP headers: HTTP request is empty!");
        }
    }

    private String hidePasswords(String headerName) {
        if (headerNamesToHide.stream().anyMatch(i -> i.equalsIgnoreCase(headerName))) {
            return "***";
        }
        else {
            return this.httpRequest.getHeader(headerName);
        }
    }

    public String getMyMethodName() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        return stackTraceElement.getMethodName();
    }
}
