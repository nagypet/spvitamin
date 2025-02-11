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

package hu.perit.spvitamin.spring.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author Peter Nagy
 */


@Data
@Component
@ConfigurationProperties(prefix = "spvitamin.feign")
@Slf4j
public class FeignProperties
{
    private String loggerLevel = "BASIC";

    @NestedConfigurationProperty
    private Retry retry = new Retry();

    @Data
    public static class Retry
    {
       private int maxAttempts = 3;
       private int maxPeriod = 2000;
       private int period = 500;
    }

    @PostConstruct
    private void postConstruct()
    {
        log.info(this.toString());
    }
}
