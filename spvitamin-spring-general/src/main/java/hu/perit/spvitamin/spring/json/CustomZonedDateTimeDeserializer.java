package hu.perit.spvitamin.spring.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
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

        DateTimeParseException exception = null;
        for (String format : AcceptedDateFormats.getAcceptedLocalDateFormats())
        {
            try
            {
                return this.tryParseWithFormat(jp.getText(), format);
            }
            catch (DateTimeParseException ex)
            {
                // not succeeded to parse with this format => trying the next
                exception = ex;
            }
        }
        throw new InvalidFormatException(jp, exception != null ? exception.getMessage() : "Invalid ZonedDateTime format!", jp.getText(),
            LocalDateTime.class);
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
