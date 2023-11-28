package hu.perit.spvitamin.json.time;

import hu.perit.spvitamin.json.ExampleClass;
import hu.perit.spvitamin.json.JsonSerializable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class LocalDateTimeTest
{
    @BeforeEach
    void setUp()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Budapest"));
    }


    @Test
    void testDeserialization() throws IOException
    {
        // without T, no zone offset
        log.debug("without T, no zone offset --------------------------------------------------------------------------");
        testDeserialization("2020-05-01 10:11", refTime(2020, 5, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01 10:11:12", refTime(2020, 5, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01 10:11:12.1", refTime(2020, 5, 1, 10, 11, 12, 100));
        testDeserialization("2020-05-01 10:11:12.12", refTime(2020, 5, 1, 10, 11, 12, 120));
        testDeserialization("2020-05-01 10:11:12.123", refTime(2020, 5, 1, 10, 11, 12, 123));
        testDeserialization("2020-05-01 10:11:12.1234", refTimeNano(2020, 5, 1, 10, 11, 12, 123400000));
        testDeserialization("2020-05-01 10:11:12.12345", refTimeNano(2020, 5, 1, 10, 11, 12, 123450000));
        testDeserialization("2020-05-01 10:11:12.123456", refTimeNano(2020, 5, 1, 10, 11, 12, 123456000));
        testDeserialization("2020-05-01 10:11:12.1234567", refTimeNano(2020, 5, 1, 10, 11, 12, 123456700));
        testDeserialization("2020-05-01 10:11:12.12345678", refTimeNano(2020, 5, 1, 10, 11, 12, 123456780));
        testDeserialization("2020-05-01 10:11:12.123456789", refTimeNano(2020, 5, 1, 10, 11, 12, 123456789));

        // with T, no zone offset
        log.debug("with T, no zone offset -----------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11", refTime(2020, 5, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01T10:11:12", refTime(2020, 5, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01T10:11:12.1", refTime(2020, 5, 1, 10, 11, 12, 100));
        testDeserialization("2020-05-01T10:11:12.12", refTime(2020, 5, 1, 10, 11, 12, 120));
        testDeserialization("2020-05-01T10:11:12.123", refTime(2020, 5, 1, 10, 11, 12, 123));
        testDeserialization("2020-05-01T10:11:12.1234", refTimeNano(2020, 5, 1, 10, 11, 12, 123400000));
        testDeserialization("2020-05-01T10:11:12.12345", refTimeNano(2020, 5, 1, 10, 11, 12, 123450000));
        testDeserialization("2020-05-01T10:11:12.123456", refTimeNano(2020, 5, 1, 10, 11, 12, 123456000));
        testDeserialization("2020-05-01T10:11:12.1234567", refTimeNano(2020, 5, 1, 10, 11, 12, 123456700));
        testDeserialization("2020-05-01T10:11:12.12345678", refTimeNano(2020, 5, 1, 10, 11, 12, 123456780));
        testDeserialization("2020-05-01T10:11:12.123456789", refTimeNano(2020, 5, 1, 10, 11, 12, 123456789));

        // without T, with zone offset
        log.debug("without T, with zone offset ------------------------------------------------------------------------");
        testDeserialization("2020-05-01 10:11+0400", refTime(2020, 5, 1, 8, 11, 0, 0));
        testDeserialization("2020-05-01 10:11:12+0400", refTime(2020, 5, 1, 8, 11, 12, 0));
        testDeserialization("2020-05-01 10:11:12.1+0400", refTime(2020, 5, 1, 8, 11, 12, 100));
        testDeserialization("2020-05-01 10:11:12.12+0400", refTime(2020, 5, 1, 8, 11, 12, 120));
        testDeserialization("2020-05-01 10:11:12.123+0400", refTime(2020, 5, 1, 8, 11, 12, 123));
        testDeserialization("2020-05-01 10:11:12.1234+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123400000));
        testDeserialization("2020-05-01 10:11:12.12345+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123450000));
        testDeserialization("2020-05-01 10:11:12.123456+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456000));
        testDeserialization("2020-05-01 10:11:12.1234567+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456700));
        testDeserialization("2020-05-01 10:11:12.12345678+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456780));
        testDeserialization("2020-05-01 10:11:12.123456789+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456789));

        // with T, with zone offset
        log.debug("with T, with zone offset ---------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11+0400", refTime(2020, 5, 1, 8, 11, 0, 0));
        testDeserialization("2020-05-01T10:11:12+0400", refTime(2020, 5, 1, 8, 11, 12, 0));
        testDeserialization("2020-05-01T10:11:12.1+0400", refTime(2020, 5, 1, 8, 11, 12, 100));
        testDeserialization("2020-05-01T10:11:12.12+0400", refTime(2020, 5, 1, 8, 11, 12, 120));
        testDeserialization("2020-05-01T10:11:12.123+0400", refTime(2020, 5, 1, 8, 11, 12, 123));
        testDeserialization("2020-05-01T10:11:12.1234+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123400000));
        testDeserialization("2020-05-01T10:11:12.12345+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123450000));
        testDeserialization("2020-05-01T10:11:12.123456+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456000));
        testDeserialization("2020-05-01T10:11:12.1234567+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456700));
        testDeserialization("2020-05-01T10:11:12.12345678+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456780));
        testDeserialization("2020-05-01T10:11:12.123456789+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456789));

        // without T, Zulu time
        log.debug("without T, Zulu time -------------------------------------------------------------------------------");
        testDeserialization("2020-05-01 10:11Z", refTime(2020, 5, 1, 12, 11, 0, 0));
        testDeserialization("2020-05-01 10:11:12Z", refTime(2020, 5, 1, 12, 11, 12, 0));
        testDeserialization("2020-05-01 10:11:12.1Z", refTime(2020, 5, 1, 12, 11, 12, 100));
        testDeserialization("2020-05-01 10:11:12.12Z", refTime(2020, 5, 1, 12, 11, 12, 120));
        testDeserialization("2020-05-01 10:11:12.123Z", refTime(2020, 5, 1, 12, 11, 12, 123));
        testDeserialization("2020-05-01 10:11:12.1234Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123400000));
        testDeserialization("2020-05-01 10:11:12.12345Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123450000));
        testDeserialization("2020-05-01 10:11:12.123456Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456000));
        testDeserialization("2020-05-01 10:11:12.1234567Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456700));
        testDeserialization("2020-05-01 10:11:12.12345678Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456780));
        testDeserialization("2020-05-01 10:11:12.123456789Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456789));

        // with T, Zulu time
        log.debug("with T, Zulu time ----------------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11Z", refTime(2020, 5, 1, 12, 11, 0, 0));
        testDeserialization("2020-05-01T10:11:12Z", refTime(2020, 5, 1, 12, 11, 12, 0));
        testDeserialization("2020-05-01T10:11:12.123Z", refTime(2020, 5, 1, 12, 11, 12, 123));
        testDeserialization("2020-05-01T10:11:12.1234Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123400000));
        testDeserialization("2020-05-01T10:11:12.12345Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123450000));
        testDeserialization("2020-05-01T10:11:12.123456Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456000));
        testDeserialization("2020-05-01T10:11:12.1234567Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456700));
        testDeserialization("2020-05-01T10:11:12.12345678Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456780));
        testDeserialization("2020-05-01T10:11:12.123456789Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456789));
    }


    void testDeserialization(String dateString, LocalDateTime expectedDate) throws IOException
    {
        String jsonString = String.format("{\"localDateTime\":\"%s\"}", dateString);
        ExampleClass decodedObject = JsonSerializable.fromJson(jsonString, ExampleClass.class);
        log.debug("{} => {}", dateString, decodedObject.getLocalDateTime());

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
