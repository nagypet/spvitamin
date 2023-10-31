package hu.perit.spvitamin.spring.json;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class LocalDateTimeTest
{
    @Test
    void testDeserialization() throws IOException
    {
        testDeserialization("2020-05-01 10:11", refTime(2020, 5, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01T10:11", refTime(2020, 5, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01 10:11:12", refTime(2020, 5, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01T10:11:12", refTime(2020, 5, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01 10:11:12.123", refTime(2020, 5, 1, 10, 11, 12, 123));
        testDeserialization("2020-05-01T10:11:12.123", refTime(2020, 5, 1, 10, 11, 12, 123));
        // With timezone
        testDeserialization("2020-05-01 10:11+0400", refTime(2020, 5, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01T10:11+0400", refTime(2020, 5, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01 10:11:12+0400", refTime(2020, 5, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01T10:11:12+0400", refTime(2020, 5, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01 10:11:12.123+0400", refTime(2020, 5, 1, 10, 11, 12, 123));
        testDeserialization("2020-05-01T10:11:12.123+0400", refTime(2020, 5, 1, 10, 11, 12, 123));
        // Zulu time
        testDeserialization("2020-05-01 10:11Z", refTime(2020, 5, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01T10:11Z", refTime(2020, 5, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01 10:11:12Z", refTime(2020, 5, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01T10:11:12Z", refTime(2020, 5, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01 10:11:12.123Z", refTime(2020, 5, 1, 10, 11, 12, 123));
        testDeserialization("2020-05-01T10:11:12.123Z", refTime(2020, 5, 1, 10, 11, 12, 123));
        // Nanoseconds
        testDeserialization("2020-05-01T10:11:12.695499117", refTimeNano(2020, 5, 1, 10, 11, 12, 695499117));
        testDeserialization("2020-05-01T10:11:12.695499117Z", refTimeNano(2020, 5, 1, 10, 11, 12, 695499117));
    }


    void testDeserialization(String dateString, LocalDateTime expectedDate) throws IOException
    {
        String jsonString = String.format("{\"localDateTime\":\"%s\"}", dateString);
        log.debug(jsonString);
        Example decodedObject = JsonSerializable.fromJson(jsonString, Example.class);
        log.debug("decoded:  " + decodedObject.toString());

        assertThat(decodedObject.getLocalDateTime()).isEqualTo(expectedDate);
    }


    private LocalDateTime refTimeNano(int year, int month, int date, int hourOfDay, int minute, int second, int nanos)
    {
        return LocalDateTime.of(year, month, date, hourOfDay, minute, second, nanos);
    }


    private LocalDateTime refTime(int year, int month, int date, int hourOfDay, int minute, int second, int millis)
    {
        return LocalDateTime.of(year, month, date, hourOfDay, minute, second, millis * 1_000_000);
    }
}
