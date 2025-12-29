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

package hu.perit.spvitamin.spring.objectprovider;

import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.stream.Stream;

public class StaticObjectProvider<T> implements ObjectProvider<T>
{
    private final List<T> objects;


    private StaticObjectProvider(T object)
    {
        this.objects = List.of(object);
    }


    private StaticObjectProvider(List<T> objects)
    {
        this.objects = objects;
    }


    public static <T> ObjectProvider<T> of(T object)
    {
        return new StaticObjectProvider<>(object);
    }


    public static <T> ObjectProvider<T> of(List<T> objectList)
    {
        return new StaticObjectProvider<T>(objectList);
    }


    @Override
    public Stream<T> stream()
    {
        return this.objects.stream();
    }
}
