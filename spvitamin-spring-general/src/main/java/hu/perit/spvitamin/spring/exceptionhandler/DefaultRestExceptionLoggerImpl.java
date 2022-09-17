package hu.perit.spvitamin.spring.exceptionhandler;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.exception.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This is a default implementation of RestExceptionLogger. It can be overridden on application level to implement
 * customized exception logging. Implement the RestExceptionLogger interface in your application if you need special handling.
 */

@Component
@Slf4j
@ConditionalOnMissingBean(RestExceptionLogger.class)
public class DefaultRestExceptionLoggerImpl implements DefaultRestExceptionLogger
{
    public static final String FORMAT = "path: '%s', ex: %s";

    @PostConstruct
    void init()
    {
        log.info(String.format("%s initialized", this.getClass().getName()));
    }

    @Override
    public void log(String path, Throwable ex, LogLevel level)
    {
        switch (level)
        {
            case DEBUG:
                log.debug(String.format(FORMAT, path, StackTracer.toString(ex)));
                break;
            case INFO:
                log.info(String.format(FORMAT, path, StackTracer.toString(ex)));
                break;
            case TRACE:
                log.trace(String.format(FORMAT, path, StackTracer.toString(ex)));
                break;
            case WARN:
                log.warn(String.format(FORMAT, path, StackTracer.toString(ex)));
                break;
            case ERROR:
            default:
                log.error(String.format(FORMAT, path, StackTracer.toString(ex)));
                break;
        }
    }
}
