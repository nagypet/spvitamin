package hu.perit.spvitamin.spring.resilientjobrunner.service.impl.entity;

import com.google.common.collect.Lists;
import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.resilientjobrunner.ProcessorType;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import hu.perit.spvitamin.spring.resilientjobrunner.db.repo.AbstractResilientJobRepo;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
public class AbstractResilientJobEntityServiceImpl<T extends AbstractResilientJobEntity> implements ResilientJobEntityService<T>
{
    public static final int MAX_CRITERIA_IN_QUERIES = 1000;

    private final AbstractResilientJobRepo<T> repo;


    @Override
    public T save(T entity)
    {
        return this.repo.save(entity);
    }


    @Override
    @Transactional
    public int terminatePermanentlyFailingEntities(ProcessorType processorType, Duration timeout)
    {
        return this.repo.terminatePermanentlyFailingEntities(processorType.getProcessorId(), OffsetDateTime.now().minusSeconds(timeout.getSeconds()), ResilientJobStatus.ERROR);
    }


    @Override
    @Transactional
    public int resetStuckInProgressEntities(ProcessorType processorType, Duration timeout)
    {
        return this.repo.resetStuckInProgressEntities(processorType.getProcessorId(), OffsetDateTime.now().minusSeconds(timeout.getSeconds()), ResilientJobStatus.IN_PROGRESS, ResilientJobStatus.CREATED);
    }


    @Override
    @Transactional
    public List<T> getNextBatchAndSetInProgressState(ProcessorType processorType, Long lastId)
    {
        PageRequest pageRequest = PageRequest.of(0, 200);
        List<T> entities = this.repo.findAllByProcessorTypeAndIdGreaterThanAndStatusOrderById(processorType.getProcessorId(), lastId, ResilientJobStatus.CREATED, pageRequest);
        this.repo.updateStatusAndProcessingStartedTimestamp(entities.stream().map(i -> i.getId()).toList(), ResilientJobStatus.IN_PROGRESS, OffsetDateTime.now());
        return entities;
    }


    @Override
    @Transactional
    public int resetInProgressEntitiesById(List<Long> ids)
    {
        // com.microsoft.sqlserver.jdbc.SQLServerException: The incoming request has too many parameters. The server supports a maximum of 2100 parameters. Reduce the number of parameters and resend the request
        return Lists.partition(ids, MAX_CRITERIA_IN_QUERIES).stream()
                .mapToInt(idList -> this.repo.updateStatusWhere(ids, ResilientJobStatus.CREATED, ResilientJobStatus.IN_PROGRESS))
                .sum();
    }


    @Override
    @Transactional
    public void deleteById(Long id)
    {
        this.repo.deleteById(id);
    }


    @Override
    @Transactional
    public void saveError(Long id, ResilientJobStatus resilientJobStatus, Exception e)
    {
        this.repo.updateStatusAndError(id, resilientJobStatus, StackTracer.toString(e));
    }
}
