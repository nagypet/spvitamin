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

import hu.perit.spvitamin.core.exception.ServerExceptionProperties;
import hu.perit.spvitamin.spring.config.ServerProperties;
import hu.perit.spvitamin.spring.exceptionhandler.RestExceptionResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServerErrorAutoConfiguration
{
    private final ServerProperties serverProperties;

    @PostConstruct
    private void setUp()
    {
        log.info("Configuring {} with: {}", RestExceptionResponse.class.getName(), this.serverProperties.getError());
        log.info("Configuring {} with: {}", ServerExceptionProperties.class.getName(), this.serverProperties.getError());

        ServerExceptionProperties.setStackTraceEnabled(this.serverProperties.getError().getIncludeStacktrace() == ServerProperties.ErrorProperties.IncludeAttribute.ALWAYS);
        RestExceptionResponse.setExceptionEnabled(this.serverProperties.getError().isIncludeException());
        RestExceptionResponse.setMessageEnabled(this.serverProperties.getError().getIncludeMessage() == ServerProperties.ErrorProperties.IncludeAttribute.ALWAYS);
    }
}
