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

package hu.perit.spvitamin.spring.data.nativequery;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.text.MessageFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldMapper
{
    public static String toString(Object field)
    {
        if (field == null)
        {
            return null;
        }

        if (field instanceof String stringValue)
        {
            return StringUtils.strip(stringValue);
        }
        return StringUtils.strip(field.toString());
    }


    public static Integer toInt(Object field)
    {
        if (field == null)
        {
            return 0;
        }

        if (field instanceof Integer intValue)
        {
            return intValue;
        }
        if (field instanceof BigInteger bigInteger)
        {
            return bigInteger.intValue();
        }
        else if (field instanceof Number)
        {
            return Integer.parseInt(field.toString());
        }
        throw new ClassCastException(MessageFormat.format("Cannot cast {0} to {1}!", field.getClass(), Integer.class.getName()));
    }


    public static Long toLong(Object field)
    {
        if (field == null)
        {
            return 0L;
        }

        if (field instanceof Long longValue)
        {
            return longValue;
        }
        if (field instanceof BigInteger bigInteger)
        {
            return bigInteger.longValue();
        }
        else if (field instanceof Number)
        {
            return Long.parseLong(field.toString());
        }
        throw new ClassCastException(MessageFormat.format("Cannot cast {0} to {1}!", field.getClass(), Long.class.getName()));
    }
}
