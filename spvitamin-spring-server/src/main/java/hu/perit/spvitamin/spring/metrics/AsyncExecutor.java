package hu.perit.spvitamin.spring.metrics;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.config.SysConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncExecutor
{

    public static <T> T invoke(Supplier<T> supplier, T returnValueOnError) throws TimeoutException
    {
        CompletableFuture<T> completableFuture = null;
        try
        {
            completableFuture = CompletableFuture.supplyAsync(supplier);

            return completableFuture.get(SysConfig.getMetricsProperties().getTimeoutMillis(), TimeUnit.MILLISECONDS);
        }
        catch (ExecutionException ex)
        {
            log.error(StackTracer.toString(ex));
        }
        catch (TimeoutException ex)
        {
            completableFuture.cancel(true);
            throw ex;
        }
        catch (InterruptedException ex)
        {
            log.warn(StackTracer.toString(ex));
            Thread.currentThread().interrupt();
        }

        return returnValueOnError;
    }

}
