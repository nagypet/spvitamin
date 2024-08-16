/*
 * Copyright (c) 2024. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.spring.exceptionhandler;

import org.springframework.http.HttpStatus;

public class DefaultRestExceptionResponseFactory implements RestExceptionResponseFactory<RestExceptionResponse>
{
    @Override
    public RestExceptionResponse create(HttpStatus httpStatus, Throwable ex, String path, String traceId)
    {
        return new RestExceptionResponse(httpStatus, ex, path, traceId);
    }
}
