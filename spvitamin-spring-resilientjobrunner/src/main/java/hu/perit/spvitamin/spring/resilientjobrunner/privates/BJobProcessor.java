package hu.perit.spvitamin.spring.resilientjobrunner.privates;

import hu.perit.spvitamin.core.exception.ServerException;
import hu.perit.spvitamin.spring.resilientjobrunner.AbstractProcessor;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus;
import hu.perit.spvitamin.spring.resilientjobrunner.ProcessorType;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobData;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobDataService;
import hu.perit.spvitamin.spring.resilientjobrunner.config.ResilientJobCollectionProperties;
import hu.perit.spvitamin.spring.resilientjobrunner.config.ResilientJobProperties;
import hu.perit.spvitamin.spring.threadcontext.ThreadContextDecorator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
class BJobProcessor
{
    private final ResilientJobCollectionProperties resilientJobCollectionProperties;
    private final ResilientJobDataService resilientJobDataService;
    private final Map<ProcessorType, BatchExecutor> executorMap = new HashMap<>();


    @PostConstruct
    void setUp()
    {
        for (Map.Entry<String, ResilientJobProperties> entry : this.resilientJobCollectionProperties.getResilientJobs().entrySet())
        {
            String name = entry.getKey();
            ResilientJobProperties properties = entry.getValue();
            ProcessorType processorType = ProcessorType.of(name, properties.getId());
            this.executorMap.put(
                    processorType,
                    new BatchExecutor(properties.getThreadPoolSize(), createProcessor(processorType, properties))
            );
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


    @Scheduled(fixedDelay = 5000)
    private void triggerProcessing()
    {
        this.executorMap.keySet().parallelStream().forEach(this::process);
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
            int countTerminatedEntities = this.resilientJobDataService.terminatePermanentlyFailingEntities(processorType, properties.getRetryTimeout());
            if (countTerminatedEntities > 0)
            {
                log.info("{} jobs have been terminated due to permanent errors", countTerminatedEntities);
            }

            int countResetedEntities = this.resilientJobDataService.resetStuckInProgressEntities(processorType, Duration.ofMinutes(5));
            if (countResetedEntities > 0)
            {
                log.info("{} jobs have been reset back to {}", countResetedEntities, ResilientJobStatus.CREATED);
            }

            long lastId = 0;
            while (!Thread.currentThread().isInterrupted())
            {
                List<? extends ResilientJobData> entities = this.resilientJobDataService.getNextBatchAndSetInProgressState(processorType, lastId);
                if (entities.isEmpty())
                {
                    return;
                }
                lastId = entities.getLast().getId();

                log.info("Found {} jobs between {} and {}", entities.size(), entities.getFirst().getId(), lastId);

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
                    countResetedEntities = this.resilientJobDataService.resetStuckInProgressEntities(processorType, Duration.ofMinutes(0));
                    if (countResetedEntities > 0)
                    {
                        log.info("{} jobs have been reset back to {}", countResetedEntities, ResilientJobStatus.CREATED);
                    }
                }
            }
        }
    }
}
