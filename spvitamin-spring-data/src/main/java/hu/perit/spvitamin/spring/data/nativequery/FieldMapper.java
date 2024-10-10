/*
 * Copyright 2020-2024 the original author or authors.
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
        throw new ClassCastException(MessageFormat.format("Cannot cast {0} to long!", field.getClass()));
    }


    public static long toLong(Object field)
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
        throw new ClassCastException(MessageFormat.format("Cannot cast {0} to long!", field.getClass()));
    }
}
