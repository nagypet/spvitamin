package hu.perit.spvitamin.json.time;

import hu.perit.spvitamin.json.ExampleClass;
import hu.perit.spvitamin.json.JsonSerializable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class LocalDateTest
{
    @Test
    void testDeserialization() throws IOException
    {
        testDeserialization("2020-05-01 10:11", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123", refTime(2020, 5, 1));
        // With timezone
        testDeserialization("2020-05-01 10:11+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123+0400", refTime(2020, 5, 1));
        // Zulu time
        testDeserialization("2020-05-01 10:11Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123Z", refTime(2020, 5, 1));
        // Nanoseconds
        testDeserialization("2020-05-01T10:11:12.695499117", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.695499117Z", refTime(2020, 5, 1));
    }


    void testDeserialization(String dateString, LocalDate expectedDate) throws IOException
    {
        String jsonString = String.format("{\"localDate\":\"%s\"}", dateString);
        ExampleClass decodedObject = JsonSerializable.fromJson(jsonString, ExampleClass.class);
        log.debug("{} => {}", jsonString, decodedObject.toString());

        assertThat(decodedObject.getLocalDate()).isEqualTo(expectedDate);
    }


    private LocalDate refTime(int year, int month, int date)
    {
        return LocalDate.of(year, month, date);
    }
}
