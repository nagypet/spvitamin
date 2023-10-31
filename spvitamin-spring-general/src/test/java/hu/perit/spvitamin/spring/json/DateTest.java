package hu.perit.spvitamin.spring.json;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DateTest
{
    @Test
    void testDeserialization() throws IOException
    {
        testDeserialization("2020-05-01", refTime(2020, 4, 1, 0, 0, 0, 0));
        testDeserialization("2020-05-01 10:11", refTime(2020, 4, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01T10:11", refTime(2020, 4, 1, 10, 11, 0, 0));
        testDeserialization("2020-05-01 10:11:12", refTime(2020, 4, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01T10:11:12", refTime(2020, 4, 1, 10, 11, 12, 0));
        testDeserialization("2020-05-01 10:11:12.123", refTime(2020, 4, 1, 10, 11, 12, 123));
        testDeserialization("2020-05-01T10:11:12.123", refTime(2020, 4, 1, 10, 11, 12, 123));
        testDeserialization("2020-05-01 10:11+0400", refTime(2020, 4, 1, 10, 11, 0, 0, "GMT+4"));
        testDeserialization("2020-05-01T10:11+0400", refTime(2020, 4, 1, 10, 11, 0, 0, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12+0400", refTime(2020, 4, 1, 10, 11, 12, 0, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12+0400", refTime(2020, 4, 1, 10, 11, 12, 0, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.123+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.123+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.123499117Z", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.123499117+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
    }


    void testDeserialization(String dateString, Date expectedDate) throws IOException
    {
        String jsonString = String.format("{\"date\":\"%s\"}", dateString);
        log.debug(jsonString);
        Example decodedObject = JsonSerializable.fromJson(jsonString, Example.class);
        log.debug("decoded:  " + decodedObject.toString());

        assertThat(decodedObject.getDate()).isEqualTo(expectedDate);
    }


    private Date refTime(int year, int month, int date, int hourOfDay, int minute, int second, int millis)
    {
        return refTime(year, month, date, hourOfDay, minute, second, millis, null);
    }


    private Date refTime(int year, int month, int date, int hourOfDay, int minute, int second, int millis, String timeZone)
    {
        Calendar cal = Calendar.getInstance();
        if (timeZone != null)
        {
            cal.setTimeZone(TimeZone.getTimeZone(timeZone));
        }
        cal.set(year, month, date, hourOfDay, minute, second);
        cal.set(Calendar.MILLISECOND, millis);
        return cal.getTime();
    }
}
