package hu.perit.spvitamin.spring.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import hu.perit.spvitamin.spring.config.Constants;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class CustomOffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime>
{
    @Override
    public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        String stringValue = value.format(DateTimeFormatter.ofPattern(Constants.DEFAULT_JACKSON_ZONEDTIMESTAMPFORMAT));
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
    public Class<OffsetDateTime> handledType()
    {
        return OffsetDateTime.class;
    }

}
