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

package hu.perit.spvitamin.json.typehelpers;

import hu.perit.spvitamin.json.JSonSerializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class OffsetDateTimeUtils
{
    public static OffsetDateTime fromLocalDateTime(LocalDateTime localDateTime)
    {
        if (localDateTime == null)
        {
            return null;
        }

        ZoneId zoneId = ZoneId.systemDefault();
        ZoneOffset zoneOffset = zoneId.getRules().getOffset(localDateTime);
        return localDateTime.atOffset(zoneOffset);
    }


    public static OffsetDateTime fromLocalDate(LocalDate localDate)
    {
        if (localDate == null)
        {
            return null;
        }

        LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.of(0, 0, 0));
        return fromLocalDateTime(localDateTime);
    }


    public static OffsetDateTime fromDate(Date date)
    {
        if (date == null)
        {
            return null;
        }

        Instant instant = date.toInstant();
        return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    }


    public static OffsetDateTime fromTimestamp(Timestamp timestamp)
    {
        if (timestamp == null)
        {
            return null;
        }

        return fromLocalDateTime(timestamp.toLocalDateTime());
    }


    public static OffsetDateTime fromString(String dateAsString)
    {
        if (StringUtils.isBlank(dateAsString))
        {
            return null;
        }

        OffsetDateTime offsetDateTime = tryConvertFromString(dateAsString, OffsetDateTime.class);
        if (offsetDateTime != null)
        {
            return offsetDateTime;
        }

        // Try convert from LocalDateTime
        LocalDateTime localDateTime = tryConvertFromString(dateAsString, LocalDateTime.class);
        if (localDateTime != null)
        {
            return fromLocalDateTime(localDateTime);
        }

        // Try convert from LocalDate
        LocalDate localDate = tryConvertFromString(dateAsString, LocalDate.class);
        if (localDate != null)
        {
            return fromLocalDate(localDate);
        }

        // Try convert from Date
        Date date = tryConvertFromString(dateAsString, Date.class);
        if (date != null)
        {
            return fromDate(date);
        }

        log.error("'{}' could not be converted to OffsetDateTime!", dateAsString);
        return null;
    }


    private static <T> T tryConvertFromString(String dateAsString, Class<T> clazz)
    {
        try
        {
            return JSonSerializer.fromJson(dateAsString, clazz);
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
