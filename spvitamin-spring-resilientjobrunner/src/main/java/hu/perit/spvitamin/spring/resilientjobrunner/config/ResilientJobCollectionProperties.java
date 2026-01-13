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

package hu.perit.spvitamin.spring.resilientjobrunner.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties
@Validated
@Slf4j
public class ResilientJobCollectionProperties
{
    private Map<String, @Valid ResilientJobProperties> resilientJobs = new HashMap<>();


    public ResilientJobProperties get(String name)
    {
        return this.resilientJobs.get(name);
    }


    @PostConstruct
    private void init()
    {
        if (this.resilientJobs.isEmpty())
        {
            log.warn("Resilient jobs properties are missing!");
        }
    }
}
