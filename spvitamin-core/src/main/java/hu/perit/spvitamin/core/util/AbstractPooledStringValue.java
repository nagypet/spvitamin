/*
 * Copyright 2020-2026 the original author or authors.
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

package hu.perit.spvitamin.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * An {@link AbstractStringValue} subclass that maintains a per-type intern pool, ensuring that
 * two instances wrapping the same (non-null) normalized value are the same object reference.
 * This makes {@code ==} comparison safe, similar to how interned {@link String} literals behave.
 *
 * <p><b>Warning:</b> The pool is unbounded. Only use this class when the set of distinct values
 * is small and well-bounded (e.g. ISO currency codes, country codes). Do NOT use it when values
 * come from unbounded user input — every distinct value is retained indefinitely, which is a
 * memory leak. For such cases, extend {@link AbstractStringValue} directly.
 *
 * <p>Subclasses must expose a {@code public static T of(String)} factory method that delegates
 * to {@link #intern(Class, String, Function)}, passing the already-normalized value as the key:
 * <pre>{@code
 * public static Currency of(String value) {
 *     return intern(Currency.class, validate(value), Currency::new);
 * }
 * }</pre>
 * Using the normalized value as the pool key guarantees that {@code of("huf") == of("HUF")} when
 * both normalize to {@code "HUF"}.
 */
public abstract class AbstractPooledStringValue<T extends AbstractPooledStringValue<T>>
        extends AbstractStringValue<T>
{
    private static final Map<Class<?>, Map<String, ?>> POOL = new ConcurrentHashMap<>();


    protected AbstractPooledStringValue(String value)
    {
        super(value);
    }


    /**
     * Returns a pooled instance for the given normalized value. If an instance with this value
     * already exists in the pool for {@code clazz}, it is returned; otherwise a new instance is
     * created via {@code factory} and added to the pool.
     *
     * <p>{@code null} values bypass the pool — a new instance is created on every call.
     *
     * @param clazz           the concrete subclass token, used as the pool's outer key
     * @param normalizedValue the already-normalized (post-validation) value, or {@code null}
     * @param factory         a constructor reference, e.g. {@code MyType::new}
     */
    @SuppressWarnings("unchecked")
    protected static <T extends AbstractPooledStringValue<T>> T intern(
            Class<T> clazz, String normalizedValue, Function<String, T> factory)
    {
        if (normalizedValue == null)
        {
            return factory.apply(null);
        }
        Map<String, T> classPool =
                (Map<String, T>) POOL.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
        return classPool.computeIfAbsent(normalizedValue, factory);
    }
}
