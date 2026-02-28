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
public final class IntUtils
{
    /**
     * @param i1
     * @param i2
     * @return boolean
     */
    public static boolean equals(final Integer i1, final Integer i2)
    {
        if (i1 == i2) // NOSONAR
        {
            return true;
        }

        if (i1 == null || i2 == null)
        {
            return false;
        }

        return i1.equals(i2);
    }


    /**
     * @param bi
     * @param i
     * @return boolean
     */
    public static boolean equals(final BigInteger bi, final Integer i)
    {
        if (bi == null || i == null)
        {
            return false;
        }

        return equals(bi.intValue(), i);
    }


    /**
     * @param i
     * @param bi
     * @return boolean
     */
    public static boolean equals(final Integer i, final BigInteger bi)
    {
        if (bi == null || i == null)
        {
            return false;
        }

        return equals(bi.intValue(), i);
    }


    /**
     * @param i1
     * @param i2
     * @param nullIsLess whether consider {@code null} value less than non-{@code null} value
     * @return &lt; 0, 0, &gt; 0, if {@code i1} is respectively less, equal ou greater than {@code i2}
     */
    public static int compare(final Integer i1, final Integer i2, final boolean nullIsLess)
    {
        if (i1 == i2) // NOSONAR
        {
            return 0;
        }
        if (i1 == null)
        {
            return nullIsLess ? -1 : 1;
        }
        if (i2 == null)
        {
            return nullIsLess ? 1 : -1;
        }
        return i1.compareTo(i2);
    }


    /**
     * @param i1
     * @param i2
     * @return &lt; 0, 0, &gt; 0, if {@code i1} is respectively less, equal ou greater than {@code i2}
     */
    public static int compare(final Integer i1, final Integer i2)
    {
        return compare(i1, i2, true);
    }


    /**
     * @param i
     * @param bi
     * @param nullIsLess
     * @return &lt; 0, 0, &gt; 0, if {@code i} is respectively less, equal ou greater than {@code bi}
     */
    public static int compare(final Integer i, final BigInteger bi, final boolean nullIsLess)
    {
        if (bi == null)
        {
            return nullIsLess ? 1 : -1;
        }

        return compare(i, bi.intValue(), nullIsLess);
    }


    /**
     * @param i
     * @param bi
     * @return &lt; 0, 0, &gt; 0, if {@code i} is respectively less, equal ou greater than {@code bi}
     */
    public static int compare(final Integer i, final BigInteger bi)
    {
        return compare(i, bi, true);
    }


    /**
     * @param bi
     * @param i
     * @param nullIsLess
     * @return &lt; 0, 0, &gt; 0, if {@code bi} is respectively less, equal ou greater than {@code i}
     */
    public static int compare(final BigInteger bi, final Integer i, final boolean nullIsLess)
    {
        if (bi == null)
        {
            return nullIsLess ? -1 : 1;
        }

        return compare(bi.intValue(), i, nullIsLess);
    }


    /**
     * @param bi
     * @param i
     * @return &lt; 0, 0, &gt; 0, if {@code bi} is respectively less, equal ou greater than {@code i}
     */
    public static int compare(final BigInteger bi, final Integer i)
    {
        return compare(bi, i, true);
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


    public static boolean isBlank(Integer l)
    {
        return (l == null || l.equals(0));
    }


    public static boolean isNotBlank(Integer l)
    {
        return !isBlank(l);
    }


    public static int get(Integer l)
    {
        return l != null ? l : 0;
    }


    public static Integer parse(String text)
    {
        if (text == null)
        {
            return null;
        }

        try
        {
            return Integer.parseInt(text);
        }
        catch (Exception e)
        {
            return null;
        }
    }


    public static int min(Integer i1, Integer i2)
    {
        return Math.min(get(i1), get(i2));
    }


    public static int max(Integer i1, Integer i2)
    {
        return Math.max(get(i1), get(i2));
    }
}
