/*
 * Copyright 2020-2023 the original author or authors.
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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Peter Nagy
 */

@Slf4j
class JsonSerializableTest
{
    @Test
    void toJsonRoundtrip() throws IOException
    {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.MAY, 1, 10, 0, 0);
        cal.set(Calendar.MILLISECOND, 100);
        // originalObject
        Example originalObject = new Example("Nagy", 55,
            cal.getTime(),
            LocalDate.of(2020, 5, 11),
            LocalDateTime.of(2020, 5, 11, 10, 10, 10),
            ZonedDateTime.of(2020, 5, 11, 10, 10, 10, 0, ZoneId.of("+0200")));

        String originalObjectJson = originalObject.toJson();
        log.debug(originalObjectJson);
        // decodedObject
        Example decodedObject = JsonSerializable.fromJson(originalObjectJson, Example.class);
        log.debug("original: {}", originalObject.toString());
        log.debug("decoded:  {}", decodedObject.toString());
        assertThat(decodedObject).isEqualTo(originalObject);
    }
}
