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

package hu.perit.spvitamin.core.typehelpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A utility class for working with Java Maps and Collections.
 * 
 * <p>This class provides methods for converting collections to maps and performing
 * grouping operations with functional programming techniques. It simplifies common
 * map operations using Java Stream API and handles null collections gracefully.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Convert collections to maps using key extraction functions</li>
 *   <li>Group collection elements by a key into sets</li>
 *   <li>Group and transform collection elements in a single operation</li>
 *   <li>Null-safe operations (empty maps returned for null collections)</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Convert a collection to a map
 * List&lt;Person&gt; people = List.of(new Person(1, "Alice"), new Person(2, "Bob"));
 * Map&lt;Integer, Person&gt; peopleById = MapUtils.toMap(people, Person::getId);
 * 
 * // Group people by their city
 * Map&lt;String, Set&lt;Person&gt;&gt; peopleByCity = MapUtils.groupBy(people, Person::getCity);
 * 
 * // Group and transform: get names of people by city
 * Map&lt;String, Set&lt;String&gt;&gt; namesByCity = MapUtils.groupBy(people, Person::getCity, Person::getName);
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapUtils
{
    public static <K, V> Map<K, V> toMap(Collection<V> collection, Function<V, K> keySupplier)
    {
        if (collection == null)
        {
            return Map.of();
        }

        return collection.stream().collect(Collectors.toMap(keySupplier, v -> v));
    }


    public static <K, V> Map<K, Set<V>> groupBy(Collection<V> collection, Function<V, K> keySupplier)
    {
        if (collection == null)
        {
            return Map.of();
        }

        return collection.stream().collect(Collectors.groupingBy(keySupplier, Collectors.toSet()));
    }


    public static <K, V, V2> Map<K, Set<V2>> groupBy(Collection<V> collection, Function<V, K> keySupplier, Function<V, V2> valueSupplier)
    {
        if (collection == null)
        {
            return Map.of();
        }

        return collection.stream()
                .collect(Collectors.groupingBy(
                        keySupplier,
                        Collectors.mapping(valueSupplier, Collectors.toSet())
                ));
    }
}
