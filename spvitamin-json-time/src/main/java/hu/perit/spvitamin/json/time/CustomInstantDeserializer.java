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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ext.javatime.deser.InstantDeserializer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class CustomInstantDeserializer extends ValueDeserializer<Instant>
{
    @Override
    public Instant deserialize(JsonParser jp, DeserializationContext ctxt)
    {

        if (StringUtils.isBlank(jp.getString()))
        {
            return null;
        }

        for (String pattern : AdditionalDateFormats.getPatterns())
        {
            try
            {
                return this.tryParseWithPattern(jp.getString(), pattern);
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
            return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toInstant();
        }
        catch (Exception ex)
        {
            // not succeeded to parse with this format => trying the next
        }

        // Failed with custom formats, try default
        try
        {
            return InstantDeserializer.INSTANT.deserialize(jp, ctxt);
        }
        catch (Exception ex)
        {
            // falling back to LocalDateTime
        }

        // Legacy: timestamp in LocalDateTime format (ex. 2025-09-25T05:39:59.802)
        LocalDateTime ldt = LocalDateTime.parse(jp.getString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
        log.warn("Deserializing Instant from json timestamp without time zone! {} => {}", jp.getString(), instant.toString());
        return instant;
    }


    private Instant tryParseWithPattern(String value, String pattern)
    {
        return Instant.from(ZonedDateTime.parse(value, DateTimeFormatter.ofPattern(pattern)));
    }


    @Override
    public Class<Instant> handledType()
    {
        return Instant.class;
    }
}
