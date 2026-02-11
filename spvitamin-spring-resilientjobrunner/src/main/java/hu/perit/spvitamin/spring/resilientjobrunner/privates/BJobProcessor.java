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

import hu.perit.spvitamin.core.exception.ServerException;
import hu.perit.spvitamin.core.typehelpers.ListUtils;
import hu.perit.spvitamin.spring.resilientjobrunner.AbstractProcessor;
import hu.perit.spvitamin.spring.resilientjobrunner.ProcessorType;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus;
import hu.perit.spvitamin.spring.resilientjobrunner.config.ResilientJobCollectionProperties;
import hu.perit.spvitamin.spring.resilientjobrunner.config.ResilientJobProperties;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobEntityService;
import hu.perit.spvitamin.spring.threadcontext.ThreadContextDecorator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
class BJobProcessor
{
    private final ResilientJobCollectionProperties resilientJobCollectionProperties;
    private final ResilientJobEntityService<? extends AbstractResilientJobEntity> resilientJobEntityService;
    private final Map<ProcessorType, BatchExecutor> executorMap = new HashMap<>();
    private ScheduledExecutorService scheduler;


    @PostConstruct
    void setUp()
    {
        int processorCount = this.resilientJobCollectionProperties.getResilientJobs().size();
        this.scheduler = Executors.newScheduledThreadPool(processorCount);

        for (Map.Entry<String, ResilientJobProperties> entry : this.resilientJobCollectionProperties.getResilientJobs().entrySet())
        {
            String name = entry.getKey();
            ResilientJobProperties properties = entry.getValue();
            ProcessorType processorType = ProcessorType.of(name, properties.getId());
            this.executorMap.put(
                    processorType,
                    new BatchExecutor(properties.getThreadPoolSize(), createProcessor(processorType, properties))
            );

            long pollingMillis = properties.getPollingInterval().toMillis();
            this.scheduler.scheduleWithFixedDelay(
                    () -> process(processorType),
                    pollingMillis,
                    pollingMillis,
                    TimeUnit.MILLISECONDS
            );

            log.info("{} configured with polling interval: {}", processorType, properties.getPollingInterval());
        }
    }


    @PreDestroy
    void tearDown()
    {
        if (this.scheduler != null)
        {
            this.scheduler.shutdown();
            try
            {
                if (!this.scheduler.awaitTermination(30, TimeUnit.SECONDS))
                {
                    this.scheduler.shutdownNow();
                }
            }
            catch (InterruptedException e)
            {
                this.scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }


    AbstractProcessor createProcessor(ProcessorType processorType, ResilientJobProperties properties)
    {
        try
        {
            Class<?> clazz = Class.forName(properties.getProcessorClass());
            return (AbstractProcessor) clazz.getDeclaredConstructor(ProcessorType.class, ResilientJobProperties.class).newInstance(processorType, properties);
        }
        catch (Exception e)
        {
            return ServerException.throwFrom(e);
        }
    }


    ResilientJobProperties getProperties(ProcessorType processorType)
    {
        return this.resilientJobCollectionProperties.get(processorType.getName());
    }


    void process(ProcessorType processorType)
    {
        ResilientJobProperties properties = getProperties(processorType);
        try (var ctx = new ThreadContextDecorator(properties.getContextDecoratorTag(), BJobHelper.getBatchId(processorType, null)))
        {
            int countTerminatedEntities = this.resilientJobEntityService.terminatePermanentlyFailingEntities(processorType, properties.getRetryTimeout());
            if (countTerminatedEntities > 0)
            {
                log.info("{} jobs have been terminated due to permanent errors", countTerminatedEntities);
            }

            int countResetedEntities = this.resilientJobEntityService.resetStuckInProgressEntities(processorType, properties.getProcessingTimeout());
            if (countResetedEntities > 0)
            {
                log.info("{} jobs have been reset back to {}", countResetedEntities, ResilientJobStatus.CREATED);
            }

            long lastId = 0;
            while (!Thread.currentThread().isInterrupted())
            {
                // Here the lastId is only needed to enforce iterating through the whole list. Otherwise, we would
                // get failed jobs immediately back, and the while loop would run continuously.
                List<? extends AbstractResilientJobEntity> entities = getNextBatch(processorType, lastId);
                if (entities.isEmpty())
                {
                    return;
                }
                lastId = entities.getLast().getId();

                // Creating BJobs
                BatchExecutor batchExecutor = this.executorMap.get(processorType);
                List<BJob> jobs = entities.stream().map(i -> batchExecutor.createBJob(i)).toList();
                try
                {
                    // Parallel processing of BJobs
                    batchExecutor.process(jobs);
                }
                catch (ExecutionException e)
                {
                    log.error(e.toString());
                    // Breaking the loop in case of a batch-related error
                    return;
                }
                catch (InterruptedException e)
                {
                    log.warn(e.toString());
                    Thread.currentThread().interrupt();
                    return;
                }
                finally
                {
                    // Here we have to reset those records that were not processed successfully
                    countResetedEntities = this.resilientJobEntityService.resetInProgressEntitiesById(entities.stream().map(i -> i.getId()).toList());
                    if (countResetedEntities > 0)
                    {
                        log.info("{} jobs have been reset back to {}", countResetedEntities, ResilientJobStatus.CREATED);
                    }
                }
            }
        }
    }


    List<? extends AbstractResilientJobEntity> getNextBatch(ProcessorType processorType, long lastId)
    {
        try
        {
            List<? extends AbstractResilientJobEntity> entities = this.resilientJobEntityService.getNextBatchAndSetInProgressState(processorType, lastId);
            if (entities.isEmpty())
            {
                return entities;
            }

            log.info("Found {} jobs between {} and {}", entities.size(), ListUtils.first(entities).getId(), ListUtils.last(entities).getId());
            return entities;
        }
        catch (Exception e)
        {
            log.error("Unexpected error during job processing trigger", e);
            return Collections.emptyList();
        }
    }
}
