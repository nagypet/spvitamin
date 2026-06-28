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
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts human-readable validation error messages from Spring and Jakarta validation exceptions.
 *
 * @author Peter Nagy
 */

@UtilityClass
public class ValidationErrorExtractor
{
    /**
     * Returns a list of formatted error strings for known validation exceptions,
     * or an empty list if the exception carries no extractable validation errors.
     */
    public static List<String> extractErrors(Throwable ex)
    {
        List<String> errors = new ArrayList<>();

        if (ex instanceof HandlerMethodValidationException hmve)
        {
            for (ParameterValidationResult result : hmve.getParameterValidationResults())
            {
                if (result instanceof ParameterErrors pe)
                {
                    // @RequestBody @Valid case — has FieldErrors with rejected values
                    for (FieldError fieldError : pe.getFieldErrors())
                    {
                        errors.add(String.format("%s %s! Rejected value: '%s'",
                                fieldError.getField(),
                                fieldError.getDefaultMessage(),
                                rejectedValueAsText(fieldError.getRejectedValue())));
                    }
                }
                else
                {
                    // @PathVariable / @RequestParam constraint case
                    String paramName = result.getMethodParameter().getParameterName();
                    for (MessageSourceResolvable error : result.getResolvableErrors())
                    {
                        errors.add(String.format("%s %s", paramName != null ? paramName : "", error.getDefaultMessage()));
                    }
                }
            }
        }
        else if (ex instanceof MethodArgumentNotValidException manve)
        {
            for (FieldError fieldError : manve.getBindingResult().getFieldErrors())
            {
                errors.add(String.format("%s %s! Rejected value: '%s'",
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        rejectedValueAsText(fieldError.getRejectedValue())));
            }
        }
        else
        {
            // ConstraintViolationException may be wrapped — search the cause chain
            ExceptionWrapper.of(ex)
                    .getFromCauseChain(jakarta.validation.ConstraintViolationException.class)
                    .ifPresent(cve -> {
                        for (jakarta.validation.ConstraintViolation<?> violation : cve.getConstraintViolations())
                        {
                            errors.add(String.format("%s %s", violation.getPropertyPath(), violation.getMessage()));
                        }
                    });
        }

        return errors;
    }


    private static String rejectedValueAsText(Object rejectedValue)
    {
        return rejectedValue != null ? rejectedValue.toString() : "null";
    }
}
