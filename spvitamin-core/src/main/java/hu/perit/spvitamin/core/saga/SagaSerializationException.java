/*
 * Copyright 2020-2025 the original author or authors.
 */

package hu.perit.spvitamin.core.saga;

public class SagaSerializationException extends RuntimeException
{
    public SagaSerializationException(Throwable cause)
    {
        super(cause);
    }
}
