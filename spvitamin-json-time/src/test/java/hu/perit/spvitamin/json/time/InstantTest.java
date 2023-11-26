package hu.perit.spvitamin.json.time;

import hu.perit.spvitamin.json.ExampleClass;
import hu.perit.spvitamin.json.JsonSerializable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class InstantTest
{
    @Test
    void testDeserialization() throws IOException
    {
        // With timezone
        testDeserialization("2020-05-01 10:11+0400", refTime(2020, 5, 1, 10, 11, 0, 0, "+4"));
        testDeserialization("2020-05-01T10:11+0400", refTime(2020, 5, 1, 10, 11, 0, 0, "+4"));
        testDeserialization("2020-05-01 10:11:12+0400", refTime(2020, 5, 1, 10, 11, 12, 0, "+4"));
        testDeserialization("2020-05-01T10:11:12+0400", refTime(2020, 5, 1, 10, 11, 12, 0, "+4"));
        testDeserialization("2020-05-01 10:11:12.123+0400", refTime(2020, 5, 1, 10, 11, 12, 123, "+4"));
        testDeserialization("2020-05-01T10:11:12.123+0400", refTime(2020, 5, 1, 10, 11, 12, 123, "+4"));
        // Zulu time
        testDeserialization("2020-05-01 10:11Z", refTime(2020, 5, 1, 10, 11, 0, 0, "Z"));
        testDeserialization("2020-05-01T10:11Z", refTime(2020, 5, 1, 10, 11, 0, 0, "Z"));
        testDeserialization("2020-05-01 10:11:12Z", refTime(2020, 5, 1, 10, 11, 12, 0, "Z"));
        testDeserialization("2020-05-01T10:11:12Z", refTime(2020, 5, 1, 10, 11, 12, 0, "Z"));
        testDeserialization("2020-05-01 10:11:12.123Z", refTime(2020, 5, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.123Z", refTime(2020, 5, 1, 10, 11, 12, 123, "Z"));
        // Microseconds
        testDeserialization("2020-05-01T10:11:12.695499Z", refTimeNano(2020, 5, 1, 10, 11, 12, 695499000, "Z"));
        testDeserialization("2020-05-01T10:11:12.695499+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 695499000, "+4"));
        // Nanoseconds
        testDeserialization("2020-05-01T10:11:12.695499117Z", refTimeNano(2020, 5, 1, 10, 11, 12, 695499117, "Z"));
        testDeserialization("2020-05-01T10:11:12.695499117+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 695499117, "+4"));

        testDeserialization("2020-05-01T10:11:12.1+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 100000000, "+4"));
        testDeserialization("2020-05-01T10:11:12.12+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 120000000, "+4"));
        testDeserialization("2020-05-01T10:11:12.123+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 123000000, "+4"));
        testDeserialization("2020-05-01T10:11:12.1234+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 123400000, "+4"));
        testDeserialization("2020-05-01T10:11:12.12345+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 123450000, "+4"));
        testDeserialization("2020-05-01T10:11:12.123456+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 123456000, "+4"));
        testDeserialization("2020-05-01T10:11:12.1234567+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 123456700, "+4"));
        testDeserialization("2020-05-01T10:11:12.12345678+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 123456780, "+4"));
        testDeserialization("2020-05-01T10:11:12.123456789+0400", refTimeNano(2020, 5, 1, 10, 11, 12, 123456789, "+4"));
    }


    void testDeserialization(String dateString, Instant expectedDate) throws IOException
    {
        String jsonString = String.format("{\"instant\":\"%s\"}", dateString);
        ExampleClass decodedObject = JsonSerializable.fromJson(jsonString, ExampleClass.class);
        log.debug("{} => {}", jsonString, decodedObject.toString());

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
