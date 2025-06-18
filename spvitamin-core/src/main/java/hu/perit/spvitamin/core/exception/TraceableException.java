/*
 * Copyright (c) 2025. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.core.exception;

import lombok.Getter;

@Getter
public class TraceableException extends RuntimeException
{
    protected final String traceId;

    public TraceableException(String message, String traceId)
    {
        super(message);
        this.traceId = traceId;
    }


    public TraceableException(String message, Throwable cause, String traceId)
    {
        super(message, cause);
        this.traceId = traceId;
    }


    public TraceableException(Throwable cause, String traceId)
    {
        super(cause);
        this.traceId = traceId;
    }
}
