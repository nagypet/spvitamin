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

package hu.perit.spvitamin.spring.data.converter;

import jakarta.persistence.AttributeConverter;

public class BooleanToLongConverter implements AttributeConverter<Boolean, Long>
{
    @Override
    public Long convertToDatabaseColumn(Boolean aBoolean)
    {
        if (aBoolean == null)
        {
            return null;
        }
        return aBoolean ? 1L : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Long aLong)
    {
        if (aLong == null)
        {
            return null; // NOSONAR
        }
        return aLong > 0L;
    }
}
