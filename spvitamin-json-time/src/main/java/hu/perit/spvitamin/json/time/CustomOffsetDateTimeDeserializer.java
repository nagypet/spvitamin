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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime>
{
    @Override
    public OffsetDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        if (StringUtils.isBlank(jp.getText()))
        {
            return null;
        }

        OffsetDateTime offsetDateTime = deserializeInternal(jp, ctxt);

        // Change the offset to the local offset
        ZonedDateTime zonedDateTime = offsetDateTime.atZoneSameInstant(ZoneId.systemDefault());

        return zonedDateTime.toOffsetDateTime();
    }


    private OffsetDateTime deserializeInternal(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
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

        // Failed with custom formats, try default
        return InstantDeserializer.OFFSET_DATE_TIME.deserialize(jp, ctxt);
    }


    private OffsetDateTime tryParseWithPattern(String value, String pattern)
    {
        return OffsetDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
    }


    @Override
    public Class<OffsetDateTime> handledType()
    {
        return OffsetDateTime.class;
    }
}
