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
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * @author Peter Nagy
 */

public class CustomDateDeserializer extends JsonDeserializer<Date>
{

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {

        if (StringUtils.isBlank(jp.getText()))
        {
            return null;
        }

        try
        {
            return DateDeserializers.DateDeserializer.instance.deserialize(jp, ctxt);
        }
        catch (Exception e)
        {
            // The standard deserializer failed, try the next one
        }

        // Trying to deserialize as an Instant
        try
        {
            CustomInstantDeserializer customInstantDeserializer = new CustomInstantDeserializer();
            Instant instant = customInstantDeserializer.deserialize(jp, ctxt);
            return Date.from(instant);
        }
        catch (Exception e)
        {
            // Cannot deserialize as an Instant, try the next one
        }

        // Trying to deserialize as a LocalDateTime
        CustomLocalDateTimeDeserializer customLocalDateTimeDeserializer = new CustomLocalDateTimeDeserializer();
        LocalDateTime localDateTime = customLocalDateTimeDeserializer.deserialize(jp, ctxt);
        Instant instant = localDateTime.toInstant(ZoneOffset.of("Z"));
        return Date.from(instant);
    }


    private boolean containsTimeZone(String text)
    {
        return text.contains("+") || text.endsWith("Z");
    }


    @Override
    public Class<Date> handledType()
    {
        return Date.class;
    }
}
