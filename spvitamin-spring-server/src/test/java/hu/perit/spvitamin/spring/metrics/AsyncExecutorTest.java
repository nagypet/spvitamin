
package hu.perit.spvitamin.spring.metrics;

import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
class AsyncExecutorTest
{

    @Test
    void invoke_shouldReturnResult_whenFinishedWithinTimeout() throws TimeoutException
    {
        // Given
        String expected = "success";

        // When
        String result = AsyncExecutor.invoke(() -> {
            return expected;
        }, "error", Duration.ofMillis(500));

        // Then
        assertThat(result).isEqualTo(expected);
    }


    @Test
    void invoke_shouldThrowTimeoutException_whenExecutionIsTooLong()
    {
        // When & Then
        assertThatThrownBy(() -> AsyncExecutor.invoke(() -> {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            return "too late";
        }, "fallback", Duration.ofMillis(200))).isInstanceOf(TimeoutException.class);
    }


    @Test
    void invoke_shouldReturnFallback_whenExecutionThrowsException() throws TimeoutException
    {
        // When
        String result = AsyncExecutor.invoke(() -> {
            throw new RuntimeException("Unexpected error");
        }, "fallback", Duration.ofMillis(500));

        // Then
        assertThat(result).isEqualTo("fallback");
    }


    @Test
    void invokeVoid_shouldExecuteSuccessfully_withinTimeout() throws Exception
    {
        // Given
        AtomicBoolean executed = new AtomicBoolean(false);

        // When
        AsyncExecutor.invokeVoid(() -> {
            executed.set(true);
        }, Duration.ofMillis(500));

        // Then
        assertThat(executed.get()).isTrue();
    }


    @Test
    void invokeVoid_shouldThrowTimeoutException_onTimeout() throws Exception
    {
        // When & Then
        try
        {
            AsyncExecutor.invokeVoid(() -> {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }, Duration.ofMillis(200));
            fail("Expected TimeoutException");
        }
        catch (TimeoutException e)
        {
            log.warn(StackTracer.toString(e));
        }
    }
}
