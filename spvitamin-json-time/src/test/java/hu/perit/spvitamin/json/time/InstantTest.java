package hu.perit.spvitamin.json.time;

import hu.perit.spvitamin.json.ExampleClass;
import hu.perit.spvitamin.json.JsonSerializable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class InstantTest
{
    @BeforeEach
    void setUp()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Budapest"));
    }

    @Test
    void testDeserialization() throws IOException
    {
        // without T, with zone offset
        log.debug("without T, with zone offset ------------------------------------------------------------------------");
        testDeserialization("2020-05-01 10:11+0400", refTime(2020, 5, 1, 8, 11, 0, 0, "+2"));
        testDeserialization("2020-05-01 10:11:12+0400", refTime(2020, 5, 1, 8, 11, 12, 0, "+2"));
        testDeserialization("2020-05-01 10:11:12.1+0400", refTime(2020, 5, 1, 8, 11, 12, 100, "+2"));
        testDeserialization("2020-05-01 10:11:12.12+0400", refTime(2020, 5, 1, 8, 11, 12, 120, "+2"));
        testDeserialization("2020-05-01 10:11:12.123+0400", refTime(2020, 5, 1, 8, 11, 12, 123, "+2"));
        testDeserialization("2020-05-01 10:11:12.1234+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123400000, "+2"));
        testDeserialization("2020-05-01 10:11:12.12345+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123450000, "+2"));
        testDeserialization("2020-05-01 10:11:12.123456+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456000, "+2"));
        testDeserialization("2020-05-01 10:11:12.1234567+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456700, "+2"));
        testDeserialization("2020-05-01 10:11:12.12345678+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456780, "+2"));
        testDeserialization("2020-05-01 10:11:12.123456789+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456789, "+2"));

        // with T, with zone offset
        log.debug("with T, with zone offset ------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11+0400", refTime(2020, 5, 1, 8, 11, 0, 0, "+2"));
        testDeserialization("2020-05-01T10:11:12+0400", refTime(2020, 5, 1, 8, 11, 12, 0, "+2"));
        testDeserialization("2020-05-01T10:11:12.1+0400", refTime(2020, 5, 1, 8, 11, 12, 100, "+2"));
        testDeserialization("2020-05-01T10:11:12.12+0400", refTime(2020, 5, 1, 8, 11, 12, 120, "+2"));
        testDeserialization("2020-05-01T10:11:12.123+0400", refTime(2020, 5, 1, 8, 11, 12, 123, "+2"));
        testDeserialization("2020-05-01T10:11:12.1234+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123400000, "+2"));
        testDeserialization("2020-05-01T10:11:12.12345+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123450000, "+2"));
        testDeserialization("2020-05-01T10:11:12.123456+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456000, "+2"));
        testDeserialization("2020-05-01T10:11:12.1234567+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456700, "+2"));
        testDeserialization("2020-05-01T10:11:12.12345678+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456780, "+2"));
        testDeserialization("2020-05-01T10:11:12.123456789+0400", refTimeNano(2020, 5, 1, 8, 11, 12, 123456789, "+2"));

        // without T, Zulu time
        log.debug("without T, Zulu time -------------------------------------------------------------------------------");
        testDeserialization("2020-05-01 10:11Z", refTime(2020, 5, 1, 12, 11, 0, 0, "+2"));
        testDeserialization("2020-05-01 10:11:12Z", refTime(2020, 5, 1, 12, 11, 12, 0, "+2"));
        testDeserialization("2020-05-01 10:11:12.1Z", refTime(2020, 5, 1, 12, 11, 12, 100, "+2"));
        testDeserialization("2020-05-01 10:11:12.12Z", refTime(2020, 5, 1, 12, 11, 12, 120, "+2"));
        testDeserialization("2020-05-01 10:11:12.123Z", refTime(2020, 5, 1, 12, 11, 12, 123, "+2"));
        testDeserialization("2020-05-01 10:11:12.1234Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123400000, "+2"));
        testDeserialization("2020-05-01 10:11:12.12345Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123450000, "+2"));
        testDeserialization("2020-05-01 10:11:12.123456Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456000, "+2"));
        testDeserialization("2020-05-01 10:11:12.1234567Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456700, "+2"));
        testDeserialization("2020-05-01 10:11:12.12345678Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456780, "+2"));
        testDeserialization("2020-05-01 10:11:12.123456789Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456789, "+2"));

        // with T, Zulu time
        log.debug("with T, Zulu time -------------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11Z", refTime(2020, 5, 1, 12, 11, 0, 0, "+2"));
        testDeserialization("2020-05-01T10:11:12Z", refTime(2020, 5, 1, 12, 11, 12, 0, "+2"));
        testDeserialization("2020-05-01T10:11:12.1Z", refTime(2020, 5, 1, 12, 11, 12, 100, "+2"));
        testDeserialization("2020-05-01T10:11:12.12Z", refTime(2020, 5, 1, 12, 11, 12, 120, "+2"));
        testDeserialization("2020-05-01T10:11:12.123Z", refTime(2020, 5, 1, 12, 11, 12, 123, "+2"));
        testDeserialization("2020-05-01T10:11:12.1234Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123400000, "+2"));
        testDeserialization("2020-05-01T10:11:12.12345Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123450000, "+2"));
        testDeserialization("2020-05-01T10:11:12.123456Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456000, "+2"));
        testDeserialization("2020-05-01T10:11:12.1234567Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456700, "+2"));
        testDeserialization("2020-05-01T10:11:12.12345678Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456780, "+2"));
        testDeserialization("2020-05-01T10:11:12.123456789Z", refTimeNano(2020, 5, 1, 12, 11, 12, 123456789, "+2"));
    }


    void testDeserialization(String dateString, Instant expectedDate) throws IOException
    {
        String jsonString = String.format("{\"instant\":\"%s\"}", dateString);
        ExampleClass decodedObject = JsonSerializable.fromJson(jsonString, ExampleClass.class);
        log.debug("{} => {}", dateString, decodedObject.getInstant());

        assertThat(decodedObject.getInstant()).isEqualTo(expectedDate);
    }


    private Instant refTimeNano(int year, int month, int date, int hourOfDay, int minute, int second, int nanos, String zoneOffset)
    {
        return Instant.from(OffsetDateTime.of(year, month, date, hourOfDay, minute, second, nanos, ZoneOffset.of(zoneOffset)));
    }


    private Instant refTime(int year, int month, int date, int hourOfDay, int minute, int second, int millis, String zoneOffset)
    {
        return Instant.from(OffsetDateTime.of(year, month, date, hourOfDay, minute, second, millis * 1_000_000, ZoneOffset.of(zoneOffset)));
    }
}
