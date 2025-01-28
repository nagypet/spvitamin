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
import hu.perit.spvitamin.core.exception.*;
import hu.perit.spvitamin.spring.config.SpringContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Optional;

/**
 * @author Peter Nagy
 */

@Slf4j
@RequiredArgsConstructor
public class GenericRestExceptionResponseBuilder<T extends IRestExceptionResponse> implements RestExceptionResponseBuilder<T>
{
    protected final RestExceptionResponseSupplier<T> supplier;


    @Override
    public Optional<T> createResponse(Throwable ex, String path, String traceId)
    {
        ExceptionWrapper exception = ExceptionWrapper.of(ex);
        RestExceptionLogger exceptionLogger = getLogger();

        // ========== UNAUTHORIZED (401) ===============================================================================
        if (exception.instanceOf("org.springframework.security.core.AuthenticationException")
                || exception.instanceOf("io.jsonwebtoken.JwtException"))
        {
            exceptionLogger.log(path, ex, LogLevel.WARN);
            return Optional.of(this.supplier.get(HttpStatus.UNAUTHORIZED, ex, path, traceId));
        }

        // ========== FORBIDDEN (403) ==================================================================================
        else if (exception.instanceOf("org.springframework.security.access.AccessDeniedException"))
        {
            exceptionLogger.log(path, ex, LogLevel.WARN);
            return Optional.of(this.supplier.get(HttpStatus.FORBIDDEN, ex, path, traceId));
        }

        // ========== BAD_REQUEST (400) ================================================================================
        else if (exception.instanceOf(jakarta.validation.ValidationException.class)
                || exception.instanceOf(InputException.class))
        {
            exceptionLogger.log(path, ex, LogLevel.WARN);
            return Optional.of(this.supplier.get(HttpStatus.BAD_REQUEST, ex, path, traceId));
        }

        // ========== NOT_IMPLEMENTED (501) ============================================================================
        else if (exception.instanceOf(UnsupportedOperationException.class))
        {
            exceptionLogger.log(path, ex, LogLevel.ERROR);
            return Optional.of(this.supplier.get(HttpStatus.NOT_IMPLEMENTED, ex, path, traceId));
        }

        // ========== SERVICE_UNAVAILABLE (503) ========================================================================
        else if (exception.instanceOf("org.springframework.cloud.gateway.support.NotFoundException"))
        {
            // kiloggoljuk WARNING-gal
            exceptionLogger.log(path, ex, LogLevel.WARN);
            return Optional.of(this.supplier.get(HttpStatus.SERVICE_UNAVAILABLE, ex, path, traceId));
        }

        // ========== APPLICATION_SPECIFIC_EXCEPTION ===================================================================
        else if (exception.instanceOf(ApplicationRuntimeException.class) || exception.instanceOf(ApplicationException.class))
        {
            ApplicationSpecificException ase = (ApplicationSpecificException) ex;
            exceptionLogger.log(path, ex, ase.getType().getLevel());
            return Optional.of(this.supplier.get(HttpStatus.valueOf(ase.getType().getHttpStatusCode()), ex, path, traceId));
        }

        // ========== INTERNAL_SERVER_ERROR (hu.perit.spvitamin.spring.exception) ======================================
        else if (exception.instanceOf(NullPointerException.class) || exception.instanceOf(RuntimeException.class))
        {
            HttpStatus httpStatus = ExceptionResponseHelper.getHttpStatusFromAnnotation(ex);
            LogLevel logLevel = ExceptionResponseHelper.logLevelByHttpStatus(httpStatus);
            if (logLevel != null)
            {
                exceptionLogger.log(path, ex, logLevel);
            }
            return Optional.of(this.supplier.get(httpStatus, ex, path, traceId));
        }

        exceptionLogger.log(path, ex, LogLevel.ERROR);
        return Optional.empty();
    }


    @Override
    public T getResponseByExceptionAnnotation(Throwable ex, String path, String traceId)
    {
        return this.supplier.get(ExceptionResponseHelper.getHttpStatusFromAnnotation(ex), ex, path, traceId);
    }


    private static RestExceptionLogger getLogger()
    {
        try
        {
            return SpringContext.getBean(RestExceptionLogger.class);
        }
        catch (Exception e)
        {
            log.error(StackTracer.toString(e));
            throw e;
        }
    }
}
