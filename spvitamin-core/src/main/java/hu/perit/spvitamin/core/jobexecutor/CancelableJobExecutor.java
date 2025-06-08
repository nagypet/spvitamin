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
 * A specialized ThreadPoolExecutor that provides enhanced job tracking and cancellation capabilities.
 * 
 * <p>This executor maintains a registry of submitted jobs with their execution status,
 * allowing for individual job cancellation, status monitoring, and graceful shutdown.
 * Each job is identified by a unique ID of type T, which enables tracking and management
 * throughout its lifecycle.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Job tracking by ID with status monitoring (queued, running, stopping)</li>
 *   <li>Individual job cancellation</li>
 *   <li>Batch cancellation of all jobs</li>
 *   <li>Graceful shutdown with configurable timeout</li>
 *   <li>Detailed logging of job lifecycle events</li>
 *   <li>Prevention of duplicate job submissions</li>
 * </ul>
 * 
 * <p>This executor is particularly useful for long-running tasks that may need
 * to be cancelled, or in scenarios where the application needs to track the
 * status of specific jobs and ensure proper cleanup of resources.</p>
 * 
 * @param <T> the type of job identifier
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

    public synchronized Future<Void> submitJob(T id, Callable<Void> job)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Job id cannot be null");
        }

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

        if (r instanceof Future<?> future)
        {
            try
            {
                if (t == null)
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
                    catch (RuntimeException re)
                    {
                        t = re;
                        log.error("[{}] RuntimeException caught from future.get(): {}", this.context, re.toString());
                    }
                }

                String action = (t instanceof CancellationException) ? "has been cancelled" : "has finished";
                T id = removeFrom(this.futureMap, future);
                if (id != null)
                {
                    log.debug("[{}] afterExecute(): {}", this.context, statusTextWithAction(id, action));
                }
            }
            catch (Exception ex)
            {
                // Catch any unexpected exceptions to ensure we don't leave futures in the map
                log.error("[{}] Unexpected exception in afterExecute: {}", this.context, ex.toString());
                try
                {
                    // Last attempt to remove the future from the map
                    T id = removeFrom(this.futureMap, future);
                    if (id != null)
                    {
                        log.debug("[{}] Future removed after exception: {}", this.context, id);
                    }
                }
                catch (Exception e)
                {
                    log.error("[{}] Failed to remove future from map: {}", this.context, e.toString());
                }
            }
        }
        else
        {
            log.warn("[{}] afterExecute received non-Future runnable: {}", this.context, r);
        }

        if (t != null && !(t instanceof CancellationException))
        {
            log.error("[{}] Task execution failed: {}", this.context, t.toString());
        }
    }


    private String statusTextWithAction(T id, String action)
    {
        return String.format("Job %s %s! %s", id != null ? id : "null", action, statusText());
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
        if (id == null)
        {
            log.warn("[{}] cancelJob called with null id", this.context);
            return false;
        }

        log.debug("[{}] cancelJob({})", this.context, id);
        if (this.futureMap.contains(id))
        {
            FutureMap.Status status = this.futureMap.getStatus(id);
            Future<?> future = this.futureMap.get(id);

            if (future == null)
            {
                log.error("[{}] Future is null for job {}", this.context, id);
                this.futureMap.remove(id);
                return false;
            }

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


    /**
     * Cancels all running jobs and waits for them to complete.
     * This method has a default timeout of 30 seconds, which can be overridden.
     */
    public void cancelAll()
    {
        cancelAll(30, TimeUnit.SECONDS);
    }

    /**
     * Cancels all running jobs and waits for them to complete with a specified timeout.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return true if all jobs were cancelled successfully, false if the timeout was reached
     */
    public boolean cancelAll(long timeout, TimeUnit unit)
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

        long timeoutMillis = unit.toMillis(timeout);
        long startTime = System.currentTimeMillis();
        long elapsedTime;

        while (!Thread.currentThread().isInterrupted() && (countRunning() > 0))
        {
            elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > timeoutMillis)
            {
                log.error("[{}] Timeout reached. Some jobs could not be cancelled within {} {}!", 
                    this.context, timeout, unit);
                return false;
            }

            if (countRunning() > 0)
            {
                this.futureMap.keySet().forEach(i -> log.debug("[{}] Waiting for stop job {}", this.context, i));
            }

            try
            {
                // Sleep for a short time to avoid busy waiting, but not too long to be responsive
                TimeUnit.MILLISECONDS.sleep(100);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                log.warn("[{}] Interrupted while waiting for jobs to cancel", this.context);
                return false;
            }
        }

        log.info("[{}] All jobs cancelled.", this.context);
        return true;
    }

    /**
     * Shuts down this executor in an orderly fashion.
     * First attempts to cancel all running jobs, then shuts down the executor.
     * If jobs don't complete within the specified timeout, the executor is forcibly shut down.
     * 
     * @param timeout the maximum time to wait for jobs to complete
     * @param unit the time unit of the timeout argument
     * @return true if all jobs completed and the executor shut down normally, false otherwise
     */
    public boolean shutdown(long timeout, TimeUnit unit)
    {
        log.info("[{}] Shutting down...", this.context);

        boolean allCancelled = cancelAll(timeout, unit);

        // Initiate orderly shutdown
        super.shutdown();

        boolean terminated = false;
        try
        {
            // Wait for tasks to terminate
            terminated = super.awaitTermination(timeout, unit);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            log.warn("[{}] Interrupted while waiting for termination", this.context);
        }

        if (!terminated)
        {
            log.warn("[{}] Not all tasks completed, forcing shutdown", this.context);
            super.shutdownNow();
        }

        log.info("[{}] Shutdown complete", this.context);
        return allCancelled && terminated;
    }

    /**
     * Shuts down this executor in an orderly fashion with a default timeout of 30 seconds.
     */
    public void shutdown()
    {
        shutdown(30, TimeUnit.SECONDS);
    }
}
