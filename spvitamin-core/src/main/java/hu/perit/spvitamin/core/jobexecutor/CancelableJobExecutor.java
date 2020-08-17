/*
 * Copyright 2020-2020 the original author or authors.
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

import hu.perit.spvitamin.core.exception.UnexpectedConditionException;
import lombok.extern.log4j.Log4j;

import java.util.concurrent.*;

/**
 * @author Peter Nagy
 */

@Log4j
public class CancelableJobExecutor<T> extends ThreadPoolExecutor {

    // A sorbanálló job-ok
    protected FutureMap<T> futureMap = new FutureMap<>();


    public CancelableJobExecutor(int poolSize) {
        super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }


    @Override
    protected synchronized void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        if (r instanceof Future<?>) {
            Future<?> future = (Future<?>) r;
            T id = this.futureMap.get(future);
            if (id != null) {
                log.debug(String.format("Job %s has started!", id.toString()));
            }
            else {
                throw new UnexpectedConditionException();
            }
        }
    }


    @Override
    protected synchronized void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (t == null && r instanceof Future<?>) {
            Future<?> future = (Future<?>) r;
            try {
                future.get();
            }
            catch (CancellationException ce) {
                t = ce;
            }
            catch (ExecutionException ee) {
                t = ee.getCause();
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
            }
            finally {
                T id = this.futureMap.get(future);
                if (id != null) {
                    log.debug(String.format("Job %s has terminated!", id.toString()));

                    if (this.futureMap.contains(id)) {
                        this.futureMap.remove(id);
                    }
                }
                else {
                    log.error("Map of futures is inconsistant! You should consider a restart...");
                }
            }
        }
        if (t != null) {
            log.error(t);
        }
    }


    public synchronized void cancelJob(T id) {
        //log.debug(String.format("cancelJob(%d)", id));
        if (this.futureMap.contains(id)) {
            Future future = this.futureMap.get(id);
            if (!future.isDone()) {
                future.cancel(true);
                this.purge();
            }
        }
        else {
            throw new JobNotFoundException(String.format("There is no job with id %s", id.toString()));
        }
    }
}
