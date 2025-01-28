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

package hu.perit.spvitamin.spring.connectablecontext;

/**
 * @author Peter Nagy
 */


import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import hu.perit.spvitamin.core.connectablecontext.ConnectableContext;
import hu.perit.spvitamin.core.connectablecontext.SimpleConnectableContextHolder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public abstract class ConnectableContextHolder<T extends ConnectableContext> extends SimpleConnectableContextHolder<T>
{
    // Please @EnableScheduling! See SchedulerConfig.
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void disconnectTimer()
    {
        this.cleanup(true);
    }

    @PreDestroy
    @Override
    public void cleanup()
    {
        log.debug("cleanup on @PreDestroy");
        this.cleanup(false);
    }
}
