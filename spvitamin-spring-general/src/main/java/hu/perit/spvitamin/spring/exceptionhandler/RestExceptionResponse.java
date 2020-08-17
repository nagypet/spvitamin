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
 *
 *{
 *     "timestamp": "2020-07-23 12:47:01",
 *     "status": 401,
 *     "error": "Unauthorized",
 *     "path": "uri=/authenticate",
 *     "exception": {
 *         "exceptionClass": "org.springframework.security.authentication.InsufficientAuthenticationException",
 *         "superClasses": [
 *             "org.springframework.security.authentication.InsufficientAuthenticationException",
 *             "org.springframework.security.core.AuthenticationException",
 *             "java.lang.RuntimeException",
 *             "java.lang.Exception",
 *             "java.lang.Throwable",
 *             "java.lang.Object"
 *         ],
 *         "message": "Full authentication is required to access this resource",
 *         "stackTrace": [
 *             {
 *                 "classLoaderName": "app",
 *                 "moduleName": null,
 *                 "moduleVersion": null,
 *                 "methodName": "handleSpringSecurityException",
 *                 "fileName": "ExceptionTranslationFilter.java",
 *                 "lineNumber": 189,
 *                 "className": "org.springframework.security.web.access.ExceptionTranslationFilter",
 *                 "nativeMethod": false
 *             },
 *             ...
 *
 * @author Peter Nagy
 */

@NoArgsConstructor // Json!
@Getter
@ToString
@EqualsAndHashCode
public class RestExceptionResponse implements JsonSerializable {

    private Date timestamp;
    private int status;
    private Object error;
    private String path;
    private ServerExceptionProperties exception;


    public RestExceptionResponse(HttpStatus status, Exception ex, String path) {
        this.timestamp = new Date();
        this.status = status.value();
        this.path = path;
        this.exception = new ServerExceptionProperties(ex);

        if (ex instanceof ConstraintViolationException) {
            //Get all errors
            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();
            List<String> errors = new ArrayList<>();
            for (ConstraintViolation<?> violation : violations) {
                errors.add(String.format("%s %s", violation.getPropertyPath(), violation.getMessage()));
            }
            this.error = errors;
        }
        else if (ex instanceof MethodArgumentNotValidException) {
            BindingResult bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            List<String> errors = new ArrayList<>();
            for (FieldError fieldError : fieldErrors) {
                errors.add(String.format("%s %s! Rejected value: '%s'", fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue().toString()));
            }
            this.error = errors;
        }
        else {
            this.error = status.getReasonPhrase();
        }
    }
}
