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

package hu.perit.spvitamin.spring.resilientjobrunner.service.impl.entity;

import com.google.common.collect.Lists;
import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.crypto.HashUtils;
import hu.perit.spvitamin.spring.resilientjobrunner.ProcessorType;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus;
import hu.perit.spvitamin.spring.resilientjobrunner.config.ResilientJobProperties;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import hu.perit.spvitamin.spring.resilientjobrunner.db.repo.AbstractResilientJobRepo;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobEntityService;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractResilientJobEntityServiceImpl<T extends AbstractResilientJobEntity> implements ResilientJobEntityService<T>
{
    public static final int MAX_CRITERIA_IN_QUERIES = 1000;

    private final AbstractResilientJobRepo<T> repo;


    protected abstract T supplyEntity();


    @Override
    public T createNew(ResilientJobProperties jobProperties, ResilientJobParameter parameter)
    {
        // Checking if there is already an ongoing job with the same parameters
        String parameterJson = parameter.toJson();
        String parameterHash = HashUtils.get32BytesSha256Hash(parameterJson);
        T existingEntity = findExistingOngoingJob(jobProperties.getId(), parameterHash).orElse(null);
        if (existingEntity != null)
        {
            log.info("This job is already processing: {}", existingEntity);
            return existingEntity;
        }

        T resilientJobEntity = supplyEntity();
        resilientJobEntity.setCreationTimestamp(OffsetDateTime.now());
        resilientJobEntity.setStatus(ResilientJobStatus.CREATED);
        resilientJobEntity.setProcessorType(jobProperties.getId());
        resilientJobEntity.setParameterVersion(parameter.getVersion());
        resilientJobEntity.setParameters(parameterJson);
        resilientJobEntity.setParameterHash(parameterHash);
        resilientJobEntity.setRetryCount(0L);
        resilientJobEntity.setOperationId(UUID.randomUUID());

        return this.repo.save(resilientJobEntity);
    }


    private Optional<T> findExistingOngoingJob(Long processorType, String parameterHash)
    {
        return this.repo.findByStatusInAndProcessorTypeAndParameterHash(
                EnumSet.of(ResilientJobStatus.CREATED, ResilientJobStatus.IN_PROGRESS),
                processorType,
                parameterHash
        );
    }


    @Override
    @Transactional
    public int terminatePermanentlyFailingEntities(ProcessorType processorType, Duration timeout)
    {
        return this.repo.terminatePermanentlyFailingEntities(
                processorType.getProcessorId(),
                OffsetDateTime.now().minusSeconds(timeout.getSeconds()),
                EnumSet.of(ResilientJobStatus.CREATED, ResilientJobStatus.IN_PROGRESS),
                ResilientJobStatus.ERROR,
                OffsetDateTime.now()
        );
    }


    @Override
    @Transactional
    public int resetStuckInProgressEntities(ProcessorType processorType, Duration timeout)
    {
        return this.repo.resetStuckInProgressEntities(
                processorType.getProcessorId(),
                OffsetDateTime.now().minusSeconds(timeout.getSeconds()),
                ResilientJobStatus.IN_PROGRESS,
                ResilientJobStatus.CREATED
        );
    }


    @Override
    @Transactional
    public List<T> getNextBatchAndSetInProgressState(ProcessorType processorType, Long lastId)
    {
        PageRequest pageRequest = PageRequest.of(0, 200);
        List<T> entities = this.repo.findReadyForProcessing(
                processorType.getProcessorId(),
                lastId,
                ResilientJobStatus.CREATED,
                OffsetDateTime.now(),
                pageRequest
        );
        this.repo.updateStatusAndProcessingStartedTimestamp(
                entities.stream().map(i -> i.getId()).toList(),
                ResilientJobStatus.IN_PROGRESS,
                OffsetDateTime.now()
        );
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
    public void saveError(Long id, ResilientJobStatus resilientJobStatus, OffsetDateTime nextRetryTimestamp, Exception e)
    {
        this.repo.updateStatusAndError(id, resilientJobStatus, nextRetryTimestamp, StackTracer.toString(e));
    }


    /**
     * Önálló tranzakció: a lépés mellékhatása után a haladás biztosan megmarad.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void persistSagaContext(Long id, String contextJson, int contextVersion, String lastStep)
    {
        this.repo.updateSaga(id, contextJson, contextVersion, lastStep);
    }
}
