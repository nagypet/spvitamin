package hu.perit.spvitamin.json.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;

public class CustomInstantSerializer extends JsonSerializer<Instant>
{
    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException
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
