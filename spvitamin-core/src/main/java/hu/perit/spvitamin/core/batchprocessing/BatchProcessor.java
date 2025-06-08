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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A parallel batch processing framework that executes a list of {@link BatchJob} instances using a thread pool.
 * 
 * <p>This class provides functionality to process batch jobs in parallel with configurable thread pool size.
 * It supports running the first job synchronously to verify connectivity before processing the rest in parallel.
 * The processor handles both fatal and non-fatal exceptions, where fatal exceptions will stop the entire batch
 * processing while non-fatal exceptions allow the processing to continue.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Configurable thread pool size</li>
 *   <li>Optional synchronous execution of the first job</li>
 *   <li>Progress reporting</li>
 *   <li>Distinction between fatal and non-fatal exceptions</li>
 *   <li>Graceful shutdown on fatal errors</li>
 * </ul>
 * 
 * <p>Concrete implementations must provide the {@link #createExecutorService()} method to define
 * how the thread pool is created.</p>
 * 
 * @author Peter Nagy
 */

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class BatchProcessor
{
    protected final int threadPoolSize;

    /**
     * Process a list of batch jobs with default settings.
     * The first job will be executed synchronously, and no progress reporting will be done.
     *
     * @param batchJobs the list of batch jobs to process
     * @throws ExecutionException if a fatal exception occurs during processing
     * @throws InterruptedException if the thread is interrupted during processing
     */
    public void process(List<? extends BatchJob> batchJobs) throws ExecutionException, InterruptedException
    {
        process(batchJobs, true, null, null);
    }

    /**
     * Process a list of batch jobs with optional synchronous execution of the first job.
     * No progress reporting will be done.
     *
     * @param batchJobs the list of batch jobs to process
     * @param runFirstSynchronously whether to run the first job synchronously
     * @throws ExecutionException if a fatal exception occurs during processing
     * @throws InterruptedException if the thread is interrupted during processing
     */
    public void process(List<? extends BatchJob> batchJobs, boolean runFirstSynchronously) throws ExecutionException, InterruptedException
    {
        process(batchJobs, runFirstSynchronously, null, null);
    }

    /**
     * Process a list of batch jobs with full configuration options.
     * This is the main processing method that handles the execution of batch jobs.
     *
     * @param batchJobs the list of batch jobs to process
     * @param runFirstSynchronously whether to run the first job synchronously
     * @param reportEveryNProcessed if not null, progress will be reported every N processed jobs
     * @param name optional name for the batch process, used in progress reporting
     * @throws ExecutionException if a fatal exception occurs during processing
     * @throws InterruptedException if the thread is interrupted during processing
     */
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
                // Check if this is a fatal exception that should stop the entire batch processing
                if (firstJob.isFatalException(ex))
                {
                    // If it's a fatal error, propagate it
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

            // All jobs have been submitted, shutdown the input
            executorService.shutdown();

            // For completed jobs, check their exception status
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
                            // Check if this is a fatal exception that should stop the entire batch processing
                            if (ex instanceof ExecutionException ee)
                            {
                                if (ex.getCause() == null || mapEntry.getValue().isFatalException(ee.getCause()))
                                {
                                    // If it's a fatal error, shutdown immediately and propagate the exception
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

            // Wait for all tasks to complete
            while (!executorService.isTerminated())
            {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
                log.info("waiting for termination...");
            }

            log.debug("Processing done.");
        }
    }


    /**
     * Reports progress of the batch processing if the reportEveryNProcessed parameter is set.
     *
     * @param lastReportedCount the count at which progress was last reported
     * @param countRemaining the current count of remaining tasks
     * @param reportEveryNProcessed how often to report progress (null means no reporting)
     * @param name the name of the batch process
     * @return the new lastReportedCount value
     */
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

    /**
     * Creates the ExecutorService used for parallel processing.
     * This method must be implemented by concrete subclasses.
     *
     * @return the ExecutorService to use for parallel processing
     */
    protected ExecutorService createExecutorService()
    {
        return Executors.newFixedThreadPool(threadPoolSize);
    }
}
