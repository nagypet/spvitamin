package hu.perit.spvitamin.core.typehelpers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class LocalDateTimeUtilsTest
{
    @Test
    void testWithMinutes()
    {
        String input = "2021-06-01T07:20+0100";
        OffsetDateTime odt = OffsetDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ"));
        String formatted = LocalDateTimeUtils.format(odt);
        log.debug("{} => {}", input, formatted);
        assertEquals("2021-06-01 07:20:00", formatted);
    }

    @Test
    void testWithSeconds()
    {
        String input = "2021-06-01T07:20:43+0100";
        OffsetDateTime odt = OffsetDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
        String formatted = LocalDateTimeUtils.format(odt);
        log.debug("{} => {}", input, formatted);
        assertEquals("2021-06-01 07:20:43", formatted);
    }

    @Test
    void testInputWithMilliseconds()
    {
        String input = "2021-06-01T07:20:43.962+0100";
        OffsetDateTime odt = OffsetDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        String formatted = LocalDateTimeUtils.format(odt);
        log.debug("{} => {}", input, formatted);
        assertEquals("2021-06-01 07:20:43", formatted);
    }
}