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

package hu.perit.spvitamin.core.batchprocessing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */


@Slf4j
public abstract class BatchProcessor {

    private int threadPoolSize;

    public BatchProcessor(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }


    @SuppressWarnings({"squid:S3776", "squid:S1141", "squid:S1193"})
    public boolean process(List<BatchJob> batchJobs) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        // Call the first one synchronously to see, if connection can be established
        BatchJob firstJob = null;
        if (batchJobs != null && !batchJobs.isEmpty()) {
            log.info(String.format("Processing started with %d jobs in %d threads...", batchJobs.size(), threadPoolSize));
            firstJob = batchJobs.get(0);
            batchJobs.remove(0);

            try {
                firstJob.call();
            }
            catch (Exception ex) {
                // meg kell nézni, hogy fatalis hiba történt-e, ami az egész batch feldolgozását meg kell, hogy állítsa?
                if (firstJob.isFatalException(ex)) {
                    // Ha fatális hiba
                    throw new ExecutionException(ex);
                }
            }
        }

        if (batchJobs.isEmpty()) {
            return true;
        }

        // Invoke the rest
        boolean shutdownImmediately = false;
        try {
            Map<Future<Boolean>, BatchJob> futures = new HashMap<>();
            BatchJobStatus status = new BatchJobStatus(false);
            for (BatchJob job : batchJobs) {
                job.setStatus(status);
                if (Thread.currentThread().isInterrupted() || status.isFatalError()) {
                    shutdownImmediately = true;
                    return false;
                }
                futures.put(executorService.submit(job), job);
            }

            // mindegyiket beetettük, lezárjuk az inputot
            executorService.shutdown();

            // amelyik elkészült, megvizsgáljuk az exception állapotot
            boolean thereIsUndone = true;
            while (thereIsUndone) {
                thereIsUndone = false;
                Iterator<Map.Entry<Future<Boolean>, BatchJob>> iter = futures.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Future<Boolean>, BatchJob> mapEntry = iter.next();
                    Future<Boolean> future = mapEntry.getKey();

                    if (Thread.currentThread().isInterrupted()) {
                        shutdownImmediately = true;
                        return false;
                    }

                    if (future.isDone()) {
                        iter.remove();

                        // Calling get to see if there was an exception
                        try {
                            future.get();
                        }
                        catch (ExecutionException | InterruptedException ex) {
                            log.error(StackTracer.toString(ex));
                            // meg kell nézni, hogy fatalis hiba történt-e, ami az egész batch feldolgozását meg kell, hogy állítsa?
                            if (ex instanceof ExecutionException) {
                                if (ex.getCause() == null
                                        || mapEntry.getValue().isFatalException(((ExecutionException) ex).getCause())) {
                                    // Ha fatális hiba
                                    shutdownImmediately = true;
                                    throw ex;
                                }
                            }
                            else {
                                throw ex;
                            }
                        }
                    }
                    else {
                        thereIsUndone = true;
                    }
                }

                if (thereIsUndone) {
                    TimeUnit.MILLISECONDS.sleep(200);
                }
            }

            return true;
        }
        catch (InterruptedException ex) {
            log.warn(ex.toString());
            shutdownImmediately = true;
            throw ex;
        }
        finally {
            if (shutdownImmediately) {
                log.info("Fatal error or interrupted, exiting immediately!");
                executorService.shutdownNow();
            }

            // meg kell várni, míg mindegyik elkészül
            while (!executorService.isTerminated()) {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
            }

            log.debug("Processing done.");
        }
    }
}
