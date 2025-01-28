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

package hu.perit.spvitamin.core.connectablecontext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Supplier;


/**
 * @author Peter Nagy
 */


@Slf4j
public class ConnectableContextCollection<T extends ConnectableContext> extends HashMap<ContextKey, T>
{
    @Getter
    private String contextTypeName = "ConnectableContext";

    private final Supplier<T> contextSupplier; // NOSONAR: this class is not serializable


    public ConnectableContextCollection(Supplier<T> supplier)
    {
        this.contextSupplier = supplier;
    }


    public T get(ContextKey key)
    {
        T value = super.get(key);
        if (value == null)
        {
            T newValue = this.contextSupplier.get();
            this.contextTypeName = newValue.getClass().getSimpleName();
            super.put(key, newValue);
            return newValue;
        }
        else
        {
            return value;
        }
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (this.getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        ConnectableContextCollection<?> that = (ConnectableContextCollection<?>) o;
        return Objects.equals(contextSupplier, that.contextSupplier);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), contextSupplier);
    }


    // prevent from serialization
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        throw new NotSerializableException();
    }


    // prevent from serialization
    @Serial
    private void readObject(ObjectInputStream in) throws IOException
    {
        throw new NotSerializableException();
    }
}
