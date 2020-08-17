/*
 * Copyright 2020-2020 the original author or authors.
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
import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import hu.perit.spvitamin.core.exception.InputException;
import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import javax.validation.ValidationException;
import java.lang.annotation.Annotation;

/**
 * @author Peter Nagy
 */

@Log4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    protected final ResponseEntity<Object> exceptionHandler(Exception ex, WebRequest request) {
        String path = request != null ? request.getDescription(false) : "";

        ExceptionWrapper exception = ExceptionWrapper.of(ex);

        // ========== UNAUTHORIZED =====================================================================================
        if (exception.instanceOf(AuthenticationException.class)
                || exception.instanceOf(JwtException.class)) {
            log.warn(StackTracer.toString(ex));
            RestExceptionResponse exceptionResponse = new RestExceptionResponse(HttpStatus.UNAUTHORIZED, ex, path);
            return new ResponseEntity<>(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
        }

        // ========== FORBIDDEN ========================================================================================
        else if (exception.instanceOf(AccessDeniedException.class)) {
            log.warn(StackTracer.toString(ex));
            RestExceptionResponse exceptionResponse = new RestExceptionResponse(HttpStatus.FORBIDDEN, ex, path);
            return new ResponseEntity<>(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
        }

        // ========== BAD_REQUEST ======================================================================================
        else if (exception.instanceOf(ValidationException.class) || exception.instanceOf(InputException.class)) {
            log.warn(StackTracer.toString(ex));
            RestExceptionResponse exceptionResponse = new RestExceptionResponse(HttpStatus.BAD_REQUEST, ex, path);
            return new ResponseEntity<>(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
        }

        // ========== NOT_IMPLEMENTED ==================================================================================
        else if (exception.instanceOf(UnsupportedOperationException.class)) {
            // kiloggoljuk WARNING-gal
            log.error(StackTracer.toString(ex));
            RestExceptionResponse exceptionResponse = new RestExceptionResponse(HttpStatus.NOT_IMPLEMENTED, ex, path);
            return new ResponseEntity<>(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
        }

        /*
        else if (exception.instanceOf(ServerSoapException.class))
        {
            log.error(StackTracer.toString(ex));
            RestExceptionResponse exceptionResponse = new RestExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, path);
            return new ResponseEntity(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
        }
        */

        // ========== INTERNAL_SERVER_ERROR ============================================================================
        else if (exception.instanceOf(NullPointerException.class) || exception.instanceOf(RuntimeException.class)) {
            HttpStatus httpStatus = getHttpStatusFromAnnotation(ex);
            this.logByHttpStatus(httpStatus, ex);
            RestExceptionResponse exceptionResponse = new RestExceptionResponse(httpStatus, ex, path);
            return new ResponseEntity<>(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
        }

        // ========== EVERYTHING ELSE ==================================================================================
        else {
            try {
                log.error(StackTracer.toString(ex));
                return super.handleException(ex, request);
            }
            catch (Exception e) {
                RestExceptionResponse exceptionResponse = new RestExceptionResponse(getHttpStatusFromAnnotation(ex), ex, path);
                return new ResponseEntity<>(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
            }
        }
    }


    private static HttpStatus getHttpStatusFromAnnotation(Throwable ex) {
        Annotation[] annotations = ExceptionWrapper.of(ex).getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof ResponseStatus) {
                return ((ResponseStatus) annotation).value();
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


    private void logByHttpStatus(HttpStatus httpStatus, Throwable ex) {
        switch (classifyHttpStatus(httpStatus)) {
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

    private enum LogStatus {
        NONE,
        WARNING,
        ERROR
    }

    private LogStatus classifyHttpStatus(HttpStatus httpStatus) {
        if (httpStatus.value() < 400
                || httpStatus == HttpStatus.CONFLICT
                || httpStatus == HttpStatus.NOT_FOUND
        ) {
            // Intentionally no logging: business as usual
            return LogStatus.NONE;
        }
        else if (httpStatus.value() >= 500) {
            return LogStatus.ERROR;
        }
        // between 400 and 499
        else if (httpStatus == HttpStatus.REQUEST_TIMEOUT) {
            return LogStatus.ERROR;
        }

        return LogStatus.WARNING;
    }


    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        String path = request != null ? request.getDescription(false) : "";
        RestExceptionResponse exceptionResponse = new RestExceptionResponse(status, ex, path);
        return new ResponseEntity<>(exceptionResponse, headers, status);
    }
}
