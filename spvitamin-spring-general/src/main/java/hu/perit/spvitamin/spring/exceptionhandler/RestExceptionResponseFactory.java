/*
 * Copyright (c) 2024. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.spring.exceptionhandler;

import org.springframework.http.HttpStatus;

public interface RestExceptionResponseFactory<T>
{
    // Use the variant with traceId
    @Deprecated
    default T create(HttpStatus httpStatus, Throwable ex, String path)
    {
        return create(httpStatus, ex, path, null);
    }

    T create(HttpStatus httpStatus, Throwable ex, String path, String traceId);
}
