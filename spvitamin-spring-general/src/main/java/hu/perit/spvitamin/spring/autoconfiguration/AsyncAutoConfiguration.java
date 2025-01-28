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

package hu.perit.spvitamin.spring.autoconfiguration;

import hu.perit.spvitamin.spring.config.AsyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties(AsyncProperties.class)
@RequiredArgsConstructor
@Slf4j
public class AsyncAutoConfiguration
{
    private final AsyncProperties asyncProperties;

    @Bean
    @Primary
    public ThreadPoolTaskExecutor threadPoolTaskExecutor()
    {
        log.info("Configuring ThreadPoolTaskExecutor with: {}", this.asyncProperties);

        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.asyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(this.asyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(this.asyncProperties.getQueueCapacity());
        executor.setThreadNamePrefix(this.asyncProperties.getThreadNamePrefix());

        return executor;
    }
}
