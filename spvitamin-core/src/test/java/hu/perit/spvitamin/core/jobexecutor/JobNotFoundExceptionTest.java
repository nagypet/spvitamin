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

import hu.perit.spvitamin.core.exception.InputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String errorMessage = "Job not found";
        
        // Act
        JobNotFoundException exception = new JobNotFoundException(errorMessage);
        
        // Assert
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception).isInstanceOf(InputException.class);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Arrange
        String errorMessage = "Job not found";
        Throwable cause = new RuntimeException("Original cause");
        
        // Act
        JobNotFoundException exception = new JobNotFoundException(errorMessage, cause);
        
        // Assert
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(InputException.class);
    }
}
