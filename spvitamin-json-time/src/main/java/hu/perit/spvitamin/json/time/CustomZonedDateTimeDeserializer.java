package hu.perit.spvitamin.json.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime>
{
    @Override
    public ZonedDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {

        if (StringUtils.isBlank(jp.getText()))
        {
            return null;
        }

        for (String format : AcceptedDateFormats.getAcceptedIso8601Formats())
        {
            try
            {
                return this.tryParseWithFormat(jp.getText(), format);
            }
            catch (DateTimeParseException ex)
            {
                // not succeeded to parse with this format => trying the next
            }
        }

        // Failed with custom formats, try default
        return InstantDeserializer.ZONED_DATE_TIME.deserialize(jp, ctxt);
    }


    private ZonedDateTime tryParseWithFormat(String value, String format)
    {
        return ZonedDateTime.parse(value, DateTimeFormatter.ofPattern(format));
    }


    @Override
    public Class<ZonedDateTime> handledType()
    {
        return ZonedDateTime.class;
    }
}
