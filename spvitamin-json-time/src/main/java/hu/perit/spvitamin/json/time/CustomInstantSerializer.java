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

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.Instant;

public class CustomInstantSerializer extends ValueSerializer<Instant>
{
    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException
    {
        String stringValue = value.toString();
        if (!stringValue.isEmpty() && !stringValue.equals("null"))
        {
            gen.writeString(stringValue);
        }
        else
        {
            gen.writeNull();
        }
    }


    @Override
    public Class<Instant> handledType()
    {
        return Instant.class;
    }

}
