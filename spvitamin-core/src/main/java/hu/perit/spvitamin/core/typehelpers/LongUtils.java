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

import java.math.BigInteger;

/**
 * A utility class for working with Long and BigInteger values.
 * 
 * <p>This class provides methods for comparing, checking equality, and performing
 * other common operations on Long and BigInteger values with proper null handling.
 * It simplifies working with numeric types by providing consistent behavior for
 * null values and conversions between different numeric representations.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Null-safe equality checking between Long and BigInteger values</li>
 *   <li>Comparison methods with configurable null handling</li>
 *   <li>Checking if a Long is blank (null or zero)</li>
 *   <li>Safe parsing of strings to Long values</li>
 *   <li>Default value handling for null Longs</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Compare Long values with null handling
 * int result = LongUtils.compare(value1, value2, true); // true = null is less than non-null
 * 
 * // Check if a Long is blank (null or zero)
 * boolean isBlank = LongUtils.isBlank(value);
 * 
 * // Get a default value (0) for null
 * long safe = LongUtils.get(nullableLong);
 * 
 * // Safely parse a string to Long (returns null on error)
 * Long parsed = LongUtils.parse("123");
 * </pre>
 * 
 * @author Peter Nagy (xgxtpna)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LongUtils
{
    /**
     * @param l1
     * @param l2
     * @return boolean
     */
    public static boolean equals(final Long l1, final Long l2)
    {
        if (l1 == l2) // NOSONAR
        {
            return true;
        }

        if (l1 == null || l2 == null)
        {
            return false;
        }

        return l1.equals(l2);
    }


    /**
     * @param bi
     * @param l
     * @return boolean
     */
    public static boolean equals(final BigInteger bi, final Long l)
    {
        if (bi == null || l == null)
        {
            return false;
        }

        return equals(bi.longValue(), l);
    }


    /**
     * @param l
     * @param bi
     * @return boolean
     */
    public static boolean equals(final Long l, final BigInteger bi)
    {
        if (bi == null || l == null)
        {
            return false;
        }

        return equals(bi.longValue(), l);
    }


    /**
     * @param l1
     * @param l2
     * @param nullIsLess whether consider {@code null} value less than non-{@code null} value
     * @return &lt; 0, 0, &gt; 0, if {@code l1} is respectively less, equal ou greater than {@code l2}
     */
    public static int compare(final Long l1, final Long l2, final boolean nullIsLess)
    {
        if (l1 == l2) // NOSONAR
        {
            return 0;
        }
        if (l1 == null)
        {
            return nullIsLess ? -1 : 1;
        }
        if (l2 == null)
        {
            return nullIsLess ? 1 : -1;
        }
        return l1.compareTo(l2);
    }


    /**
     * @param l1
     * @param l2
     * @return &lt; 0, 0, &gt; 0, if {@code l1} is respectively less, equal ou greater than {@code l2}
     */
    public static int compare(final Long l1, final Long l2)
    {
        return compare(l1, l2, true);
    }


    /**
     * @param l
     * @param bi
     * @param nullIsLess
     * @return &lt; 0, 0, &gt; 0, if {@code l} is respectively less, equal ou greater than {@code bi}
     */
    public static int compare(final Long l, final BigInteger bi, final boolean nullIsLess)
    {
        if (bi == null)
        {
            return nullIsLess ? 1 : -1;
        }

        return compare(l, bi.longValue(), nullIsLess);
    }


    /**
     * @param l
     * @param bi
     * @return &lt; 0, 0, &gt; 0, if {@code l} is respectively less, equal ou greater than {@code bi}
     */
    public static int compare(final Long l, final BigInteger bi)
    {
        return compare(l, bi, true);
    }


    /**
     * @param bi
     * @param l
     * @param nullIsLess
     * @return &lt; 0, 0, &gt; 0, if {@code bi} is respectively less, equal ou greater than {@code l}
     */
    public static int compare(final BigInteger bi, final Long l, final boolean nullIsLess)
    {
        if (bi == null)
        {
            return nullIsLess ? -1 : 1;
        }

        return compare(bi.longValue(), l, nullIsLess);
    }


    /**
     * @param bi
     * @param l
     * @return &lt; 0, 0, &gt; 0, if {@code bi} is respectively less, equal ou greater than {@code l}
     */
    public static int compare(final BigInteger bi, final Long l)
    {
        return compare(bi, l, true);
    }


    /**
     * @param bi1
     * @param bi2
     * @param nullIsLess
     * @return &lt; 0, 0, &gt; 0, if {@code bi1} is respectively less, equal ou greater than {@code bi2}
     */
    public static int compare(final BigInteger bi1, final BigInteger bi2, final boolean nullIsLess)
    {
        if (bi1 == bi2)
        {
            return 0;
        }
        if (bi1 == null)
        {
            return nullIsLess ? -1 : 1;
        }
        if (bi2 == null)
        {
            return nullIsLess ? 1 : -1;
        }
        return bi1.compareTo(bi2);
    }


    /**
     * @param bi1
     * @param bi2
     * @return &lt; 0, 0, &gt; 0, if {@code bi1} is respectively less, equal ou greater than {@code bi2}
     */
    public static int compare(final BigInteger bi1, final BigInteger bi2)
    {
        return compare(bi1, bi2, true);
    }


    public static boolean isBlank(Long l)
    {
        return (l == null || l.equals(0L));
    }


    public static boolean isNotBlank(Long l)
    {
        return !isBlank(l);
    }


    public static long get(Long l)
    {
        return l != null ? l : 0;
    }


    public static Long parse(String text)
    {
        if (text == null)
        {
            return null;
        }

        try
        {
            return Long.parseLong(text);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
