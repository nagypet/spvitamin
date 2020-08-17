/*
 * Copyright 2020-2020 the original author or authors.
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

package hu.perit.spvitamin.spring.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Peter Nagy
 */

@Log4j
class JsonSerializableTest
{
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Getter
    static class Example implements JsonSerializable
    {
        private final String name;
        private final int age;
        private final Date date;
        private final LocalDate localDate;
        private final LocalDateTime localDateTime;

        public Example()
        {
            this.name = null;
            age = 0;
            date = null;
            localDate = null;
            localDateTime = null;
        }

        @Override
        public void finalizeJsonDeserialization()
        {
            log.debug("finalizeJsonDeserialization() called!");
        }
    }

    @Test
    void toJsonRoundtrip() throws IOException
    {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 4, 1, 10, 0, 0);
        cal.set(Calendar.MILLISECOND, 100);
        Example originalObject = new Example("Nagy", 55,
                cal.getTime(),
                LocalDate.of(2020,5,11),
                LocalDateTime.of(2020,5,11, 10, 10, 10));

        String jsonString = originalObject.toJson();
        log.debug(jsonString);
        Example decodedObject = JsonSerializable.fromJson(jsonString, Example.class);
        log.debug("original: " + originalObject.toString());
        log.debug("decoded:  " + decodedObject.toString());
        Assertions.assertEquals(originalObject, decodedObject);
    }

    @Test
    void fromJsonDateWithoutTime() throws IOException
    {
        String jsonString = "{\"name\":\"Nagy\",\"age\":55,\"date\":\"2020-05-01\",\"localDate\":\"2020-05-11\",\"localDateTime\":\"2020-05-11 10:00\"}";
        log.debug(jsonString);
        Example decodedObject = JsonSerializable.fromJson(jsonString, Example.class);
        log.debug("decoded:  " + decodedObject.toString());

        Calendar cal = Calendar.getInstance();
        cal.set(2020, 4, 1, 00, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Assertions.assertEquals(55, decodedObject.age);
        Assertions.assertEquals("Nagy", decodedObject.name);
        Assertions.assertEquals(cal.getTime(), decodedObject.date);
        Assertions.assertEquals(LocalDate.of(2020, 5, 11), decodedObject.localDate);
    }

    @Test
    void fromJsonDateWithTimezone() throws IOException
    {
        String jsonString = "{\"name\":\"Nagy\",\"age\":55,\"date\":\"2020-05-12T09:59:09.276+0000\",\"localDate\":\"2020-05-11\",\"localDateTime\":\"2020-05-11 10:00\"}";
        log.debug(jsonString);
        Example decodedObject = JsonSerializable.fromJson(jsonString, Example.class);
        log.debug("decoded:  " + decodedObject.toString());

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        cal.set(2020, 4, 12, 11, 59, 9);
        cal.set(Calendar.MILLISECOND, 276);

        Assertions.assertEquals(55, decodedObject.age);
        Assertions.assertEquals("Nagy", decodedObject.name);
        Assertions.assertEquals(cal.getTime(), decodedObject.date);
        Assertions.assertEquals(LocalDate.of(2020, 5, 11), decodedObject.localDate);
    }
}