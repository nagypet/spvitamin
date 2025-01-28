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

package hu.perit.spvitamin.spring.data.cache;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.took.Took;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
public class JpaWriteBehindCacheImpl<T, ID> implements JpaWriteBehindCache<T, ID>
{

    private JpaRepository<T, ID> jpaRepository;
    private long maxQueueSize = 200;
    private long maxDelayMillis = 5_000;
    private Consumer thrownAwayMethod;

    private final List<T> cache = new ArrayList<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> future;
    private boolean errorState = false;
    private Exception lastException = null;
    private boolean shutdownInProgress = false;

    @Override
    public synchronized void setRepo(JpaRepository<T, ID> jpaRepository)
    {
        this.jpaRepository = jpaRepository;
        future = executor.scheduleWithFixedDelay(new ScheduledTask(), 0, this.maxDelayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void setMaxQueueSize(long maxQueueSize)
    {
        this.maxQueueSize = maxQueueSize;
    }

    @Override
    public synchronized void setMaxDelayMillis(long maxDelayMillis)
    {
        this.maxDelayMillis = maxDelayMillis;
        if (this.future != null)
        {
            this.future.cancel(false);
        }
        if (this.jpaRepository != null)
        {
            this.future = executor.scheduleWithFixedDelay(new ScheduledTask(), 0, this.maxDelayMillis, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public synchronized void put(T data)
    {
        if (this.jpaRepository == null)
        {
            throw new WriteBehindCacheException("Not initialized!");
        }

        if (this.shutdownInProgress)
        {
            throw new WriteBehindCacheException("Shutdown in progress!");
        }

        if (this.errorState)
        {
            throw new WriteBehindCacheException("Failure at saving in EVENTLOGS!", this.lastException);
        }

        this.cache.add(data);

        if (this.cache.size() > this.maxQueueSize)
        {
            CompletableFuture.runAsync(() -> this.persistCache());
        }
    }

    @Override
    public void setThrownAwayMethod(Consumer<List<T>> thrownAwayRecords)
    {
        this.thrownAwayMethod = thrownAwayMethod;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Implementation private staff
    //------------------------------------------------------------------------------------------------------------------


    @Override
    public synchronized void preDestroy()
    {
        this.shutdownInProgress = true;
        this.persistCache();
    }


    private synchronized void persistCache()
    {
        try (Took took = new Took(false))
        {
            int countRecords = this.cache.size();
            if (countRecords > 0)
            {
                this.jpaRepository.saveAll(cache);
                cache.clear();
                this.errorState = false;
                this.lastException = null;

                double duration = (double) took.getDuration();
                double avg = duration / countRecords;
                log.info(String.format("Saving %d records took: %d ms. Average: %.2f ms", countRecords, took.getDuration(), avg));
            }
        }
        catch (RuntimeException ex)
        {
            log.error(StackTracer.toString(ex));
            this.errorState = true;
            this.lastException = ex;

            if (shutdownInProgress)
            {
                if (this.thrownAwayMethod != null)
                {
                    this.thrownAwayMethod.accept(this.cache);
                }
            }
            else
            {
                log.warn(String.format("There is still %d unsaved events in the cache", this.cache.size()));
            }
        }
    }


    private class ScheduledTask implements Runnable
    {

        @Override
        public void run()
        {
            persistCache();
        }
    }
}
