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
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class OffsetDateTimeToUTCConverter implements AttributeConverter<OffsetDateTime, LocalDateTime>
{
    @Override
    public LocalDateTime convertToDatabaseColumn(OffsetDateTime offsetDateTime)
    {
        if (offsetDateTime == null)
        {
            return null;
        }

        return offsetDateTime.atZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(LocalDateTime localDateTime)
    {
        if (localDateTime == null)
        {
            return null;
        }

        ZonedDateTime utc = localDateTime.atZone(ZoneId.of("UTC"));
        return utc.withZoneSameInstant(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
