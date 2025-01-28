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

package hu.perit.spvitamin.spring.exceptionhandler;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.exception.LogLevel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a default implementation of RestExceptionLogger. It can be overridden on application level to implement
 * customized exception logging. Implement the RestExceptionLogger interface in your application if you need special handling.
 */

@Slf4j
public class DefaultRestExceptionLoggerImpl implements RestExceptionLogger
{
    public static final String FORMAT = "path: '%s', ex: %s";


    @PostConstruct
    void init()
    {
        log.info(String.format("%s initialized", this.getClass().getName()));
    }


    @Override
    public void log(String path, Throwable ex, LogLevel level)
    {
        if ("uri=/favicon.ico".equalsIgnoreCase(path))
        {
            return;
        }

        switch (level)
        {
            case DEBUG -> log.debug(String.format(FORMAT, path, StackTracer.toString(ex)));
            case INFO -> log.info(String.format(FORMAT, path, StackTracer.toString(ex)));
            case TRACE -> log.trace(String.format(FORMAT, path, StackTracer.toString(ex)));
            case WARN -> log.warn(String.format(FORMAT, path, StackTracer.toString(ex)));
            default -> log.error(String.format(FORMAT, path, StackTracer.toString(ex)));
        }
    }
}
