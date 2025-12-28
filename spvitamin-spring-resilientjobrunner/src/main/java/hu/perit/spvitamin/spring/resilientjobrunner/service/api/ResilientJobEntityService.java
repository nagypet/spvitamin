package hu.perit.spvitamin.spring.resilientjobrunner.service.api;

import hu.perit.spvitamin.spring.resilientjobrunner.ProcessorType;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;

import java.time.Duration;
import java.util.List;

public interface ResilientJobEntityService<T extends AbstractResilientJobEntity>
{
    int terminatePermanentlyFailingEntities(ProcessorType processorType, Duration timeout);

    int resetStuckInProgressEntities(ProcessorType processorType, Duration timeout);

    List<T> getNextBatchAndSetInProgressState(ProcessorType processorType, Long lastId);

    int resetInProgressEntitiesById(List<Long> ids);

    void deleteById(Long id);

    void saveError(Long id, ResilientJobStatus resilientJobStatus, Exception e);

    T save(T entity);
}
