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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import java.util.Optional;

/**
 * @author Peter Nagy
 */

@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler
{

    protected final ResponseEntity<Object> exceptionHandler(Exception ex, WebRequest request)
    {
        String path = request != null ? request.getDescription(false) : "";

        Optional<RestExceptionResponse> restExceptionResponse = RestExceptionResponseFactory.of(ex, path);
        if (restExceptionResponse.isPresent())
        {
            return new ResponseEntity<>(restExceptionResponse.get(), HttpStatus.valueOf(restExceptionResponse.get().getStatus()));
        }

        try
        {
            // TODO create the same response structure as in the RestExceptionResponse
            return super.handleException(ex, request);
        }
        catch (Exception e)
        {
            log.error(StackTracer.toString(e));
            RestExceptionResponse exceptionResponse = new RestExceptionResponse(RestExceptionResponseFactory.getHttpStatusFromAnnotation(ex), ex, path);
            return new ResponseEntity<>(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
        }
    }


    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status,
                                                             WebRequest request)
    {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status))
        {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        String path = request != null ? request.getDescription(false) : "";
        log.error(StackTracer.toString(ex));
        RestExceptionResponse exceptionResponse = new RestExceptionResponse(status, ex, path);
        return new ResponseEntity<>(exceptionResponse, headers, status);
    }
}
