/*
 * Copyright (c) 2023. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.core.typehelpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.TimeZone;

/**
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

        return timestamp.toLocalDateTime().format(getDateTimeFormatter());
    }


    /**
     * @return {@link DateTimeFormatter} yyyy-mm-dd hh:mm:ss formátummal,
     * ahogy az adatbázis szereti
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
}
