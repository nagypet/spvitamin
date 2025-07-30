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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class CancelableJobExecutorTest
{

    private CancelableJobExecutor<String> jobExecutor;
    private static final String TEST_CONTEXT = "TestContext";


    @BeforeEach
    void setUp()
    {
        jobExecutor = new CancelableJobExecutor<>(2, TEST_CONTEXT);
    }


    @Test
    void testSubmitJob() throws ExecutionException, InterruptedException
    {
        log.debug("--- testSubmitJob");

        // Arrange
        String jobId = "job1";
        CountDownLatch jobExecuted = new CountDownLatch(1);

        Callable<Void> job = () -> {
            jobExecuted.countDown();
            return null;
        };

        // Act
        Future<Void> future = jobExecutor.submitJob(jobId, job);
        future.get(); // Wait for job to complete

        // Wait for job to be removed from executor
        // The job is removed asynchronously in the afterExecute method
        Thread.sleep(200);

        // Assert
        assertTrue(jobExecuted.await(0, TimeUnit.MILLISECONDS), "Job was not executed");
        assertThat(jobExecutor.countAll()).isEqualTo(0); // Job should be removed after completion
    }


    @Test
    void testSubmitJobAlreadyProcessing()
    {
        log.debug("--- testSubmitJobAlreadyProcessing");

        // Arrange
        String jobId = "job1";
        AtomicBoolean jobBlocked = new AtomicBoolean(true);

        Callable<Void> job = () -> {
            while (jobBlocked.get())
            {
                Thread.sleep(10);
            }
            return null;
        };

        // Act & Assert
        jobExecutor.submitJob(jobId, job);

        // Try to submit the same job ID again
        assertThrows(JobAlreadyProcessingException.class, () -> {
            jobExecutor.submitJob(jobId, () -> null);
        });

        // Cleanup
        jobBlocked.set(false);
    }


    @Test
    void testCancelJob() throws InterruptedException
    {
        log.debug("--- testCancelJob");

        // Arrange
        String jobId = "job1";
        CountDownLatch jobStarted = new CountDownLatch(1);
        CountDownLatch jobCancelled = new CountDownLatch(1);

        Callable<Void> job = () -> {
            try
            {
                jobStarted.countDown();
                // Long-running task
                while (!Thread.currentThread().isInterrupted())
                {
                    Thread.sleep(50);
                }
            }
            catch (InterruptedException e)
            {
                log.debug("Job {} was cancelled", jobId);
                jobCancelled.countDown();
                throw e;
            }
            return null;
        };

        // Act
        jobExecutor.submitJob(jobId, job);

        // Wait for job to start
        assertTrue(jobStarted.await(1, TimeUnit.SECONDS), "Job did not start within timeout");

        // Make sure job is in RUNNING state
        Thread.sleep(100);

        // Cancel the job
        boolean result = jobExecutor.cancelJob(jobId);

        // Wait for cancellation to take effect
        boolean cancelled = jobCancelled.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(result).isTrue();
        assertThat(cancelled).isTrue();

        // Wait for job to be removed from executor
        Thread.sleep(100);
        assertThat(jobExecutor.countAll()).isZero(); // Job should be removed after cancellation
    }


    @Test
    void testCancelNonExistentJob()
    {
        log.debug("--- testCancelNonExistentJob");

        // Act
        boolean result = jobExecutor.cancelJob("nonExistentJob");

        // Assert
        assertThat(result).isFalse();
    }


    @Test
    void testCancelAll() throws InterruptedException
    {
        log.debug("--- testCancelAll");

        // Arrange
        int jobCount = 2;
        CountDownLatch allJobsStarted = new CountDownLatch(jobCount);
        CountDownLatch allJobsCancelled = new CountDownLatch(jobCount);

        for (int i = 0; i < jobCount; i++)
        {
            final String jobId = "job" + i;
            Callable<Void> job = () -> {
                try
                {
                    allJobsStarted.countDown();
                    // Long-running task
                    while (!Thread.currentThread().isInterrupted())
                    {
                        Thread.sleep(50);
                    }
                }
                catch (InterruptedException e)
                {
                    log.debug("Job {} was cancelled", jobId);
                    allJobsCancelled.countDown();
                    throw e;
                }
                return null;
            };

            jobExecutor.submitJob(jobId, job);
        }

        // Wait for all jobs to start
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertEquals(0, allJobsStarted.getCount(), "Jobs did not start within timeout"));

        jobExecutor.cancelAll();

        // Wait for cancellation to take effect
        await().atMost(10, TimeUnit.SECONDS).until(() -> allJobsCancelled.getCount() == 0);
        assertThat(allJobsCancelled.getCount()).isZero();

        // Wait for jobs to be removed from executor
        await().atMost(10, TimeUnit.SECONDS).until(() -> jobExecutor.countAll() == 0);
        assertThat(jobExecutor.countAll()).isZero();
    }


    @Test
    void testCountRunning() throws InterruptedException
    {
        log.debug("--- testCountRunning");

        // Arrange
        CountDownLatch jobStarted = new CountDownLatch(1);
        AtomicBoolean jobBlocked = new AtomicBoolean(true);

        Callable<Void> job = () -> {
            jobStarted.countDown();
            while (jobBlocked.get())
            {
                Thread.sleep(10);
            }
            return null;
        };

        // Act
        jobExecutor.submitJob("job1", job);

        // Wait for job to start
        jobStarted.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(jobExecutor.countRunning()).isEqualTo(1);

        // Cleanup
        jobBlocked.set(false);
    }


    @Test
    void testCountAll()
    {
        log.debug("--- testCountAll");

        // Arrange
        AtomicBoolean jobBlocked = new AtomicBoolean(true);

        Callable<Void> job = () -> {
            while (jobBlocked.get())
            {
                Thread.sleep(10);
            }
            return null;
        };

        // Act
        jobExecutor.submitJob("job1", job);
        jobExecutor.submitJob("job2", job);

        // Assert
        assertThat(jobExecutor.countAll()).isEqualTo(2);

        // Cleanup
        jobBlocked.set(false);
    }


    @Test
    void testJobWithException() throws InterruptedException
    {
        log.debug("--- testJobWithException");

        // Arrange
        String jobId = "job1";
        CountDownLatch jobStarted = new CountDownLatch(1);

        Callable<Void> job = () -> {
            jobStarted.countDown();
            throw new RuntimeException("Test exception");
        };

        // Act
        Future<Void> future = jobExecutor.submitJob(jobId, job);

        // Wait for job to start and complete
        jobStarted.await(1, TimeUnit.SECONDS);
        Thread.sleep(100);

        // Assert
        assertThat(future.isDone()).isTrue();
        assertThrows(ExecutionException.class, future::get);
        assertThat(jobExecutor.countAll()).isEqualTo(0); // Job should be removed after exception
    }


    @Test
    void testRuntimeExceptionInFutureGet() throws Exception
    {
        log.debug("--- testRuntimeExceptionInFutureGet");

        // Arrange
        String jobId = "job1";
        CountDownLatch jobExecuted = new CountDownLatch(1);

        // Create a job that completes normally
        Callable<Void> job = () -> {
            jobExecuted.countDown();
            return null;
        };

        // Submit the job
        Future<Void> future = jobExecutor.submitJob(jobId, job);

        // Wait for the job to complete
        assertTrue(jobExecuted.await(1, TimeUnit.SECONDS), "Job was not executed");

        // Replace the real future with our custom future that throws RuntimeException on get()
        Field futureMapField = CancelableJobExecutor.class.getDeclaredField("futureMap");
        futureMapField.setAccessible(true);
        FutureMap<String> futureMap = (FutureMap<String>) futureMapField.get(jobExecutor);

        // Create a custom future that throws RuntimeException when get() is called
        // It also implements Runnable so it can be passed to afterExecute
        RunnableFuture badFuture = new RunnableFuture();

        // Replace the future in the map
        futureMap.remove(jobId);
        futureMap.put(jobId, badFuture);

        // Manually trigger afterExecute to simulate the ThreadPoolExecutor calling it
        jobExecutor.getClass().getDeclaredMethod("afterExecute", Runnable.class, Throwable.class)
                .invoke(jobExecutor, badFuture, null);

        // Wait for afterExecute to complete
        Thread.sleep(100);

        // Assert that the future was removed from the map despite the RuntimeException
        assertThat(jobExecutor.countAll()).isEqualTo(0);
    }


    // Custom class that implements both Runnable and Future interfaces
    private static class RunnableFuture implements Runnable, Future<Void>
    {
        @Override
        public void run()
        {
            // Do nothing
        }


        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            return false;
        }


        @Override
        public boolean isCancelled()
        {
            return false;
        }


        @Override
        public boolean isDone()
        {
            return true;
        }


        @Override
        public Void get()
        {
            throw new RuntimeException("Test RuntimeException from future.get()");
        }


        @Override
        public Void get(long timeout, TimeUnit unit)
        {
            throw new RuntimeException("Test RuntimeException from future.get()");
        }
    }
}
