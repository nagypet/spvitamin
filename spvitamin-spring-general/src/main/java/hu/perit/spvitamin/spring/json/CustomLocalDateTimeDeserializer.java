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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import hu.perit.spvitamin.spring.config.Constants;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Nagy
 */

@Log4j
public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<String> formats = new ArrayList<>();
        formats.add(Constants.DEFAULT_JACKSON_TIMESTAMPFORMAT);
        formats.add("yyyy-MM-dd HH:mm:ss");
        formats.add("yyyy-MM-dd HH:mm");

        DateTimeParseException exception = null;
        for (String format : formats) {
            try {
                return this.tryParseWithFormat(jp.getText(), format);
            }
            catch (DateTimeParseException ex) {
                // nem sikerült parse-olni, próbáljuk a következő formátummal
                exception = ex;
            }
        }
        throw new InvalidFormatException(jp, exception != null ? exception.getMessage() : "Invalid LocalDateTime format!", jp.getText(), LocalDateTime.class);
    }

    private LocalDateTime tryParseWithFormat(String value, String format) {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(format));
    }

    @Override
    public Class<LocalDateTime> handledType() {
        return LocalDateTime.class;
    }
}
