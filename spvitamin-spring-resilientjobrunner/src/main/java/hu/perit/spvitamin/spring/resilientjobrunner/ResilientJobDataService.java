package hu.perit.spvitamin.spring.resilientjobrunner;


import java.time.Duration;
import java.util.List;

public interface ResilientJobDataService
{
    int terminatePermanentlyFailingEntities(ProcessorType processorType, Duration timeout);

    int resetStuckInProgressEntities(ProcessorType processorType, Duration timeout);

    List<? extends ResilientJobData> getNextBatchAndSetInProgressState(ProcessorType processorType, Long lastId);

    int resetInProgressEntitiesById(List<Long> ids);

    void deleteById(Long id);

    void saveError(Long id, ResilientJobStatus resilientJobStatus, Exception e);
}
