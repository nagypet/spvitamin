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
import org.apache.commons.lang3.Strings;

import java.util.List;

/**
 * This is a default implementation of RestExceptionLogger. It can be overridden on application level to implement
 * customized exception logging. Implement the RestExceptionLogger interface in your application if you need special handling.
 */

@Slf4j
public class DefaultRestExceptionLoggerImpl implements RestExceptionLogger
{
    public static final String FORMAT = "path: '%s', ex: %s";
    public static final String FORMAT_WITH_ERRORS = "path: '%s', ex: %s, errors: %s";


    @PostConstruct
    void init()
    {
        log.info(String.format("%s initialized", this.getClass().getName()));
    }


    @Override
    public void log(String path, Throwable ex, LogLevel level)
    {
        if (shouldIgnore(path))
        {
            return;
        }
        String message = buildMessage(path, ex);
        switch (level)
        {
            case DEBUG -> log.debug(message);
            case INFO -> log.info(message);
            case TRACE -> log.trace(message);
            case WARN -> log.warn(message);
            default -> log.error(message);
        }
    }


    private String buildMessage(String path, Throwable ex)
    {
        List<String> errors = ValidationErrorExtractor.extractErrors(ex);
        if (!errors.isEmpty())
        {
            return String.format(FORMAT_WITH_ERRORS, path, StackTracer.toString(ex), errors);
        }
        return String.format(FORMAT, path, StackTracer.toString(ex));
    }


    private boolean shouldIgnore(String path)
    {
        return Strings.CI.equalsAny(path,
                "uri=/favicon.ico",
                "uri=/.well-known/appspecific/com.chrome.devtools.json"
        ) || path != null && path.matches("uri=.*/sse/subscribe");
    }
}
