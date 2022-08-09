/*
 * Copyright 2020-2021 the original author or authors.
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
import hu.perit.spvitamin.core.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ValidationException;
import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * @author Peter Nagy
 */

@Slf4j
public class RestExceptionResponseFactory
{
    public static Optional<RestExceptionResponse> of(Throwable ex, String path)
    {
        ExceptionWrapper exception = ExceptionWrapper.of(ex);

        // ========== UNAUTHORIZED (401) ===============================================================================
        if (exception.instanceOf("org.springframework.security.core.AuthenticationException")
                || exception.instanceOf("io.jsonwebtoken.JwtException"))
        {
            log.warn(StackTracer.toString(ex));
            return Optional.of(new RestExceptionResponse(HttpStatus.UNAUTHORIZED, ex, path));
        }

        // ========== FORBIDDEN (403) ==================================================================================
        else if (exception.instanceOf("org.springframework.security.access.AccessDeniedException"))
        {
            log.warn(StackTracer.toString(ex));
            return Optional.of(new RestExceptionResponse(HttpStatus.FORBIDDEN, ex, path));
        }

        // ========== BAD_REQUEST (400) ================================================================================
        else if (exception.instanceOf(ValidationException.class) || exception.instanceOf(InputException.class))
        {
            log.warn(StackTracer.toString(ex));
            return Optional.of(new RestExceptionResponse(HttpStatus.BAD_REQUEST, ex, path));
        }

        // ========== NOT_IMPLEMENTED (501) ============================================================================
        else if (exception.instanceOf(UnsupportedOperationException.class))
        {
            log.error(StackTracer.toString(ex));
            return Optional.of(new RestExceptionResponse(HttpStatus.NOT_IMPLEMENTED, ex, path));
        }

        // ========== SERVICE_UNAVAILABLE (503) ========================================================================
        else if (exception.instanceOf("org.springframework.cloud.gateway.support.NotFoundException"))
        {
            // kiloggoljuk WARNING-gal
            log.warn(StackTracer.toString(ex));
            return Optional.of(new RestExceptionResponse(HttpStatus.SERVICE_UNAVAILABLE, ex, path));
        }

        // ========== APPLICATION_SPECIFIC_EXCEPTION ===================================================================
        else if (exception.instanceOf(ApplicationRuntimeException.class) || exception.instanceOf(ApplicationException.class))
        {
            ApplicationSpecificException ase = (ApplicationSpecificException) ex;
            logByLogLevel(ex, ase.getType().getLevel());
            return Optional.of(new RestExceptionResponse(HttpStatus.valueOf(ase.getType().getHttpStatusCode()), ex, path));
        }

        // ========== INTERNAL_SERVER_ERROR (hu.perit.spvitamin.spring.exception) ======================================
        else if (exception.instanceOf(NullPointerException.class) || exception.instanceOf(RuntimeException.class))
        {
            HttpStatus httpStatus = getHttpStatusFromAnnotation(ex);
            logByHttpStatus(httpStatus, ex);
            return Optional.of(new RestExceptionResponse(httpStatus, ex, path));
        }

        log.error(StackTracer.toString(ex));
        return Optional.empty();
    }


    public static HttpStatus getHttpStatusFromAnnotation(Throwable ex)
    {
        Annotation[] annotations = ExceptionWrapper.of(ex).getAnnotations();
        for (Annotation annotation : annotations)
        {
            if (annotation instanceof ResponseStatus)
            {
                return ((ResponseStatus) annotation).value();
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


    private static void logByHttpStatus(HttpStatus httpStatus, Throwable ex)
    {
        switch (classifyHttpStatus(httpStatus))
        {
            case NONE:
                return;
            case WARNING:
                log.warn(StackTracer.toString(ex));
                break;
            case ERROR:
                log.error(StackTracer.toString(ex));
                break;
        }
    }


    private static void logByLogLevel(Throwable ex, LogLevel level)
    {
        switch (level)
        {
            case DEBUG:
                log.debug(StackTracer.toString(ex));
                break;
            case ERROR:
                log.error(StackTracer.toString(ex));
                break;
            case INFO:
                log.info(StackTracer.toString(ex));
                break;
            case TRACE:
                log.trace(StackTracer.toString(ex));
                break;
            case WARN:
                log.warn(StackTracer.toString(ex));
                break;
            default:
                log.error(StackTracer.toString(ex));
                break;
        }
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
