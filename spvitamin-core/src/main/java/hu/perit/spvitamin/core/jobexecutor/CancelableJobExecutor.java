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

    // A sorbanálló job-ok
    protected FutureMap<T> futureMap = new FutureMap<>();


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
                log.debug("beforeExecute({}, {}): Job {} has started! Running jobs: {}", t, r, id.toString(), this.futureMap.size());
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
                T id = this.futureMap.get(future);
                if (id != null)
                {
                    if (this.futureMap.contains(id))
                    {
                        this.futureMap.remove(id);
                    }
                    log.debug("afterExecute(): Job {} has terminated! Running jobs: {}", id.toString(), this.futureMap.size());
                }
            }
        }
        if (t != null)
        {
            log.error(t.toString());
        }
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
            log.debug("Running jobs: {}", this.futureMap.size());
            return true;
        }

        log.info("cancelJob({}): Job not found!", id.toString());
        return false;
    }
}
