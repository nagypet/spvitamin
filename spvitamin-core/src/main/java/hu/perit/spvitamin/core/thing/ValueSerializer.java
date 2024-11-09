package hu.perit.spvitamin.core.thing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ValueSerializer extends JsonSerializer<Value>
{
    @Override
    public void serialize(Value value, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        // Csak a "value" mezőt írjuk ki közvetlenül
        gen.writeObject(value.getValue());
    }
}
