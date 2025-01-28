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

package hu.perit.spvitamin.core.jobexecutor;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Nagy
 */

@Slf4j
public class CancelableJobExecutor<T> extends ThreadPoolExecutor
{

    private final String context;

    // Running and queued jobs
    private final FutureMap<T> futureMap = new FutureMap<>();

    public CancelableJobExecutor(int poolSize, String context)
    {
        super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.context = context;
    }

    public Future<Void> submitJob(T id, Callable<Void> job)
    {
        if (this.futureMap.contains(id))
        {
            throw new JobAlreadyProcessingException(String.format("[%s] Job '%s' is already being processed, request is ignored.", this.context, id.toString()));
        }

        Future<Void> future = super.submit(job);
        this.futureMap.put(id, future);
        log.debug("[{}] submitJob({}), {}", this.context, id, statusText());
        return future;
    }

    @Override
    protected synchronized void beforeExecute(Thread t, Runnable r)
    {
        super.beforeExecute(t, r);
        if (r instanceof Future<?> future)
        {
            T id = this.futureMap.get(future);
            if (id != null)
            {
                this.futureMap.setStatus(id, FutureMap.Status.RUNNING);
                log.debug("[{}] beforeExecute({}, {}): {}", this.context, t, r, statusTextWithAction(id, "has started"));
            }
            else
            {
                log.error("[{}] beforeExecute({}, {}): Future is not in the futureMap!", this.context, t, r);
            }
        }
        else
        {
            log.error("[{}] beforeExecute - non Future({}, {})", this.context, t, r);
        }
    }


    @Override
    protected synchronized void afterExecute(Runnable r, Throwable t)
    {
        log.debug("[{}] afterExecute({}, {})", this.context, r, t);
        super.afterExecute(r, t);

        if (t == null && r instanceof Future<?> future)
        {
            try
            {
                future.get();
            }
            catch (CancellationException ce)
            {
                t = ce;
            }
            catch (ExecutionException ee)
            {
                t = ee.getCause();
            }
            catch (InterruptedException ie)
            {
                Thread.currentThread().interrupt(); // ignore/reset
            }
            finally
            {
                String action = (t instanceof CancellationException) ? "has been cancelled" : "has finished";
                T id = removeFrom(this.futureMap, future);
                if (id != null)
                {
                    log.debug("[{}] afterExecute(): {}", this.context, statusTextWithAction(id, action));
                }
            }
        }
        if (t != null && !(t instanceof CancellationException))
        {
            log.error(t.toString());
        }
    }


    private String statusTextWithAction(T id, String action)
    {
        return String.format("Job %s %s! %s", id != null ? id.toString() : "null", action, statusText());
    }

    private String statusText()
    {
        return String.format("[Queued jobs: %d, Running jobs: %d, stopping jobs: %d]",
                this.futureMap.getCountByStatus(FutureMap.Status.QUEUED),
                this.futureMap.getCountByStatus(FutureMap.Status.RUNNING),
                this.futureMap.getCountByStatus(FutureMap.Status.STOPPING)
        );
    }


    private T removeFrom(FutureMap<T> map, Future<?> future)
    {
        T id = map.get(future);
        if (id != null)
        {
            if (map.contains(id))
            {
                map.remove(id);
                return id;
            }
        }

        return null;
    }

    public synchronized boolean cancelJob(T id)
    {
        log.debug("[{}] cancelJob({})", this.context, id.toString());
        if (this.futureMap.contains(id))
        {
            FutureMap.Status status = this.futureMap.getStatus(id);
            Future<?> future = this.futureMap.get(id);

            if (!future.isDone() && status != FutureMap.Status.STOPPING)
            {
                log.debug("[{}] Job {} is in {} state, will be cancelled!", this.context, id, status);
                future.cancel(true);
                this.purge();
            }

            if (status == FutureMap.Status.QUEUED)
            {
                this.futureMap.remove(id);
                log.debug("[{}] {}", this.context, statusText());
            }
            else if (status == FutureMap.Status.RUNNING)
            {
                this.futureMap.setStatus(id, FutureMap.Status.STOPPING);
                log.debug("[{}] {}", this.context, statusText());
            }
            else
            {
                log.debug("[{}] Job {} is in {} state, no action required.", this.context, id, status);
            }

            return true;
        }

        log.info("[{}] cancelJob({}): Job not found!", this.context, id);
        return false;
    }


    public synchronized long countRunning()
    {
        return this.futureMap.getCountByStatus(FutureMap.Status.RUNNING) + this.futureMap.getCountByStatus(FutureMap.Status.STOPPING);
    }


    public synchronized int countAll()
    {
        return this.futureMap.size();
    }


    // intentionally not synchronized
    public void cancelAll()
    {
        synchronized (this.futureMap)
        {
            log.info("[{}] Cancel all...", this.context);
            Set<T> keySet = new HashSet<>(this.futureMap.keySet());
            for (T id : keySet)
            {
                cancelJob(id);
            }
        }

        int countRetries = 0;
        while (!Thread.currentThread().isInterrupted() && (countRunning() > 0))
        {
            if (++countRetries > 300)
            {
                log.error("[{}] Some jobs could not be cancelled!", this.context);
                return;
            }

            if (countRunning() > 0)
            {
                this.futureMap.keySet().forEach(i -> log.debug("[{}] Waiting for stop job {}", this.context, i));
            }

            try
            {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
        log.info("[{}] All jobs cancelled.", this.context);
    }
}
