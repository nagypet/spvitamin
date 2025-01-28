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

package hu.perit.spvitamin.core.timeformatter;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class TimeFormatterTest
{

    @Test
    void formattedElapsedTime()
    {
        String s = TimeFormatter.getHumanReadableDuration(11);
        log.debug(s);
        assertThat(s).isEqualTo("11 ms");

        s = TimeFormatter.getHumanReadableDuration(123);
        log.debug(s);
        assertThat(s).isEqualTo("123 ms");

        s = TimeFormatter.getHumanReadableDuration(1234);
        log.debug(s);
        assertThat(s).isEqualTo("1.234 sec");

        s = TimeFormatter.getHumanReadableDuration(12345);
        log.debug(s);
        assertThat(s).isEqualTo("12.345 sec");

        s = TimeFormatter.getHumanReadableDuration(123456);
        log.debug(s);
        assertThat(s).isEqualTo("2:03.456 min");

        s = TimeFormatter.getHumanReadableDuration(1234567);
        log.debug(s);
        assertThat(s).isEqualTo("20:34 min");

        s = TimeFormatter.getHumanReadableDuration(12345678);
        log.debug(s);
        assertThat(s).isEqualTo("3:25:45 hour");

        s = TimeFormatter.getHumanReadableDuration(123456789);
        log.debug(s);
        assertThat(s).isEqualTo("1:10:17:36 day");

        s = TimeFormatter.getHumanReadableDuration(1234567899);
        log.debug(s);
        assertThat(s).isEqualTo("14:06:56:07 day");
    }
}
