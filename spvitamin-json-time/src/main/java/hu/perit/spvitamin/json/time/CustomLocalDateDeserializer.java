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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Peter Nagy
 */

public class CustomLocalDateDeserializer extends JsonDeserializer<LocalDate>
{

    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        if (StringUtils.isBlank(jp.getText()))
        {
            return null;
        }

        for (String pattern : AdditionalDateFormats.getPatterns())
        {
            try
            {
                return this.tryParseWithPattern(jp.getText(), pattern);
            }
            catch (Exception ex)
            {
                // not succeeded to parse with this format => trying the next
            }
        }

        // Try to read with time zone offset
        try
        {
            OffsetDateTime offsetDateTime = InstantDeserializer.OFFSET_DATE_TIME.deserialize(jp, ctxt);
            return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        }
        catch (Exception ex)
        {
            // not succeeded to parse with this format => trying the next
        }

        // Failed with custom formats, try default
        return LocalDateDeserializer.INSTANCE.deserialize(jp, ctxt);
    }


    private LocalDate tryParseWithPattern(String value, String pattern)
    {
        try
        {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
            return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        }
        catch (Exception ex)
        {
            // Cannot deserialize as OffsetDateTime, let's try as LocalDateTime
        }

        return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
    }


    @Override
    public Class<LocalDate> handledType()
    {
        return LocalDate.class;
    }
}
