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

package hu.perit.spvitamin.json.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author Peter Nagy
 */

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
{

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {

        if (StringUtils.isBlank(jp.getText()))
        {
            return null;
        }

        for (String format : AcceptedDateFormats.getAcceptedIso8601Formats())
        {
            try
            {
                return this.tryParseWithFormat(jp.getText(), format);
            }
            catch (DateTimeParseException ex)
            {
                // not succeeded to parse with this format => trying the next
            }
        }

        // Failed with custom formats, try default
        return LocalDateTimeDeserializer.INSTANCE.deserialize(jp, ctxt);
    }


    private LocalDateTime tryParseWithFormat(String value, String format)
    {
        try
        {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(value, DateTimeFormatter.ofPattern(format));
            return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
        catch (DateTimeParseException ex)
        {
            // Cannot deserialize as OffsetDateTime, let's try as LocalDateTime
        }

        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(format));
    }


    @Override
    public Class<LocalDateTime> handledType()
    {
        return LocalDateTime.class;
    }
}
