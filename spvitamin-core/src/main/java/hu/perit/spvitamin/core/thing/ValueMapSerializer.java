package hu.perit.spvitamin.core.thing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class ValueMapSerializer extends JsonSerializer<ValueMap>
{

    @Override
    public void serialize(ValueMap valueMap, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        gen.writeStartObject();  // Új JSON objektum kezdete

        // Map elemek közvetlen kiírása
        for (Map.Entry<String, Thing> entry : valueMap.getProperties().entrySet())
        {
            gen.writeObjectField(entry.getKey(), entry.getValue());
        }

        gen.writeEndObject();  // JSON objektum vége
    }
}
