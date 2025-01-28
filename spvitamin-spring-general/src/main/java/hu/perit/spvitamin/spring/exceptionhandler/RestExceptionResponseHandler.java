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

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

/**
 * @author Peter Nagy
 */

@Slf4j
public abstract class RestExceptionResponseHandler<T extends IRestExceptionResponse>
{
    // Override it if you need another factory
    protected abstract RestExceptionResponseBuilder<T> getBuilder();


    // Use the variant with traceId
    @Deprecated
    protected ResponseEntity<T> exceptionHandler(Exception ex, WebRequest request)
    {
        return exceptionHandler(ex, request, null);
    }


    protected ResponseEntity<T> exceptionHandler(Exception ex, WebRequest request, String traceId)
    {
        T exceptionResponse = getExceptionResponse(ex, request, traceId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.valueOf(exceptionResponse.getStatus()));
    }


    protected T getExceptionResponse(Exception ex, WebRequest request, String traceId)
    {
        String path = request != null ? request.getDescription(false) : "";
        return getBuilder().createResponse(ex, path, traceId).orElse(getBuilder().getResponseByExceptionAnnotation(ex, path, traceId));
    }
}
