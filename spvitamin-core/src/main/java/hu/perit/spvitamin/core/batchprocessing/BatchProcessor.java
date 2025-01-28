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

package hu.perit.spvitamin.core.batchprocessing;

import hu.perit.spvitamin.core.StackTracer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Peter Nagy
 */


@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class BatchProcessor
{

    protected final int threadPoolSize;


    public void process(List<? extends BatchJob> batchJobs) throws ExecutionException, InterruptedException
    {
        process(batchJobs, true, null, null);
    }


    public void process(List<? extends BatchJob> batchJobs, boolean runFirstSynchronously) throws ExecutionException, InterruptedException
    {
        process(batchJobs, runFirstSynchronously, null, null);
    }


    @SuppressWarnings({"squid:S3776", "squid:S1141", "squid:S1193"})
    public void process(List<? extends BatchJob> batchJobs, boolean runFirstSynchronously, Integer reportEveryNProcessed, String name)
            throws ExecutionException, InterruptedException
    {
        if (batchJobs == null || batchJobs.isEmpty())
        {
            return;
        }

        // Creating a copy of the ArrayList of BatchJobs, so that the input remains untouched
        List<BatchJob> copyOfBatchJobs = new ArrayList<>(batchJobs);

        log.info(String.format("Processing started with %d jobs in %d threads...", copyOfBatchJobs.size(), threadPoolSize));

        // Call the first one synchronously to see, if connection can be established
        BatchJob firstJob = copyOfBatchJobs.get(0);

        if (runFirstSynchronously)
        {
            copyOfBatchJobs.remove(0);

            try
            {
                firstJob.call();
            }
            catch (Exception ex)
            {
                // meg kell nézni, hogy fatalis hiba történt-e, ami az egész batch feldolgozását meg kell, hogy állítsa?
                if (firstJob.isFatalException(ex))
                {
                    // Ha fatális hiba
                    throw new ExecutionException(ex);
                }
            }
        }

        if (copyOfBatchJobs.isEmpty())
        {
            return;
        }

        ExecutorService executorService = createExecutorService();

        // Invoke the rest
        boolean shutdownImmediately = false;
        final Map<Future<Void>, BatchJob> futures = new HashMap<>();
        try
        {
            BatchJobStatus status = new BatchJobStatus(false);
            for (BatchJob job : copyOfBatchJobs)
            {
                job.setStatus(status);
                if (Thread.currentThread().isInterrupted() || status.isFatalError())
                {
                    shutdownImmediately = true;
                    return;
                }
                futures.put(executorService.submit(job), job);
            }

            // mindegyiket beetettük, lezárjuk az inputot
            executorService.shutdown();

            // amelyik elkészült, megvizsgáljuk az exception állapotot
            boolean thereIsUndone = true;
            int lastReportedCount = 0;
            while (thereIsUndone)
            {
                thereIsUndone = false;
                Iterator<Map.Entry<Future<Void>, BatchJob>> iter = futures.entrySet().iterator();
                lastReportedCount = reportProgress(lastReportedCount, futures.size(), reportEveryNProcessed, name);
                while (iter.hasNext())
                {
                    Map.Entry<Future<Void>, BatchJob> mapEntry = iter.next();
                    Future<Void> future = mapEntry.getKey();

                    if (Thread.currentThread().isInterrupted())
                    {
                        shutdownImmediately = true;
                        return;
                    }

                    if (future.isDone())
                    {
                        iter.remove();

                        // Calling get to see if there was an exception
                        try
                        {
                            future.get();
                        }
                        catch (ExecutionException | InterruptedException ex)
                        {
                            log.error(StackTracer.toString(ex));
                            // meg kell nézni, hogy fatalis hiba történt-e, ami az egész batch feldolgozását meg kell, hogy állítsa?
                            if (ex instanceof ExecutionException ee)
                            {
                                if (ex.getCause() == null || mapEntry.getValue().isFatalException(ee.getCause()))
                                {
                                    // Ha fatális hiba
                                    shutdownImmediately = true;
                                    throw ex;
                                }
                            }
                            else
                            {
                                throw ex;
                            }
                        }
                    }
                    else
                    {
                        thereIsUndone = true;
                    }
                }

                if (thereIsUndone)
                {
                    TimeUnit.MILLISECONDS.sleep(200);
                }
            }
        }
        catch (InterruptedException ex)
        {
            log.warn(ex.toString());
            shutdownImmediately = true;
            throw ex;
        }
        finally
        {
            if (shutdownImmediately)
            {
                List<Runnable> runnables = executorService.shutdownNow();
                log.info("Fatal error or interrupted, cancelling {} threads immediately!", runnables.size());
            }

            // meg kell várni, míg mindegyik elkészül
            while (!executorService.isTerminated())
            {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
                log.info("waiting for termination...");
            }

            log.debug("Processing done.");
        }
    }


    private static int reportProgress(int lastReportedCount, int countRemaining, Integer reportEveryNProcessed, String name)
    {
        if (reportEveryNProcessed != null)
        {
            if (lastReportedCount == 0 || lastReportedCount > countRemaining + reportEveryNProcessed)
            {
                log.info("{} - count of remaining tasks: {}", name, countRemaining);
                return countRemaining;
            }
        }
        return lastReportedCount;
    }


    protected ExecutorService createExecutorService()
    {
        return Executors.newFixedThreadPool(threadPoolSize);
    }
}
