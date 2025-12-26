package hu.perit.spvitamin.spring.resilientjobrunner;

import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionHelper
{
    /**
     * An exception thrown during the download process may be related to a single document or to the whole batch.
     * Document related exceptions will be stored in the processing_log table but the execution of the batch may
     * continue. In contrast, non document related exceptions will cause an interrupt of the whole job.
     *
     * @param throwable
     * @return
     */
    public static boolean isItemRelatedException(Throwable throwable, List<String> itemRelatedExceptions)
    {
        ExceptionWrapper exception = ExceptionWrapper.of(throwable);

        // Checking doc-related exceptions
        if (itemRelatedExceptions != null)
        {
            for (String exceptionSpecification : itemRelatedExceptions)
            {
                if (exception.causedBy(getExceptionClass(exceptionSpecification), getExceptionMessage(exceptionSpecification)))
                {
                    log.info("'{}' is registered as item-related!", throwable.toString());
                    return true;
                }
            }
        }

        log.info("'{}' is not registered as item related!", throwable.toString());
        return false;
    }

    public static boolean isRetryableException(Throwable throwable, List<String> retryableExceptions)
    {
        ExceptionWrapper exception = ExceptionWrapper.of(throwable);

        if (retryableExceptions != null)
        {
            for (String exceptionSpecification : retryableExceptions)
            {
                if (exception.causedBy(getExceptionClass(exceptionSpecification), getExceptionMessage(exceptionSpecification)))
                {
                    log.info("'{}' is registered as retryable!", throwable.toString());
                    return true;
                }
            }
        }

        log.info("'{}' is not registered as retryable!", throwable.toString());
        return false;
    }


    // java.net.SocketTimeoutException: read timed out
    protected static String getExceptionClass(String exceptionSpecification)
    {
        int indexOfColon = exceptionSpecification.indexOf(':');
        if (indexOfColon >= 0)
        {
            return exceptionSpecification.substring(0, indexOfColon);
        }

        return exceptionSpecification;
    }

    // java.net.SocketTimeoutException: read timed out
    protected static String getExceptionMessage(String exceptionSpecification)
    {
        int indexOfColon = exceptionSpecification.indexOf(':');
        if (indexOfColon >= 0)
        {
            return exceptionSpecification.substring(indexOfColon + 1).strip();
        }

        return "";
    }
}
