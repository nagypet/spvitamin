/*
 * Copyright (c) 2022. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "async")
public class AsyncProperties
{
    private static final int CORE_POOL_SIZE = 200;
    private static final int MAX_POOL_SIZE = 1000;
    private static final int QUEUE_CAPACITY = 100;

    private int corePoolSize = CORE_POOL_SIZE;
    private int maxPoolSize = MAX_POOL_SIZE;
    private int queueCapacity = QUEUE_CAPACITY;
    private String threadNamePrefix = "async-";
}
