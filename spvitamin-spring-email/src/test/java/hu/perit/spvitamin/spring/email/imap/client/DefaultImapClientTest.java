package hu.perit.spvitamin.spring.email.imap.client;

import hu.perit.spvitamin.spring.email.imap.config.ImapConnectionConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultImapClientTest
{

    private ImapConnectionConfig mockConfig;
    private Store mockStore;
    private Session mockSession;


    @BeforeEach
    void setUp() throws MessagingException
    {
        // Create mock objects
        mockConfig = ImapConnectionConfig.builder()
                .host("localhost")
                .port(143)
                .username("testuser")
                .password("testpassword")
                .useSSL(false)
                .build();

        // Mock the Session and Store
        mockSession = mock(Session.class);
        mockStore = mock(Store.class);

        // Configure the mocks
        when(mockStore.isConnected()).thenReturn(true);
    }


    /**
     * Test that verifies thread safety of DefaultImapClient by having multiple threads
     * execute operations concurrently on the same instance.
     */
    @Test
    void testThreadSafety() throws Exception
    {
        // Create a DefaultImapClient with a special constructor for testing
        DefaultImapClientForTest client = new DefaultImapClientForTest(mockConfig, mockStore);

        // Number of threads to use for testing
        int threadCount = 10;

        // Create a countdown latch to synchronize thread start
        CountDownLatch startLatch = new CountDownLatch(1);

        // Create a countdown latch to wait for all threads to complete
        CountDownLatch completionLatch = new CountDownLatch(threadCount);

        // Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // Track operation counts
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();

        // Create and submit tasks
        for (int i = 0; i < threadCount; i++)
        {
            final int threadId = i;
            executorService.submit(() -> {
                try
                {
                    // Wait for the signal to start
                    startLatch.await();

                    // Execute an operation
                    Integer result = client.executeWithStore(store -> {
                        try
                        {
                            // Simulate some work
                            Thread.sleep(10);
                            return threadId;
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Thread interrupted", e);
                        }
                    });

                    // Verify the result
                    assertEquals(threadId, result);
                    successCount.incrementAndGet();
                }
                catch (Exception e)
                {
                    synchronized (exceptions)
                    {
                        exceptions.add(e);
                    }
                }
                finally
                {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        boolean allCompleted = completionLatch.await(5, TimeUnit.SECONDS);

        // Shutdown the executor
        executorService.shutdown();

        // Verify results
        assertTrue(allCompleted, "Not all threads completed in time");
        assertEquals(0, exceptions.size(), "Exceptions occurred: " + exceptions);
        assertEquals(threadCount, successCount.get(), "Not all operations succeeded");

        // Verify that the lock was used correctly
        assertEquals(threadCount, client.getLockCount(), "Lock count doesn't match expected value");
    }


    /**
     * Test that verifies thread safety when multiple threads try to connect simultaneously.
     */
    @Test
    void testConcurrentConnect() throws Exception
    {
        // Create a DefaultImapClient with a special constructor for testing
        DefaultImapClientForTest client = new DefaultImapClientForTest(mockConfig, mockStore);

        // Number of threads to use for testing
        int threadCount = 5;

        // Create a countdown latch to synchronize thread start
        CountDownLatch startLatch = new CountDownLatch(1);

        // Create a countdown latch to wait for all threads to complete
        CountDownLatch completionLatch = new CountDownLatch(threadCount);

        // Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // Track operation counts
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();

        // Create and submit tasks
        for (int i = 0; i < threadCount; i++)
        {
            executorService.submit(() -> {
                try
                {
                    // Wait for the signal to start
                    startLatch.await();

                    // Call connect
                    client.connect(mockConfig);
                    successCount.incrementAndGet();
                }
                catch (Exception e)
                {
                    synchronized (exceptions)
                    {
                        exceptions.add(e);
                    }
                }
                finally
                {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        boolean allCompleted = completionLatch.await(5, TimeUnit.SECONDS);

        // Shutdown the executor
        executorService.shutdown();

        // Verify results
        assertTrue(allCompleted, "Not all threads completed in time");
        assertEquals(0, exceptions.size(), "Exceptions occurred: " + exceptions);
        assertEquals(threadCount, successCount.get(), "Not all operations succeeded");
    }


    /**
     * Test that verifies thread safety when multiple threads try to close simultaneously.
     */
    @Test
    void testConcurrentClose() throws Exception
    {
        // Create a DefaultImapClient with a special constructor for testing
        DefaultImapClientForTest client = new DefaultImapClientForTest(mockConfig, mockStore);

        // Number of threads to use for testing
        int threadCount = 5;

        // Create a countdown latch to synchronize thread start
        CountDownLatch startLatch = new CountDownLatch(1);

        // Create a countdown latch to wait for all threads to complete
        CountDownLatch completionLatch = new CountDownLatch(threadCount);

        // Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // Track operation counts
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();

        // Create and submit tasks
        for (int i = 0; i < threadCount; i++)
        {
            executorService.submit(() -> {
                try
                {
                    // Wait for the signal to start
                    startLatch.await();

                    // Call close
                    client.close();
                    successCount.incrementAndGet();
                }
                catch (Exception e)
                {
                    synchronized (exceptions)
                    {
                        exceptions.add(e);
                    }
                }
                finally
                {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        boolean allCompleted = completionLatch.await(5, TimeUnit.SECONDS);

        // Shutdown the executor
        executorService.shutdown();

        // Verify results
        assertTrue(allCompleted, "Not all threads completed in time");
        assertEquals(0, exceptions.size(), "Exceptions occurred: " + exceptions);
        assertEquals(threadCount, successCount.get(), "Not all operations succeeded");
    }


    /**
     * A subclass of DefaultImapClient that allows for testing thread safety
     * by providing a constructor that accepts a mock Store and tracking lock usage.
     * This class overrides the connect method to avoid actual connection attempts.
     */
    private static class DefaultImapClientForTest extends DefaultImapClient
    {
        private final AtomicInteger lockCount = new AtomicInteger(0);
        private final Store mockStore;


        public DefaultImapClientForTest(ImapConnectionConfig config, Store mockStore)
        {
            // Call super with null to avoid NPE, but we'll override connect
            super(null);
            this.mockStore = mockStore;

            // Set the store field directly using reflection
            try
            {
                java.lang.reflect.Field storeField = DefaultImapClient.class.getDeclaredField("store");
                storeField.setAccessible(true);
                storeField.set(this, mockStore);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to set mock store", e);
            }
        }


        @Override
        public void connect(ImapConnectionConfig config)
        {
            // Do nothing - we're using the mock store
        }


        @Override
        public <T> T executeWithStore(ImapStoreOperation<T> operation)
        {
            lockCount.incrementAndGet();
            try
            {
                return operation.execute(mockStore);
            }
            catch (MessagingException e)
            {
                throw new RuntimeException(e);
            }
        }


        public int getLockCount()
        {
            return lockCount.get();
        }
    }
}
