/*
 * Copyright 2020-2020 the original author or authors.
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

package hu.perit.spvitamin.core.connectablecontext;

import java.util.Objects;

/**
 * @author Peter Nagy
 */


public class GenericContextKey<T> extends ContextKey
{
    protected T keyValue;

    public GenericContextKey() {}

    public GenericContextKey(T keyValue)
    {
        this.keyValue = keyValue;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof GenericContextKey))
        {
            return false;
        }
        T other = (T) ((GenericContextKey) o).keyValue; // NOSONAR
        return this.keyValue.equals(other);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.keyValue);
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "{" +
                "keyValue=" + keyValue +
                '}';
    }
}
