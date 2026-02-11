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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class ResilientJobProperties
{
    @NotNull
    private Long id;
    @NotNull
    private String processorClass;
    private int threadPoolSize = 10;
    private Duration pollingInterval = Duration.ofSeconds(10);
    private Duration retryTimeout = Duration.ofHours(24);
    private Duration processingTimeout = Duration.ofMinutes(5);
    private Duration initialRetryDelay = Duration.ofSeconds(5);
    private Duration maxRetryDelay = Duration.ofMinutes(5);
    private String contextDecoratorTag = "batchId";
    @NotEmpty
    private List<String> retryableExceptions;
    @NotEmpty
    private List<String> itemRelatedExceptions;
}
