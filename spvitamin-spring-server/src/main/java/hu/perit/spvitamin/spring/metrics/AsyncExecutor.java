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

package hu.perit.spvitamin.spring.metrics;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.exception.ThrowingRunnable;
import hu.perit.spvitamin.spring.config.SysConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AsyncExecutor
{

    public static <T> T invoke(Supplier<T> supplier, T returnValueOnError) throws TimeoutException
    {
        return invoke(supplier, returnValueOnError, Duration.ofMillis(SysConfig.getMetricsProperties().getTimeoutMillis()));
    }


    public static <T> T invoke(Supplier<T> supplier, T returnValueOnError, Duration timeout) throws TimeoutException
    {
        CompletableFuture<T> completableFuture = null;
        AtomicReference<Thread> asyncThread = new AtomicReference<>();
        try
        {
            // Capture the current thread's logging context (Log4j ThreadContext)
            final var parentThreadContext = ThreadContext.getContext();

            completableFuture = CompletableFuture.supplyAsync(() -> {
                asyncThread.set(Thread.currentThread());
                try
                {
                    // Propagate the parent thread's context to the async thread
                    if (parentThreadContext != null && !parentThreadContext.isEmpty())
                    {
                        ThreadContext.putAll(parentThreadContext);
                    }

                    return supplier.get();
                }
                finally
                {
                    // Clean up the ThreadContext in the async thread to avoid leaking data
                    ThreadContext.clearMap();
                }
            });

            return completableFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (ExecutionException ex)
        {
            log.error(StackTracer.toString(ex));
        }
        catch (TimeoutException ex)
        {
            completableFuture.cancel(true);
            Thread thread = asyncThread.get();
            if (thread != null)
            {
                thread.interrupt();
            }
            throw ex;
        }
        catch (InterruptedException ex)
        {
            log.warn(StackTracer.toString(ex));
            Thread.currentThread().interrupt();
        }

        return returnValueOnError;
    }


    public static void invokeVoid(ThrowingRunnable runnable, Duration timeout) throws Exception
    {
        CompletableFuture<Void> completableFuture = null;
        AtomicReference<Thread> asyncThread = new AtomicReference<>();
        try
        {
            // Capture the current thread's logging context (Log4j ThreadContext)
            final var parentThreadContext = ThreadContext.getContext();

            completableFuture = CompletableFuture.supplyAsync(() -> {
                asyncThread.set(Thread.currentThread());
                try
                {
                    // Propagate the parent thread's context to the async thread
                    if (parentThreadContext != null && !parentThreadContext.isEmpty())
                    {
                        ThreadContext.putAll(parentThreadContext);
                    }

                    runnable.run();
                    return null;
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                finally
                {
                    // Clean up the ThreadContext in the async thread to avoid leaking data
                    ThreadContext.clearMap();
                }
            });
            completableFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (ExecutionException ex)
        {
            if (ex.getCause() instanceof Exception exception)
            {
                throw (Exception) exception.getCause();
            }
            log.error(StackTracer.toString(ex));
        }
        catch (TimeoutException ex)
        {
            completableFuture.cancel(true);
            Thread thread = asyncThread.get();
            if (thread != null)
            {
                thread.interrupt();
            }
            throw ex;
        }
        catch (InterruptedException ex)
        {
            log.warn(StackTracer.toString(ex));
            Thread.currentThread().interrupt();
        }
    }
}
