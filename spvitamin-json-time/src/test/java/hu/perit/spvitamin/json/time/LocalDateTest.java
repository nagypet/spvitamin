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
import hu.perit.spvitamin.json.JsonSerializable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class LocalDateTest
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
        testDeserialization("2020-05-01", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.1", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.12", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.1234", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.12345", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123456", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.1234567", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.12345678", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123456789", refTime(2020, 5, 1));

        // with T, no zone offset
        log.debug("with T, no zone offset -----------------------------------------------------------------------------");
        testDeserialization("2020-05-01", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.1", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.12", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.1234", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.12345", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123456", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.1234567", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.12345678", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123456789", refTime(2020, 5, 1));

        // without T, with zone offset
        log.debug("without T, with zone offset ------------------------------------------------------------------------");
        testDeserialization("2020-05-01 10:11+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.1+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.12+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.1234+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.12345+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123456+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.1234567+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.12345678+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123456789+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 23:11:12.123456789-0400", refTime(2020, 5, 2));

        // with T, with zone offset
        log.debug("with T, with zone offset ---------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.1+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.12+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.1234+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.12345+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123456+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.1234567+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.12345678+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123456789+0400", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T23:11:12.123456789-0400", refTime(2020, 5, 2));

        // without T, Zulu time
        log.debug("without T, Zulu time -------------------------------------------------------------------------------");
        testDeserialization("2020-05-01 10:11Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.1Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.12Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.1234Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.12345Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123456Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.1234567Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.12345678Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01 10:11:12.123456789Z", refTime(2020, 5, 1));

        // with T, Zulu time
        log.debug("with T, Zulu time ----------------------------------------------------------------------------------");
        testDeserialization("2020-05-01T10:11Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.1Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.12Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.1234Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.12345Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123456Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.1234567Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.12345678Z", refTime(2020, 5, 1));
        testDeserialization("2020-05-01T10:11:12.123456789Z", refTime(2020, 5, 1));
    }


    void testDeserialization(String dateString, LocalDate expectedDate) throws IOException
    {
        String jsonString = String.format("{\"localDate\":\"%s\"}", dateString);
        ExampleClass decodedObject = JsonSerializable.fromJson(jsonString, ExampleClass.class);
        log.debug("{} => {}", dateString, decodedObject.getLocalDate());

        assertThat(decodedObject.getLocalDate()).isEqualTo(expectedDate);
    }


    private LocalDate refTime(int year, int month, int date)
    {
        return LocalDate.of(year, month, date);
    }
}
