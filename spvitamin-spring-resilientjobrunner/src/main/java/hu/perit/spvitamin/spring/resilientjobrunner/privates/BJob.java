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

package hu.perit.spvitamin.spring.resilientjobrunner.privates;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.timeformatter.TimeFormatter;
import hu.perit.spvitamin.spring.batchprocessing.ContextAwareBatchJob;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.resilientjobrunner.AbstractProcessor;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobContext;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobEntityService;
import hu.perit.spvitamin.spring.threadcontext.ThreadContextDecorator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Slf4j
class BJob extends ContextAwareBatchJob
{
    private final AbstractResilientJobEntity entity;
    private final AbstractProcessor processor;

    private final ResilientJobEntityService<?> resilientJobEntityService = SpringContext.getBean(ResilientJobEntityService.class);


    @Override
    protected Void execute() throws Exception
    {
        try (var ctx = ThreadContextDecorator.with(processor.getProcessorType().getName(), BJobHelper.getTraceId(processor.getProcessorType(), entity.getId()));
             var jobCtx = ResilientJobContext.bind(entity.getOperationId()))
        {
            try
            {
                processEntity();
            }
            catch (Exception e)
            {
                log.error(StackTracer.toString(e));

                boolean isItemRelated = this.processor.isItemRelatedException(e);
                boolean isRetryable = this.processor.isRetryableException(e);

                if (isRetryable || !isItemRelated)
                {
                    // retryable or unknown error => we will retry the job
                    OffsetDateTime nextRetryTimestamp = calculateNextRetryTimestamp(entity.getRetryCount());
                    boolean canRetryAgain = shouldRetryAgain(nextRetryTimestamp);

                    if (canRetryAgain)
                    {
                        log.info("Job id: {} retried {} times. Remaining time for retries: {}. Next retry in {}.",
                                entity.getId(),
                                entity.getRetryCount(),
                                calculateRemainingTime(entity.getProcessingFirstStartedTimestamp()),
                                formatDurationUntil(nextRetryTimestamp)
                        );
                        this.resilientJobEntityService.saveError(
                                entity.getId(),
                                ResilientJobStatus.CREATED,
                                nextRetryTimestamp,
                                e
                        );

                        // If retryable and not item-related: this is most probably an infrastructure problem, the batch should be interrupted
                        if (isRetryable && !isItemRelated)
                        {
                            throw e;
                        }
                    }
                    else
                    {
                        log.info("Job id: {} retried {} times. Retry timeout reached.",
                                entity.getId(),
                                entity.getRetryCount()
                        );
                        // This is the final execution cycle, the error remained, there is no more retry
                        this.resilientJobEntityService.saveError(
                                entity.getId(),
                                ResilientJobStatus.ERROR,
                                null,
                                e
                        );
                        onError(e);
                    }
                }
                else // item-related && not-retryable
                {
                    log.info("Job id: {} will not be retried", entity.getId());
                    // job should be set in error, but the batch should continue
                    this.resilientJobEntityService.saveError(entity.getId(), ResilientJobStatus.ERROR, null, e);
                    onError(e);
                }
            }
        }

        return null;
    }


    private String formatDurationUntil(OffsetDateTime futureTimestamp)
    {
        long millis = Math.max(Duration.between(OffsetDateTime.now(), futureTimestamp).toMillis(), 0L);
        return TimeFormatter.getHumanReadableDuration(millis);
    }


    String calculateRemainingTime(OffsetDateTime creationTimestamp)
    {
        long elapsedSeconds = 0;
        if (creationTimestamp != null)
        {
            elapsedSeconds = Duration.between(creationTimestamp, OffsetDateTime.now()).toSeconds();
        }
        long remainingSeconds = Math.max(this.processor.getProperties().getRetryTimeout().getSeconds() - elapsedSeconds, 0);
        return TimeFormatter.getHumanReadableDuration(remainingSeconds * 1000);
    }


    void processEntity() throws Exception
    {
        log.info("Processor {} starting entity {}", this.processor.getClass().getSimpleName(), this.entity);

        // Calling processor
        if (this.processor != null)
        {
            this.processor.processJob(this.entity);
        }
        else
        {
            throw new RuntimeException("Job parameters could not be retrieved!");
        }

        log.info("Processor {} finished entity {}", this.processor.getClass().getSimpleName(), this.entity);

        // Deleting the entity after successful processing
        try
        {
            this.resilientJobEntityService.deleteById(this.entity.getId());
        }
        catch (Exception e)
        {
            // The job was processed successfully, but deletion failed (likely a transient DB issue).
            // The entity stays IN_PROGRESS and will eventually be reset by resetStuckInProgressEntities.
            // When that happens, processJob will run again - the processor must be idempotent (use operationId as idempotency key).
            log.error("Failed to delete job entity {} after successful processing: {}", entity.getId(), StackTracer.toString(e));
        }
    }


    void onError(Exception e)
    {
        try
        {
            this.processor.onError(entity, e);
        }
        catch (Exception ex)
        {
            log.error("Error in onError method: {}", StackTracer.toString(ex));
        }
    }


    private boolean shouldRetryAgain(OffsetDateTime nextRetryTimestamp)
    {
        OffsetDateTime processingFirstStartedTimestamp = this.entity.getProcessingFirstStartedTimestamp();
        if (processingFirstStartedTimestamp == null)
        {
            // First failure cycle: retry is still allowed
            return true;
        }

        OffsetDateTime retryDeadline = processingFirstStartedTimestamp.plus(this.processor.getProperties().getRetryTimeout());
        return nextRetryTimestamp.isBefore(retryDeadline) || nextRetryTimestamp.isEqual(retryDeadline);
    }


    /**
     * Exponential backoff: 5s, 10s, 20s, 40s, 80s, ... capped at 5 minutes
     */
    OffsetDateTime calculateNextRetryTimestamp(Long retryCount)
    {
        Duration initialRetryDelay = this.processor.getProperties().getInitialRetryDelay();
        Duration maxRetryDelay = this.processor.getProperties().getMaxRetryDelay();
        long count = retryCount != null ? retryCount : 0;
        long delaySeconds = Math.min((long) (initialRetryDelay.toSeconds() * Math.pow(2, count)), maxRetryDelay.toSeconds());
        return OffsetDateTime.now().plusSeconds(delaySeconds);
    }


    @Override
    public boolean isFatalException(Throwable ex)
    {
        return true;
    }
}
