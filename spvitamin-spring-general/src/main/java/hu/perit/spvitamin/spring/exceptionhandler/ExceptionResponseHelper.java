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

import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import hu.perit.spvitamin.core.exception.LogLevel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.Annotation;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionResponseHelper
{
    public static HttpStatus getHttpStatusFromAnnotation(Throwable ex)
    {
        Annotation[] annotations = ExceptionWrapper.of(ex).getAnnotations();
        for (Annotation annotation : annotations)
        {
            if (annotation instanceof ResponseStatus responseStatus)
            {
                return responseStatus.value();
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


    public static LogLevel logLevelByHttpStatus(HttpStatus httpStatus)
    {
        return switch (classifyHttpStatus(httpStatus))
        {
            case WARNING -> LogLevel.WARN;
            case ERROR -> LogLevel.ERROR;
            default -> null;
        };
    }


    private enum LogStatus
    {
        NONE, WARNING, ERROR
    }


    private static LogStatus classifyHttpStatus(HttpStatus httpStatus)
    {
        if (httpStatus.value() < 400 || httpStatus == HttpStatus.CONFLICT || httpStatus == HttpStatus.NOT_FOUND)
        {
            // Intentionally no logging: business as usual
            return LogStatus.NONE;
        }
        else if (httpStatus.value() >= 500)
        {
            return LogStatus.ERROR;
        }
        // between 400 and 499
        else if (httpStatus == HttpStatus.REQUEST_TIMEOUT)
        {
            return LogStatus.ERROR;
        }

        return LogStatus.WARNING;
    }
}
