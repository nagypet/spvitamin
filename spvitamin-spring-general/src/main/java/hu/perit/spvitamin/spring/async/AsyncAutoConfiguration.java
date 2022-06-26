/*
 * Copyright (c) 2022. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.spring.async;

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
