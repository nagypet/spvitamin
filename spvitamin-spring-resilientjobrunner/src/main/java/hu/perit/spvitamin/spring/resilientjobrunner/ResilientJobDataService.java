package hu.perit.spvitamin.spring.resilientjobrunner;


import java.time.Duration;
import java.util.List;

public interface ResilientJobDataService
{
    int terminatePermanentlyFailingEntities(ProcessorType processorType, Duration timeout);

    int resetStuckInProgressEntities(ProcessorType processorType, Duration timeout);

    List<? extends ResilientJobData> getNextBatchAndSetInProgressState(ProcessorType processorType, long lastId);

    void deleteById(Long id);

    void saveError(Long id, ResilientJobStatus resilientJobStatus, Exception e);
}
