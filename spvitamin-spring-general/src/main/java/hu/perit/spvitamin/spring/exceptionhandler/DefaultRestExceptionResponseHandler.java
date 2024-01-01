/*
 * Copyright 2020-2024 the original author or authors.
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Optional;

/**
 * @author Peter Nagy
 */

@Slf4j
public class DefaultRestExceptionResponseHandler
{

    // Use the variant with traceId
    @Deprecated
    protected ResponseEntity<RestExceptionResponse> exceptionHandler(Exception ex, WebRequest request)
    {
        return exceptionHandler(ex, request, null);
    }


    protected ResponseEntity<RestExceptionResponse> exceptionHandler(Exception ex, WebRequest request, String traceId)
    {
        RestExceptionResponse exceptionResponse = getExceptionResponse(ex, request, traceId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
    }


    protected RestExceptionResponse getExceptionResponse(Exception ex, WebRequest request, String traceId)
    {
        String path = request != null ? request.getDescription(false) : "";

        Optional<RestExceptionResponse> restExceptionResponse = RestExceptionResponseFactory.of(ex, path, traceId);
        return restExceptionResponse.orElseGet(
            () -> new RestExceptionResponse(RestExceptionResponseFactory.getHttpStatusFromAnnotation(ex), ex, path, traceId));
    }
}