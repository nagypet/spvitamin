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

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

/**
 * A utility class for working with Java's date and time classes.
 * 
 * <p>This class provides methods for formatting, converting, and manipulating
 * various date-time objects including LocalDateTime, OffsetDateTime, and Date.
 * It offers consistent formatting across different date-time types and simplifies
 * conversion between them.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Format date-time objects as strings in a consistent format</li>
 *   <li>Convert between different date-time types (Date, LocalDateTime, etc.)</li>
 *   <li>Create LocalDateTime from epoch milliseconds</li>
 *   <li>Standardized DateTimeFormatter for database-compatible formatting</li>
 *   <li>Null-safe operations</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Format a LocalDateTime as a string
 * String formatted = LocalDateTimeUtils.format(LocalDateTime.now()); // "2023-05-15 14:30:45"
 * 
 * // Convert from Date to LocalDateTime
 * LocalDateTime ldt = LocalDateTimeUtils.fromDate(new Date());
 * 
 * // Create from milliseconds
 * LocalDateTime fromMillis = LocalDateTimeUtils.getLocalDateTimeFromMillis(System.currentTimeMillis());
 * </pre>
 * 
 * @author nagy_peter
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalDateTimeUtils
{

    public static String format(LocalDateTime timestamp)
    {
        if (timestamp == null)
        {
            return "";
        }

        return timestamp.format(getDateTimeFormatter());
    }


    public static String format(OffsetDateTime timestamp)
    {
        if (timestamp == null)
        {
            return "";
        }

        return timestamp.toLocalDateTime().truncatedTo(ChronoUnit.SECONDS).format(getDateTimeFormatter());
    }


    public static String format(Date date)
    {
        if (date == null)
        {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }


    /**
     * Returns a DateTimeFormatter with yyyy-MM-dd HH:mm:ss format,
     * which is compatible with database timestamp formats.
     *
     * @return {@link DateTimeFormatter} with database-friendly format
     */
    public static DateTimeFormatter getDateTimeFormatter()
    {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(DateTimeFormatter.ISO_TIME)
                .toFormatter();
    }


    public static LocalDateTime getLocalDateTimeFromMillis(long millis)
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), TimeZone.getDefault().toZoneId());
    }


    public static LocalDateTime fromDate(Date date)
    {
        if (date == null)
        {
            return null;
        }

        return getLocalDateTimeFromMillis(date.getTime());
    }
}
