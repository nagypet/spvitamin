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
import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
 * If a read-write transaction is already active when {@link #findByIdReadOnly} or
 * {@link #findAllByIdReadOnly} is called, the entity is NOT marked as read-only. This avoids
 * silently disabling dirty-checking on an entity that was previously fetched with a write lock
 * in the same transaction (e.g. via {@link #findByIdWithWriteLock}).
 * <p>
 * Register as base class in {@code @EnableJpaRepositories(repositoryBaseClass = PessimisticJpaRepositoryImpl.class)}.
 */
public class PessimisticJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> implements PessimisticJpaRepository<T, ID>
{
    private final EntityManager entityManager;


    public PessimisticJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager)
    {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<T> findByIdReadOnly(ID id)
    {
        Optional<T> result = super.findById(id);
        if (!TransactionSynchronizationManager.isCurrentTransactionReadOnly())
        {
            return result;
        }
        result.ifPresent(entity -> this.entityManager.unwrap(Session.class).setReadOnly(entity, true));
        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public List<T> findAllByIdReadOnly(Iterable<ID> ids)
    {
        List<T> result = super.findAllById(ids);
        if (!TransactionSynchronizationManager.isCurrentTransactionReadOnly())
        {
            return result;
        }
        Session session = this.entityManager.unwrap(Session.class);
        result.forEach(entity -> session.setReadOnly(entity, true));
        return result;
    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<T> findByIdWithWriteLock(ID id)
    {
        Map<String, Object> hints = Map.of("jakarta.persistence.lock.timeout", 3000);
        T entity = this.entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_WRITE, hints);
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
                result.add(entity);
            }
        }
        return result;
    }
}
