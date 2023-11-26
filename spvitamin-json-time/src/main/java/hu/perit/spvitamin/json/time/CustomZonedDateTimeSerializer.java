package hu.perit.spvitamin.json.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime>
{
    @Override
    public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException
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
    public Class<ZonedDateTime> handledType()
    {
        return ZonedDateTime.class;
    }

}
