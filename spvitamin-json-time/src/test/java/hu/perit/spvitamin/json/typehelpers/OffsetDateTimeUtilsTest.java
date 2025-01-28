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

package hu.perit.spvitamin.json.typehelpers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OffsetDateTimeUtilsTest
{
    @Test
    void test()
    {
        assertThat(OffsetDateTimeUtils.fromString("2024.12.09")).isEqualTo("2024-12-09T00:00+01:00");
        assertThat(OffsetDateTimeUtils.fromString("2024-12-09")).isEqualTo("2024-12-09T00:00+01:00");
        assertThat(OffsetDateTimeUtils.fromString("2024-12-09 10:00:00")).isEqualTo("2024-12-09T10:00+01:00");
        assertThat(OffsetDateTimeUtils.fromString("2024-12-09T10:00:00Z")).isEqualTo("2024-12-09T11:00+01:00");
        // RFC_1123_DATE_TIME
        assertThat(OffsetDateTimeUtils.fromString("Mon, 9 Dec 2024 09:07:54 +0100")).isEqualTo("2024-12-09T09:07:54+01:00");
        assertThat(OffsetDateTimeUtils.fromString("Mon, 9 Dec 2024 09:07:54 GMT")).isEqualTo("2024-12-09T10:07:54+01:00");
        assertThat(OffsetDateTimeUtils.fromString("Mon, 9 Dec 2024 09:07:54 UTC")).isEqualTo("2024-12-09T10:07:54+01:00");
    }

    @Test
    void testFromLocalDateTime()
    {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 12, 13, 10, 0, 0);
        assertThat(OffsetDateTimeUtils.fromLocalDateTime(localDateTime)).isEqualTo("2024-12-13T10:00+01:00");
    }


    @Test
    void testFromTimestamp()
    {
        Timestamp timestamp = new Timestamp(124, 11, 13, 10, 0, 0, 0);
        assertThat(OffsetDateTimeUtils.fromTimestamp(timestamp)).isEqualTo("2024-12-13T10:00+01:00");
    }
}
