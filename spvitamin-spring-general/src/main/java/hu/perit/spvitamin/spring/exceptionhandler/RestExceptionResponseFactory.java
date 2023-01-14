/*
 * Copyright 2020-2023 the original author or authors.
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
import hu.perit.spvitamin.core.exception.ApplicationException;
import hu.perit.spvitamin.core.exception.ApplicationRuntimeException;
import hu.perit.spvitamin.core.exception.ApplicationSpecificException;
import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import hu.perit.spvitamin.core.exception.InputException;
import hu.perit.spvitamin.core.exception.LogLevel;
import hu.perit.spvitamin.spring.config.SpringContext;
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
        DefaultRestExceptionLogger exceptionLogger = getLogger();

        // ========== UNAUTHORIZED (401) ===============================================================================
        if (exception.instanceOf("org.springframework.security.core.AuthenticationException")
                || exception.instanceOf("io.jsonwebtoken.JwtException"))
        {
            exceptionLogger.log(path, ex, LogLevel.WARN);
            return Optional.of(new RestExceptionResponse(HttpStatus.UNAUTHORIZED, ex, path));
        }

        // ========== FORBIDDEN (403) ==================================================================================
        else if (exception.instanceOf("org.springframework.security.access.AccessDeniedException"))
        {
            exceptionLogger.log(path, ex, LogLevel.WARN);
            return Optional.of(new RestExceptionResponse(HttpStatus.FORBIDDEN, ex, path));
        }

        // ========== BAD_REQUEST (400) ================================================================================
        else if (exception.instanceOf(ValidationException.class) || exception.instanceOf(InputException.class))
        {
            exceptionLogger.log(path, ex, LogLevel.WARN);
            return Optional.of(new RestExceptionResponse(HttpStatus.BAD_REQUEST, ex, path));
        }

        // ========== NOT_IMPLEMENTED (501) ============================================================================
        else if (exception.instanceOf(UnsupportedOperationException.class))
        {
            exceptionLogger.log(path, ex, LogLevel.ERROR);
            return Optional.of(new RestExceptionResponse(HttpStatus.NOT_IMPLEMENTED, ex, path));
        }

        // ========== SERVICE_UNAVAILABLE (503) ========================================================================
        else if (exception.instanceOf("org.springframework.cloud.gateway.support.NotFoundException"))
        {
            // kiloggoljuk WARNING-gal
            exceptionLogger.log(path, ex, LogLevel.WARN);
            return Optional.of(new RestExceptionResponse(HttpStatus.SERVICE_UNAVAILABLE, ex, path));
        }

        // ========== APPLICATION_SPECIFIC_EXCEPTION ===================================================================
        else if (exception.instanceOf(ApplicationRuntimeException.class) || exception.instanceOf(ApplicationException.class))
        {
            ApplicationSpecificException ase = (ApplicationSpecificException) ex;
            exceptionLogger.log(path, ex, ase.getType().getLevel());
            return Optional.of(new RestExceptionResponse(HttpStatus.valueOf(ase.getType().getHttpStatusCode()), ex, path));
        }

        // ========== INTERNAL_SERVER_ERROR (hu.perit.spvitamin.spring.exception) ======================================
        else if (exception.instanceOf(NullPointerException.class) || exception.instanceOf(RuntimeException.class))
        {
            HttpStatus httpStatus = getHttpStatusFromAnnotation(ex);
            LogLevel logLevel = logLevelByHttpStatus(path, httpStatus, ex);
            if (logLevel != null)
            {
                exceptionLogger.log(path, ex, logLevel);
            }
            return Optional.of(new RestExceptionResponse(httpStatus, ex, path));
        }

        exceptionLogger.log(path, ex, LogLevel.ERROR);
        return Optional.empty();
    }


    private static DefaultRestExceptionLogger getLogger()
    {
        try
        {
            return SpringContext.getBean(DefaultRestExceptionLogger.class);
        }
        catch (Exception e)
        {
            log.error(StackTracer.toString(e));
            throw e;
        }
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


    private static LogLevel logLevelByHttpStatus(String path, HttpStatus httpStatus, Throwable ex)
    {
        switch (classifyHttpStatus(httpStatus))
        {
            case WARNING:
                return LogLevel.WARN;
            case ERROR:
                return LogLevel.ERROR;
            case NONE:
            default:
                return null;
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
