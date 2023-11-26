package hu.perit.spvitamin.json.time;

import hu.perit.spvitamin.json.ExampleClass;
import hu.perit.spvitamin.json.JsonSerializable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ZonedDateTimeTest
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
    }


    void testDeserialization(String dateString, ZonedDateTime expectedDate) throws IOException
    {
        String jsonString = String.format("{\"zonedDateTime\":\"%s\"}", dateString);
        log.debug(jsonString);
        ExampleClass decodedObject = JsonSerializable.fromJson(jsonString, ExampleClass.class);
        log.debug("decoded:  " + decodedObject.toString());

        assertThat(decodedObject.getZonedDateTime()).isEqualTo(expectedDate);
    }


    private ZonedDateTime refTimeNano(int year, int month, int date, int hourOfDay, int minute, int second, int nanos, String zoneId)
    {
        return ZonedDateTime.of(year, month, date, hourOfDay, minute, second, nanos, ZoneId.of(zoneId));
    }


    private ZonedDateTime refTime(int year, int month, int date, int hourOfDay, int minute, int second, int millis, String zoneId)
    {
        return ZonedDateTime.of(year, month, date, hourOfDay, minute, second, millis * 1_000_000, ZoneId.of(zoneId));
    }
}
