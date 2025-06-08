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

import java.util.List;

/**
 * A utility class providing helper methods for working with Java Lists.
 * 
 * <p>This class offers convenience methods that handle common List operations
 * with built-in null safety. It simplifies code by eliminating the need for
 * repetitive null checks when working with lists.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Null-safe access to the last element of a list</li>
 *   <li>Null-safe size calculation</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Get the last element safely (returns null for empty or null lists)
 * List&lt;String&gt; names = Arrays.asList("Alice", "Bob", "Charlie");
 * String lastName = ListUtils.last(names); // "Charlie"
 * 
 * // Get size safely (returns 0 for null lists)
 * int count = ListUtils.size(null); // 0
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ListUtils
{
    public static <T> T last(List<T> list)
    {
        return list == null || list.isEmpty() ? null : list.get(list.size() - 1);
    }


    public static int size(List<?> list)
    {
        if (list == null)
        {
            return 0;
        }
        return list.size();
    }
}
