package hu.perit.spvitamin.spring.resilientjobrunner;

import hu.perit.spvitamin.spring.resilientjobrunner.config.ResilientJobProperties;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractProcessor
{
    private final ProcessorType processorType;
    private final ResilientJobProperties properties;


    public abstract void processJob(AbstractResilientJobEntity entity) throws Exception;

    public abstract void onError(AbstractResilientJobEntity entity, Exception e);


    /**
     * If the exception is retryable, we will retry the job once again. For instance, FeignException.ServiceUnavailable.
     */
    public boolean isRetryableException(Throwable e)
    {
        return ExceptionHelper.isRetryableException(e, this.properties.getRetryableExceptions());
    }


    public boolean isItemRelatedException(Throwable e)
    {
        return ExceptionHelper.isItemRelatedException(e, this.properties.getItemRelatedExceptions());
    }
}
