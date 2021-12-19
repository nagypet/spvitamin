/*
 * Copyright 2020-2021 the original author or authors.
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

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Data
public class LogEvent
{
    private LocalDateTime eventTime;
    private boolean directionInput;
    private String clientIpAddr;
    private String traceId;
    private String user;
    private String hostName;
    private String subsystemName;
    private int eventId;
    private String eventText;
    private String parameters;

    @Override
    public String toString()
    {
        return String.format("%s | %s | %s | user: %s | host: %s | system: %s | eventId: %d | event: %s | %s ",
                this.directionInput ? ">>>" : "<<<",
                this.clientIpAddr,
                StringUtils.defaultIfBlank(this.traceId, "null"),
                StringUtils.defaultIfBlank(this.user, "null"),
                StringUtils.defaultString(this.hostName, "null"),
                StringUtils.defaultIfBlank(this.subsystemName, "null"),
                this.eventId,
                StringUtils.defaultIfBlank(this.eventText, "null"),
                StringUtils.defaultIfBlank(this.parameters, "null"));
    }
}
