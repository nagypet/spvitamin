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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import hu.perit.spvitamin.core.exception.ApplicationSpecificException;
import hu.perit.spvitamin.core.exception.ServerExceptionProperties;
import hu.perit.spvitamin.spring.json.JsonSerializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
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
@EqualsAndHashCode
@JsonInclude(Include.NON_NULL)
public class RestExceptionResponse implements JsonSerializable
{

    private static boolean myExceptionEnabled = true;
    private static boolean myMessageEnabled = true;

    private Date timestamp;
    private int status;
    private Object error;
    private String path;
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

    public RestExceptionResponse(HttpStatus status, Throwable ex, String path)
    {
        this.timestamp = new Date();
        this.status = status.value();
        this.path = path;
        if (myExceptionEnabled)
        {
            this.exception = new ServerExceptionProperties(ex);
        }
        else if (myMessageEnabled)
        {
            this.message = ex.getMessage();
        }

        if (ex instanceof ConstraintViolationException)
        {
            //Get all errors
            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();
            List<String> errors = new ArrayList<>();
            for (ConstraintViolation<?> violation : violations)
            {
                errors.add(String.format("%s %s", violation.getPropertyPath(), violation.getMessage()));
            }
            this.error = errors;
        }
        else if (ex instanceof MethodArgumentNotValidException)
        {
            BindingResult bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            List<String> errors = new ArrayList<>();
            for (FieldError fieldError : fieldErrors)
            {
                errors.add(String.format("%s %s! Rejected value: '%s'", fieldError.getField(), fieldError.getDefaultMessage(),
                        getRejectedValueAsText(fieldError.getRejectedValue())));
            }
            this.error = errors;
        }
        else if (ex instanceof ApplicationSpecificException)
        {
            ApplicationSpecificException ase = (ApplicationSpecificException) ex;
            this.type = ase.getType().name();
        }
        else
        {
            this.error = status.getReasonPhrase();
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
