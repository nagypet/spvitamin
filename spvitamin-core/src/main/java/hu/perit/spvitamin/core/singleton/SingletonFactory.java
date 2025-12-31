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

package hu.perit.spvitamin.core.singleton;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Thread-safe lazy initializer using the initialization-on-demand holder idiom.
 *
 * @param <T> The type of the lazily initialized object
 */
@RequiredArgsConstructor
public class SingletonFactory<T>
{
    private final Supplier<T> supplier;
    private final AtomicReference<T> atomicReference = new AtomicReference<>();


    public static <T> SingletonFactory<T> of(Supplier<T> supplier)
    {
        return new SingletonFactory<>(supplier);
    }


    public boolean isNull()
    {
        return atomicReference.get() == null;
    }


    public boolean isNotNull()
    {
        return !isNull();
    }


    public T getInstance()
    {
        T value = atomicReference.get();
        if (value == null)
        {
            T newValue = supplier.get();
            if (atomicReference.compareAndSet(null, newValue))
            {
                value = newValue;
            }
            else
            {
                value = atomicReference.get();
            }
        }
        return value;
    }


    public void renew()
    {
        atomicReference.set(null);
    }
}
