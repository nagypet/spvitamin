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

package hu.perit.spvitamin.core.typehelpers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class LocalDateTimeUtilsTest
{
    @Test
    void testWithMinutes()
    {
        String input = "2021-06-01T07:20+0100";
        OffsetDateTime odt = OffsetDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ"));
        String formatted = LocalDateTimeUtils.format(odt);
        log.debug("{} => {}", input, formatted);
        assertEquals("2021-06-01 07:20:00", formatted);
    }

    @Test
    void testWithSeconds()
    {
        String input = "2021-06-01T07:20:43+0100";
        OffsetDateTime odt = OffsetDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
        String formatted = LocalDateTimeUtils.format(odt);
        log.debug("{} => {}", input, formatted);
        assertEquals("2021-06-01 07:20:43", formatted);
    }

    @Test
    void testInputWithMilliseconds()
    {
        String input = "2021-06-01T07:20:43.962+0100";
        OffsetDateTime odt = OffsetDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        String formatted = LocalDateTimeUtils.format(odt);
        log.debug("{} => {}", input, formatted);
        assertEquals("2021-06-01 07:20:43", formatted);
    }
}
