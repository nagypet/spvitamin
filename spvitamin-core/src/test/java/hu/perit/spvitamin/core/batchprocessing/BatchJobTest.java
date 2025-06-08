package hu.perit.spvitamin.core.batchprocessing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BatchJobTest
{

    private TestBatchJob testBatchJob;
    private BatchJobStatus status;


    @BeforeEach
    void setUp()
    {
        status = new BatchJobStatus(false);
        testBatchJob = new TestBatchJob();
        testBatchJob.setStatus(status);
    }


    @Test
    void testCallExecutesSetupAndExecute() throws Exception
    {
        // Act
        testBatchJob.call();

        // Assert
        assertThat(testBatchJob.isSetupCalled()).isTrue();
        assertThat(testBatchJob.isExecuteCalled()).isTrue();
    }


    @Test
    void testCallWithNonFatalException()
    {
        // Arrange
        testBatchJob.setShouldThrowException(true);
        testBatchJob.setFatalException(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            testBatchJob.call();
        });

        assertThat(exception.getMessage()).isEqualTo("Non-fatal error");
        assertThat(status.isFatalError()).isFalse();
    }


    @Test
    void testCallWithFatalException()
    {
        // Arrange
        testBatchJob.setShouldThrowException(true);
        testBatchJob.setFatalException(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            testBatchJob.call();
        });

        assertThat(exception.getMessage()).isEqualTo("Fatal error");
        assertThat(status.isFatalError()).isTrue();
    }


    @Test
    void testCallWithNullPointerException()
    {
        // Arrange
        testBatchJob.setShouldThrowNullPointerException(true);
        testBatchJob.setFatalException(true);

        // Act & Assert
        Exception exception = assertThrows(NullPointerException.class, () -> {
            testBatchJob.call();
        });

        assertThat(status.isFatalError()).isTrue();
    }


    @Test
    void testCallWithInterruptedException()
    {
        // Arrange
        testBatchJob.setShouldThrowInterruptedException(true);

        // Act & Assert
        Exception exception = assertThrows(InterruptedException.class, () -> {
            testBatchJob.call();
        });

        // InterruptedException should not set fatalError flag
        assertThat(status.isFatalError()).isFalse();
    }


    // A concrete implementation of BatchJob for testing
    private static class TestBatchJob extends BatchJob
    {
        private boolean setupCalled = false;
        private boolean executeCalled = false;
        private boolean shouldThrowException = false;
        private boolean shouldThrowNullPointerException = false;
        private boolean shouldThrowInterruptedException = false;
        private boolean fatalException = false;


        @Override
        protected void setUp()
        {
            setupCalled = true;
        }


        @Override
        protected Void execute() throws Exception
        {
            executeCalled = true;
            if (shouldThrowException)
            {
                throw new RuntimeException(fatalException ? "Fatal error" : "Non-fatal error");
            }
            if (shouldThrowNullPointerException)
            {
                throw new NullPointerException("Null pointer exception");
            }
            if (shouldThrowInterruptedException)
            {
                throw new InterruptedException("Interrupted");
            }
            return null;
        }


        @Override
        public boolean isFatalException(Throwable ex)
        {
            return fatalException;
        }


        public boolean isSetupCalled()
        {
            return setupCalled;
        }


        public boolean isExecuteCalled()
        {
            return executeCalled;
        }


        public void setShouldThrowException(boolean shouldThrowException)
        {
            this.shouldThrowException = shouldThrowException;
        }


        public void setShouldThrowNullPointerException(boolean shouldThrowNullPointerException)
        {
            this.shouldThrowNullPointerException = shouldThrowNullPointerException;
        }


        public void setShouldThrowInterruptedException(boolean shouldThrowInterruptedException)
        {
            this.shouldThrowInterruptedException = shouldThrowInterruptedException;
        }


        public void setFatalException(boolean fatalException)
        {
            this.fatalException = fatalException;
        }
    }
}
