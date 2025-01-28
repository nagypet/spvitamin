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

package hu.perit.spvitamin.core.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CheckedExceptionConverter
{
    public static <T> T invoke(Callable<T> callable)
    {
        return invoke(callable, ServerException::throwFrom);
    }


    public static <T> T invoke(Callable<T> callable, Function<Exception, T> exceptionHandler)
    {
        try
        {
            return callable.call();
        }
        catch (Exception e)
        {
            return exceptionHandler.apply(e);
        }
    }


    public static void invokeVoid(ThrowingRunnable runnable)
    {
        invokeVoid(runnable, ServerException::throwFrom);
    }


    public static void invokeVoid(ThrowingRunnable runnable, Consumer<Exception> exceptionHandler)
    {
        try
        {
            runnable.run();
        }
        catch (Exception e)
        {
            exceptionHandler.accept(e);
        }
    }
}
