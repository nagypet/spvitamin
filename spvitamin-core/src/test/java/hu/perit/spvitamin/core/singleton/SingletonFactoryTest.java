package hu.perit.spvitamin.core.singleton;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@Slf4j
class SingletonFactoryTest
{
    private static class Resource
    {
        private static final AtomicInteger instanceCount = new AtomicInteger(0);


        public Resource()
        {
            instanceCount.incrementAndGet();
            OffsetDateTime created = OffsetDateTime.now();
            log.debug("Resource({}) created", created);
        }


        public static int getInstanceCount()
        {
            return instanceCount.get();
        }


        public static void resetInstanceCount()
        {
            instanceCount.set(0);
        }
    }


    @Test
    void test()
    {
        log.debug("test()");
        SingletonFactory<Resource> singletonFactory = SingletonFactory.of(Resource::new);
        log.debug(singletonFactory.getInstance().toString());
        log.debug(singletonFactory.getInstance().toString());
    }


    @Test
    void testThreadSafety() throws InterruptedException
    {
        log.debug("testThreadSafety()");

        // Create a singleton and initialize it
        final SingletonFactory<Resource> singletonFactory = SingletonFactory.of(Resource::new);

        // Get the resource once to initialize it
        Resource initialResource = singletonFactory.getInstance();
        log.debug("Initial resource created: {}", initialResource);

        // Reset the instance count after initial creation
        Resource.resetInstanceCount();

        // Number of threads to use in the thread pool
        final int threadCount = 100;

        // Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // Create an atomic counter to track the number of tasks executed
        AtomicInteger tasksExecuted = new AtomicInteger(0);

        // Set the duration for the test to run (5 seconds)
        final long testDurationMillis = 5000;
        final long startTime = System.currentTimeMillis();

        log.debug("Starting continuous test for {} seconds", testDurationMillis / 1000);

        // Run continuously for 5 seconds
        while (System.currentTimeMillis() - startTime < testDurationMillis)
        {
            final int taskIndex = tasksExecuted.incrementAndGet();

            executorService.submit(() -> {
                try
                {
                    // Get the resource from the singleton
                    Resource resource = singletonFactory.getInstance();

                    if (taskIndex % 1000 == 0)
                    {
                        log.debug("Task {} got resource: {}", taskIndex, resource);
                    }

                    // Verify that the resource is the same as the initial resource
                    assertSame(initialResource, resource, "Task " + taskIndex + " should get the same resource instance");
                }
                catch (Exception e)
                {
                    log.error("Error in task {}: {}", taskIndex, e.getMessage(), e);
                }
            });

            // Small sleep to prevent overwhelming the system
            if (taskIndex % 1000 == 0)
            {
                Thread.sleep(1);
            }
        }

        log.debug("Test duration of {} seconds completed. Tasks executed: {}", testDurationMillis / 1000, tasksExecuted.get());

        // Shutdown the executor service
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Verify that no new instances of Resource were created
        assertEquals(0, Resource.getInstanceCount(), "No new instances of Resource should be created");

        log.debug("All threads completed, instance count: {}, tasks executed: {}", Resource.getInstanceCount(), tasksExecuted.get());
    }
}
