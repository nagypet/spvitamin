package hu.perit.spvitamin.spring.exceptionhandler;

import hu.perit.spvitamin.core.exception.LogLevel;

public interface DefaultRestExceptionLogger
{
    void log(String path, Throwable ex, LogLevel level);
}
