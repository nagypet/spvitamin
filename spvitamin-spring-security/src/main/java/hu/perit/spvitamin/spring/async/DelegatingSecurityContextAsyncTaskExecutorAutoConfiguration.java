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
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

/**
 * This bean delegates the SecurityContext to a thread which is annotated with @Async
 * See also hu.perit.spvitamin.spring.async.AsyncAutoConfiguration
 */

@Configuration
@Slf4j
@EnableConfigurationProperties(AsyncProperties.class)
@RequiredArgsConstructor
public class DelegatingSecurityContextAsyncTaskExecutorAutoConfiguration
{
    private final AsyncProperties asyncProperties;

    @Bean
    @Primary
    public DelegatingSecurityContextAsyncTaskExecutor taskExecutor(ThreadPoolTaskExecutor delegate)
    {
        return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
    }
}
