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

package hu.perit.spvitamin.spring.resilientjobrunner.db.repo;

import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobStatus;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.OffsetDateTime;
import java.util.List;

public interface AbstractResilientJobRepo<T extends AbstractResilientJobEntity> extends JpaRepository<T, Long>
{
    @Modifying
    @Query("update #{#entityName} e set e.status = :errorState where e.processorType = :processorType and e.status <> :errorState and e.creationTimestamp < :timestamp and e.processingStartedTimestamp is not null")
    int terminatePermanentlyFailingEntities(
            Long processorType,
            OffsetDateTime timestamp,
            ResilientJobStatus errorState
    );

    @Modifying
    @Query("update #{#entityName} e set e.status = :targetState, e.retryCount = e.retryCount + 1 where e.processorType = :processorType and e.status = :whereState and e.processingStartedTimestamp < :timestamp")
    int resetStuckInProgressEntities(
            Long processorType,
            OffsetDateTime timestamp,
            ResilientJobStatus whereState,
            ResilientJobStatus targetState
    );

    // We use here timeout = 0 which means, do not wait for locked rows.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
    List<T> findAllByProcessorTypeAndIdGreaterThanAndStatusOrderById(Long processorType, long lastId, ResilientJobStatus status, PageRequest pageRequest);

    @Modifying
    @Query("""
            update #{#entityName} e set e.status = :status,
            e.processingStartedTimestamp = :processingStartedTimestamp,
            e.creationTimestamp = case when (e.retryCount = 0 or e.retryCount is null) then :processingStartedTimestamp else e.creationTimestamp end
            where e.id in :ids""")
    int updateStatusAndProcessingStartedTimestamp(
            List<Long> ids,
            ResilientJobStatus status,
            OffsetDateTime processingStartedTimestamp
    );


    @Modifying
    @Query("update #{#entityName} e set e.status = :status, e.errorText = :errorText, e.retryCount = e.retryCount + 1 where e.id = :id")
    void updateStatusAndError(
            Long id,
            ResilientJobStatus status,
            String errorText
    );

    @Modifying
    @Query("update #{#entityName} e set e.status = :status where e.id in :ids and e.status = :criteria")
    int updateStatusWhere(List<Long> ids, ResilientJobStatus status, ResilientJobStatus criteria);
}
