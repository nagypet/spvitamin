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

package hu.perit.spvitamin.spring.time;

import hu.perit.spvitamin.spring.config.SystemProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

@Component
@Slf4j
public class TimeZoneConfig
{

    private final SystemProperties systemProperties;


    public TimeZoneConfig(SystemProperties systemProperties)
    {
        this.systemProperties = systemProperties;
    }


    @PostConstruct
    void started()
    {
        log.debug(String.format("Time zone set to: '%s'", this.systemProperties.getTimeZone()));
        TimeZone.setDefault(TimeZone.getTimeZone(this.systemProperties.getTimeZone()));
    }
}
