package hu.perit.spvitamin.core.batchprocessing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BatchProcessorTest
{

    private TestBatchProcessor batchProcessor;
    private List<BatchJob> batchJobs;


    @BeforeEach
    void setUp()
    {
        batchProcessor = new TestBatchProcessor(2); // Use 2 threads for testing
        batchJobs = new ArrayList<>();
    }


    @Test
    void testProcessEmptyList() throws ExecutionException, InterruptedException
    {
        // Test with empty list
        batchProcessor.process(new ArrayList<>());
        // No exception should be thrown
    }


    @Test
    void testProcessNullList() throws ExecutionException, InterruptedException
    {
        // Test with null list
        batchProcessor.process(null);
        // No exception should be thrown
    }


    @Test
    void testProcessSingleJob() throws Exception
    {
        // Setup
        TestBatchJob job = new TestBatchJob(false);
        batchJobs.add(job);

        // Execute
        batchProcessor.process(batchJobs);

        // Verify
        assertThat(job.isExecuted()).isTrue();
    }


    @Test
    void testProcessMultipleJobs() throws Exception
    {
        // Setup
        TestBatchJob job1 = new TestBatchJob(false);
        TestBatchJob job2 = new TestBatchJob(false);
        TestBatchJob job3 = new TestBatchJob(false);

        batchJobs.add(job1);
        batchJobs.add(job2);
        batchJobs.add(job3);

        // Execute
        batchProcessor.process(batchJobs);

        // Verify
        assertThat(job1.isExecuted()).isTrue();
        assertThat(job2.isExecuted()).isTrue();
        assertThat(job3.isExecuted()).isTrue();
    }


    @Test
    void testProcessWithFatalException()
    {
        // Setup
        TestBatchJob job1 = new TestBatchJob(true, true); // Will throw fatal exception
        TestBatchJob job2 = new TestBatchJob(false);

        batchJobs.add(job1);
        batchJobs.add(job2);

        // Execute and verify
        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            batchProcessor.process(batchJobs);
        });

        assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("Fatal error");

        // Verify job2 was never executed due to fatal exception in job1
        assertThat(job2.isExecuted()).isFalse();
    }


    @Test
    void testProcessWithNonFatalException() throws Exception
    {
        // Setup
        TestBatchJob job1 = new TestBatchJob(true, false); // Will throw non-fatal exception
        TestBatchJob job2 = new TestBatchJob(false);

        batchJobs.add(job1);
        batchJobs.add(job2);

        // Execute
        batchProcessor.process(batchJobs);

        // Verify job2 was still executed despite exception in job1
        assertThat(job2.isExecuted()).isTrue();
    }


    @Test
    void testProcessWithRunFirstSynchronously() throws Exception
    {
        // Setup
        TestBatchJob job1 = new TestBatchJob(false);
        TestBatchJob job2 = new TestBatchJob(false);

        batchJobs.add(job1);
        batchJobs.add(job2);

        // Execute with runFirstSynchronously = true
        batchProcessor.process(batchJobs, true);

        // Verify
        assertThat(job1.isExecuted()).isTrue();
        assertThat(job2.isExecuted()).isTrue();
    }


    @Test
    void testProcessWithoutRunFirstSynchronously() throws Exception
    {
        // Setup
        TestBatchJob job1 = new TestBatchJob(false);
        TestBatchJob job2 = new TestBatchJob(false);

        batchJobs.add(job1);
        batchJobs.add(job2);

        // Execute with runFirstSynchronously = false
        batchProcessor.process(batchJobs, false);

        // Verify
        assertThat(job1.isExecuted()).isTrue();
        assertThat(job2.isExecuted()).isTrue();
    }


    @Test
    void testProcessWithReporting() throws Exception
    {
        // Setup
        TestBatchJob job1 = new TestBatchJob(false);
        TestBatchJob job2 = new TestBatchJob(false);

        batchJobs.add(job1);
        batchJobs.add(job2);

        // Execute with reporting
        batchProcessor.process(batchJobs, true, 1, "Test Batch");

        // Verify
        assertThat(job1.isExecuted()).isTrue();
        assertThat(job2.isExecuted()).isTrue();
    }


    // A concrete implementation of BatchProcessor for testing
    private static class TestBatchProcessor extends BatchProcessor
    {
        public TestBatchProcessor(int threadPoolSize)
        {
            super(threadPoolSize);
        }


        @Override
        protected ExecutorService createExecutorService()
        {
            return Executors.newFixedThreadPool(threadPoolSize);
        }
    }


    // A concrete implementation of BatchJob for testing
    private static class TestBatchJob extends BatchJob
    {
        private final boolean throwException;
        private final boolean fatalException;
        private boolean executed = false;


        public TestBatchJob(boolean throwException)
        {
            this(throwException, false);
        }


        public TestBatchJob(boolean throwException, boolean fatalException)
        {
            this.throwException = throwException;
            this.fatalException = fatalException;
        }


        @Override
        protected Void execute() throws Exception
        {
            executed = true;
            if (throwException)
            {
                throw new RuntimeException(fatalException ? "Fatal error" : "Non-fatal error");
            }
            return null;
        }


        @Override
        public boolean isFatalException(Throwable ex)
        {
            return fatalException;
        }


        public boolean isExecuted()
        {
            return executed;
        }
    }
}
