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

package hu.perit.spvitamin.core.thing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ValueListSerializer extends JsonSerializer<ValueList>
{

    @Override
    public void serialize(ValueList valueList, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        gen.writeStartArray();

        for (Thing element : valueList.getElements())
        {
            if (element instanceof Value value)
            {
                // Serialize Value objects directly
                gen.writeObject(value.getValue());
            }
            else if (element instanceof ValueMap valueMap)
            {
                // Serialize nested ValueMap objects
                gen.writeObject(valueMap);
            }
            else if (element instanceof ValueList nestedList)
            {
                // Serialize nested lists
                gen.writeObject(nestedList);
            }
            else
            {
                // Default serialization for any other Thing type
                gen.writeObject(element);
            }
        }

        gen.writeEndArray();
    }
}
