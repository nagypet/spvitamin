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

package hu.perit.spvitamin.spring.data.pessimistic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Custom base repository implementation that automatically marks entities as Hibernate read-only
 * when loaded via {@link #findByIdReadOnly} or {@link #findAllByIdReadOnly}.
 * <p>
 * This prevents Hibernate dirty-checking on entities that are loaded for read-only purposes
 * (e.g. authorization checks, quota checks), even when these loads happen inside a writable
 * outer transaction or within an Open EntityManager In View session.
 * <p>
 * The entity is NOT marked as read-only if it already holds a write lock in the current session
 * (e.g. previously fetched via {@link #findByIdWithWriteLock}). This is determined by checking
 * the JPA lock mode rather than the transaction's read-only flag, so read-only marking works
 * correctly even when called from within a read-write outer transaction.
 * <p>
 * Register as base class in {@code @EnableJpaRepositories(repositoryBaseClass = PessimisticJpaRepositoryImpl.class)}.
 */
@Slf4j
public class PessimisticJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> implements PessimisticJpaRepository<T, ID>
{
    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ?> entityInformation;


    public PessimisticJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager)
    {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }


    @Override
    @Transactional
    public <S extends T> S save(S entity)
    {
        if (!this.entityInformation.isNew(entity) && this.entityManager.contains(entity))
        {
            Session session = this.entityManager.unwrap(Session.class);
            if (session.isReadOnly(entity))
            {
                throw new IllegalStateException(
                        "save() called on a Hibernate read-only entity!" +
                                " Entity: " + entity.getClass().getSimpleName() +
                                " id=" + this.entityInformation.getId(entity));
            }
            else
            {
                LockModeType lockMode = this.entityManager.getLockMode(entity);
                if (lockMode == LockModeType.NONE || lockMode == LockModeType.OPTIMISTIC || lockMode == LockModeType.READ)
                {
                    log.warn("save() called on entity without a pessimistic write lock — consider using" +
                            " findByIdWithWriteLock() to prevent optimistic locking failures in concurrent scenarios." +
                            " Entity: {} id={} lockMode={}", entity.getClass().getSimpleName(), this.entityInformation.getId(entity), lockMode);
                }
            }
        }
        return super.save(entity);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<T> findByIdReadOnly(ID id)
    {
        Optional<T> result = super.findById(id);
        result.ifPresent(entity -> {
            // Mark as read-only unless the entity already holds a write lock in this session
            // (e.g. previously fetched via findByIdWithWriteLock). Checking the JPA lock mode
            // avoids silently disabling dirty-checking on intentionally write-locked entities,
            // while still protecting genuinely read-only loads in read-write outer transactions.
            LockModeType lockMode = this.entityManager.getLockMode(entity);
            if (lockMode == LockModeType.NONE || lockMode == LockModeType.OPTIMISTIC || lockMode == LockModeType.READ)
            {
                this.entityManager.unwrap(Session.class).setReadOnly(entity, true);
            }
        });
        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public List<T> findAllByIdReadOnly(Iterable<ID> ids)
    {
        List<T> result = super.findAllById(ids);
        Session session = this.entityManager.unwrap(Session.class);
        result.forEach(entity -> {
            LockModeType lockMode = this.entityManager.getLockMode(entity);
            if (lockMode == LockModeType.NONE || lockMode == LockModeType.OPTIMISTIC || lockMode == LockModeType.READ)
            {
                session.setReadOnly(entity, true);
            }
        });
        return result;
    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<T> findByIdWithWriteLock(ID id)
    {
        Map<String, Object> hints = Map.of("jakarta.persistence.lock.timeout", 3000);
        T entity = this.entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_WRITE, hints);
        if (entity != null)
        {
            this.entityManager.unwrap(Session.class).setReadOnly(entity, false);
        }
        return Optional.ofNullable(entity);
    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<T> findAllByIdWithWriteLock(Iterable<ID> ids)
    {
        Map<String, Object> hints = Map.of("jakarta.persistence.lock.timeout", 3000);
        List<T> result = new ArrayList<>();
        for (ID id : ids)
        {
            T entity = this.entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_WRITE, hints);
            if (entity != null)
            {
                this.entityManager.unwrap(Session.class).setReadOnly(entity, false);
                result.add(entity);
            }
        }
        return result;
    }
}
