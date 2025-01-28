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
