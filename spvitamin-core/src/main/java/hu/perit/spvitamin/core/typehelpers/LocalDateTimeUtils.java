/*
 * Copyright (c) 2023. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.core.typehelpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.TimeZone;

/**
 * @author nagy_peter
 */
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

    private LocalDateTimeUtils()
    {
        // Utility class
    }
}
