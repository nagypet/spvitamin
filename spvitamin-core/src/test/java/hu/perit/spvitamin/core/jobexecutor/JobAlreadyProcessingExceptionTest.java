package hu.perit.spvitamin.core.jobexecutor;

import hu.perit.spvitamin.core.exception.InputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobAlreadyProcessingExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String errorMessage = "Job is already processing";
        
        // Act
        JobAlreadyProcessingException exception = new JobAlreadyProcessingException(errorMessage);
        
        // Assert
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception).isInstanceOf(InputException.class);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Arrange
        String errorMessage = "Job is already processing";
        Throwable cause = new RuntimeException("Original cause");
        
        // Act
        JobAlreadyProcessingException exception = new JobAlreadyProcessingException(errorMessage, cause);
        
        // Assert
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(InputException.class);
    }
}
