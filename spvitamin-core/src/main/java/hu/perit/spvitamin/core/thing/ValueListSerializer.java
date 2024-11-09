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
