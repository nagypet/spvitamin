/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.json.time;

import hu.perit.spvitamin.json.ExampleClass;
import hu.perit.spvitamin.json.JSonSerializer;
import hu.perit.spvitamin.json.JsonSerializable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DateTest
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
        testDeserialization("2020-05-01", refTime(2020, 4, 1, 2, 0, 0, 0));
        testDeserialization("2020-05-01 10:11", refTime(2020, 4, 1, 10, 11, 0, 0, "Z"));
        testDeserialization("2020-05-01 10:11:12", refTime(2020, 4, 1, 10, 11, 12, 0, "Z"));
        testDeserialization("2020-05-01 10:11:12.1", refTime(2020, 4, 1, 10, 11, 12, 100, "Z"));
        testDeserialization("2020-05-01 10:11:12.12", refTime(2020, 4, 1, 10, 11, 12, 120, "Z"));
        testDeserialization("2020-05-01 10:11:12.123", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01 10:11:12.1234", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01 10:11:12.12345", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01 10:11:12.123456", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01 10:11:12.1234567", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01 10:11:12.12345678", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01 10:11:12.123456789", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));

        // with T, no zone offset
        log.debug("with T, no zone offset -----------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11", refTime(2020, 4, 1, 10, 11, 0, 0, "Z"));
        testDeserialization("2020-05-01T10:11:12", refTime(2020, 4, 1, 10, 11, 12, 0, "Z"));
        testDeserialization("2020-05-01T10:11:12.1", refTime(2020, 4, 1, 10, 11, 12, 100, "Z"));
        testDeserialization("2020-05-01T10:11:12.12", refTime(2020, 4, 1, 10, 11, 12, 120, "Z"));
        testDeserialization("2020-05-01T10:11:12.123", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.1234", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.12345", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.123456", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.1234567", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.12345678", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.123456789", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));

        // without T, with zone offset
        log.debug("without T, with zone offset ------------------------------------------------------------------------");
        testDeserialization("2020-05-01 10:11+0400", refTime(2020, 4, 1, 10, 11, 0, 0, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12+0400", refTime(2020, 4, 1, 10, 11, 12, 0, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.1+0400", refTime(2020, 4, 1, 10, 11, 12, 100, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.12+0400", refTime(2020, 4, 1, 10, 11, 12, 120, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.123+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.1234+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.12345+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.123456+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.1234567+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.12345678+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01 10:11:12.123456789+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));

        // with T, with zone offset
        log.debug("with T, with zone offset ---------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11+0400", refTime(2020, 4, 1, 10, 11, 0, 0, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12+0400", refTime(2020, 4, 1, 10, 11, 12, 0, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.1+0400", refTime(2020, 4, 1, 10, 11, 12, 100, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.12+0400", refTime(2020, 4, 1, 10, 11, 12, 120, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.123+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.1234+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.12345+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.123456+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.1234567+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.12345678+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));
        testDeserialization("2020-05-01T10:11:12.123456789+0400", refTime(2020, 4, 1, 10, 11, 12, 123, "GMT+4"));

        // without T, Zulu time
        log.debug("without T, Zulu time -------------------------------------------------------------------------------");
        testDeserialization("2020-05-01 10:11Z", refTime(2020, 4, 1, 10, 11, 0, 0, "Z"));
        testDeserialization("2020-05-01 10:11:12Z", refTime(2020, 4, 1, 10, 11, 12, 0, "Z"));
        testDeserialization("2020-05-01 10:11:12.1Z", refTime(2020, 4, 1, 10, 11, 12, 100, "Z"));
        testDeserialization("2020-05-01 10:11:12.12Z", refTime(2020, 4, 1, 10, 11, 12, 120, "Z"));
        testDeserialization("2020-05-01 10:11:12.123Z", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01 10:11:12.123456Z", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01 10:11:12.123456789Z", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));

        // with T, Zulu time
        log.debug("with T, Zulu time ----------------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11Z", refTime(2020, 4, 1, 10, 11, 0, 0, "Z"));
        testDeserialization("2020-05-01T10:11:12Z", refTime(2020, 4, 1, 10, 11, 12, 0, "Z"));
        testDeserialization("2020-05-01T10:11:12.1Z", refTime(2020, 4, 1, 10, 11, 12, 100, "Z"));
        testDeserialization("2020-05-01T10:11:12.12Z", refTime(2020, 4, 1, 10, 11, 12, 120, "Z"));
        testDeserialization("2020-05-01T10:11:12.123Z", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.123456Z", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
        testDeserialization("2020-05-01T10:11:12.123456789Z", refTime(2020, 4, 1, 10, 11, 12, 123, "Z"));
    }


    void testDeserialization(String dateString, Date expectedDate) throws IOException
    {
        String jsonString = String.format("{\"date\":\"%s\"}", dateString);
        ExampleClass decodedObject = JsonSerializable.fromJson(jsonString, ExampleClass.class);
        log.debug("{} => {}", dateString, JSonSerializer.toJson(decodedObject.getDate()));

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
