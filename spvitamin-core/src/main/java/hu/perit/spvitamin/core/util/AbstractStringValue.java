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

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;

public abstract class AbstractStringValue<T extends AbstractStringValue<T>> implements Comparable<T>, Serializable
{
    protected final String value;


    protected AbstractStringValue(String value)
    {
        this.value = value;
    }


    @JsonValue
    public final String value()
    {
        return value;
    }


    /**
     * Default ordering: lexicographical by the normalized string value.
     * Subclasses can override if they need different semantics (e.g. numeric compare).
     */
    @Override
    public int compareTo(T other)
    {
        Objects.requireNonNull(other, "other must not be null");
        return this.value.compareTo(other.value());
    }


    @Override
    public final String toString()
    {
        return value;
    }


    @Override
    public final boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false; // type-safe equals
        }
        AbstractStringValue<?> that = (AbstractStringValue<?>) o;
        return value.equals(that.value);
    }


    @Override
    public final int hashCode()
    {
        return Objects.hash(getClass(), value);
    }
}
