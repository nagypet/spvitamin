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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import hu.perit.spvitamin.core.exception.ApplicationException;
import hu.perit.spvitamin.core.exception.ApplicationRuntimeException;
import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import hu.perit.spvitamin.core.exception.ServerExceptionProperties;
import hu.perit.spvitamin.spring.json.JsonSerializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * #know-how:custom-rest-error-response
 * <p>
 * {
 * "timestamp": "2020-07-23 12:47:01",
 * "status": 401,
 * "error": "Unauthorized",
 * "path": "uri=/authenticate",
 * "exception": {
 * "exceptionClass": "org.springframework.security.authentication.InsufficientAuthenticationException",
 * "superClasses": [
 * "org.springframework.security.authentication.InsufficientAuthenticationException",
 * "org.springframework.security.core.AuthenticationException",
 * "java.lang.RuntimeException",
 * "java.lang.Exception",
 * "java.lang.Throwable",
 * "java.lang.Object"
 * ],
 * "message": "Full authentication is required to access this resource",
 * "stackTrace": [
 * {
 * "classLoaderName": "app",
 * "moduleName": null,
 * "moduleVersion": null,
 * "methodName": "handleSpringSecurityException",
 * "fileName": "ExceptionTranslationFilter.java",
 * "lineNumber": 189,
 * "className": "org.springframework.security.web.access.ExceptionTranslationFilter",
 * "nativeMethod": false
 * },
 * ...
 *
 * @author Peter Nagy
 */

@NoArgsConstructor // Json!
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@JsonInclude(Include.NON_NULL)
public class RestExceptionResponse implements JsonSerializable, IRestExceptionResponse
{

    private static boolean myExceptionEnabled = true;
    private static boolean myMessageEnabled = true;

    private LocalDateTime timestamp;
    private int status;
    private Object error;
    private String path;
    private String traceId;
    private ServerExceptionProperties exception;
    private String type;
    private String message;


    public static void setExceptionEnabled(boolean exceptionEnabled)
    {
        myExceptionEnabled = exceptionEnabled;
    }


    public static void setMessageEnabled(boolean messageEnabled)
    {
        myMessageEnabled = messageEnabled;
    }


    // Use the variant with traceId
    @Deprecated
    public RestExceptionResponse(HttpStatusCode statusCode, Throwable ex, String path)
    {
        this(statusCode, ex, path, null);
    }


    public RestExceptionResponse(HttpStatusCode statusCode, Throwable ex, String path, String traceId)
    {
        this.timestamp = LocalDateTime.now();
        this.status = statusCode.value();
        this.path = path;
        this.traceId = traceId;
        if (myExceptionEnabled)
        {
            this.exception = new ServerExceptionProperties(ex);
        }
        else if (myMessageEnabled)
        {
            this.message = ex.getMessage();
        }

        ExceptionWrapper exception = ExceptionWrapper.of(ex);
        if (exception.causedBy("jakarta.validation.ConstraintViolationException"))
        {
            //Get all errors
            exception.getFromCauseChain(jakarta.validation.ConstraintViolationException.class).ifPresent(throwable -> {
                jakarta.validation.ConstraintViolationException cve = (jakarta.validation.ConstraintViolationException) throwable;
                Set<jakarta.validation.ConstraintViolation<?>> violations = cve.getConstraintViolations();
                List<String> errors = new ArrayList<>();
                for (jakarta.validation.ConstraintViolation<?> violation : violations)
                {
                    errors.add(String.format("%s %s", violation.getPropertyPath(), violation.getMessage()));
                }
                this.error = errors;
            });
        }
        else if (ex instanceof MethodArgumentNotValidException manve)
        {
            BindingResult bindingResult = manve.getBindingResult();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            List<String> errors = new ArrayList<>();
            for (FieldError fieldError : fieldErrors)
            {
                errors.add(String.format("%s %s! Rejected value: '%s'", fieldError.getField(), fieldError.getDefaultMessage(),
                    getRejectedValueAsText(fieldError.getRejectedValue())));
            }
            this.error = errors;
        }
        else
        {
            this.error = HttpStatus.valueOf(statusCode.value()).getReasonPhrase();
        }

        if (ex instanceof ApplicationException ae)
        {
            this.type = ae.getType().name();
        }
        else if (ex instanceof ApplicationRuntimeException are)
        {
            this.type = are.getType().name();
        }
    }


    private String getRejectedValueAsText(Object rejectedValue)
    {
        if (rejectedValue == null)
        {
            return "null";
        }

        return rejectedValue.toString();
    }
}
