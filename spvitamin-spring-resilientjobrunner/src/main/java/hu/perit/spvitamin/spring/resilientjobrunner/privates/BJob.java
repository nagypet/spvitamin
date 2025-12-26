package hu.perit.spvitamin.spring.resilientjobrunner.privates;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.batchprocessing.BatchJob;
import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import hu.perit.spvitamin.core.timeformatter.TimeFormatter;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.resilientjobrunner.AbstractProcessor;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobData;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobDataService;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus;
import hu.perit.spvitamin.spring.threadcontext.ThreadContextDecorator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Slf4j
class BJob extends BatchJob
{
    private final ResilientJobData jobData;
    private final AbstractProcessor processor;

    private final ResilientJobDataService resilientJobDataService = SpringContext.getBean(ResilientJobDataService.class);


    @Override
    protected Void execute() throws Exception
    {
        try (var ctx = new ThreadContextDecorator(processor.getProperties().getContextDecoratorTag(), BJobHelper.getBatchId(processor.getProcessorType(), jobData.getId())))
        {
            try
            {
                processEntity();
            }
            catch (Exception e)
            {
                ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(e);
                log.error(exceptionWrapper.toStringWithCauses());
                log.error("Exception. Retried {} times. Remaining time for retries: {}.",
                        jobData.getRetryCount(),
                        calculateRemainingTime(jobData.getCreationTimestamp()));

                boolean isItemRelated = this.processor.isItemRelatedException(e);
                boolean isRetryable = this.processor.isRetryableException(e);

                if (isRetryable || !isItemRelated)
                {
                    // retryable or unknown error => we will retry the job
                    this.resilientJobDataService.saveError(jobData.getId(), ResilientJobStatus.CREATED, e);

                    // If retryable and not item-related: this is most probably an infrastructure problem, the batch should be interrupted
                    if (isRetryable && !isItemRelated)
                    {
                        throw e;
                    }
                }
                else // item-related && not-retryable
                {
                    // job should be set in error, but the batch should continue
                    this.resilientJobDataService.saveError(jobData.getId(), ResilientJobStatus.ERROR, e);
                    onError(e);
                }
            }
        }

        return null;
    }


    String calculateRemainingTime(OffsetDateTime creationTimestamp)
    {
        long elapsedSeconds = Duration.between(creationTimestamp, OffsetDateTime.now()).toSeconds();
        long remainingSeconds = this.processor.getProperties().getRetryTimeout().getSeconds() - elapsedSeconds;
        return TimeFormatter.getHumanReadableDuration(remainingSeconds * 1000);
    }


    void processEntity() throws Exception
    {
        log.info("Processing entity");

        // Calling processor
        if (this.processor != null)
        {
            this.processor.processJob(this.jobData);
        }
        else
        {
            throw new RuntimeException("Job parameters could not be retrieved!");
        }

        log.info("Processed successfully");

        // Deleting the entity after successful processing
        this.resilientJobDataService.deleteById(this.jobData.getId());
    }


    void onError(Exception e)
    {
        try
        {
            this.processor.onError(jobData, e);
        }
        catch (Exception ex)
        {
            log.error("Error in onError method: {}", StackTracer.toString(ex));
        }
    }


    @Override
    public boolean isFatalException(Throwable ex)
    {
        return true;
    }
}
