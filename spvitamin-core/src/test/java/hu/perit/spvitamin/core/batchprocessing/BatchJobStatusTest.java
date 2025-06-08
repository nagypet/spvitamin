package hu.perit.spvitamin.core.batchprocessing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatchJobStatusTest
{

    @Test
    void testConstructorWithFalse()
    {
        // Act
        BatchJobStatus status = new BatchJobStatus(false);

        // Assert
        assertThat(status.isFatalError()).isFalse();
    }


    @Test
    void testConstructorWithTrue()
    {
        // Act
        BatchJobStatus status = new BatchJobStatus(true);

        // Assert
        assertThat(status.isFatalError()).isTrue();
    }


    @Test
    void testSetFatalError()
    {
        // Arrange
        BatchJobStatus status = new BatchJobStatus(false);

        // Act
        status.setFatalError(true);

        // Assert
        assertThat(status.isFatalError()).isTrue();
    }


    @Test
    void testSetFatalErrorToFalse()
    {
        // Arrange
        BatchJobStatus status = new BatchJobStatus(true);

        // Act
        status.setFatalError(false);

        // Assert
        assertThat(status.isFatalError()).isFalse();
    }
}
