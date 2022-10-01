/*
 * Copyright 2020-2021 the original author or authors.
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

    // Running jobs
    protected final FutureMap<T> futureMap = new FutureMap<>();

    // Stopping jobs
    protected final FutureMap<T> stoppingFutures = new FutureMap<>();

    public CancelableJobExecutor(int poolSize)
    {
        super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }


    @Override
    protected synchronized void beforeExecute(Thread t, Runnable r)
    {
        super.beforeExecute(t, r);
        if (r instanceof Future<?>)
        {
            Future<?> future = (Future<?>) r;
            T id = this.futureMap.get(future);
            if (id != null)
            {
                log.debug("beforeExecute({}, {}): {}", t, r, statusTextWithAction(id, "has started"));
            }
            else
            {
                log.error("beforeExecute({}, {}): Future is not in the futureMap!", t, r);
            }
        }
        else
        {
            log.error("beforeExecute - non Future({}, {})", t, r);
        }
    }


    @Override
    protected synchronized void afterExecute(Runnable r, Throwable t)
    {
        log.debug("afterExecute({}, {})", r, t);
        super.afterExecute(r, t);

        if (t == null && r instanceof Future<?>)
        {
            Future<?> future = (Future<?>) r;
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
                    log.debug("afterExecute(): {}", statusTextWithAction(id, action));
                }
                else
                {
                    id = removeFrom(this.stoppingFutures, future);
                    if (id != null)
                    {
                        log.debug("afterExecute(): {}", statusTextWithAction(id, action));
                    }
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
        return String.format("[Running jobs: %d, stopping jobs: %d]", this.futureMap.size(), this.stoppingFutures.size());
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
        log.debug("cancelJob({})", id.toString());
        if (this.futureMap.contains(id))
        {
            Future<?> future = this.futureMap.get(id);
            if (!future.isDone())
            {
                log.debug("Job {} will be cancelled!", id.toString());
                future.cancel(true);
                this.purge();
            }
            this.futureMap.remove(id);
            this.stoppingFutures.put(id, future);
            log.debug(statusText());
            return true;
        }

        log.info("cancelJob({}): Job not found!", id.toString());
        return false;
    }

    public synchronized int countRunning()
    {
        return this.futureMap.size();
    }

    public synchronized int countStopping()
    {
        return this.stoppingFutures.size();
    }

    // intentionally not synchronized
    public void cancelAll()
    {
        synchronized (this.futureMap)
        {
            log.info("Cancel all...");
            Set<T> keySet = new HashSet<>(this.futureMap.keySet());
            for (T id : keySet)
            {
                cancelJob(id);
            }
        }

        while (!Thread.currentThread().isInterrupted() && (countRunning() > 0 || countStopping() > 0))
        {
            try
            {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
        log.info("All jobs cancelled.");
    }
}
