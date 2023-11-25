package hu.perit.spvitamin.spring.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomInstantDeserializer extends JsonDeserializer<Instant>
{
    @Override
    public Instant deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {

        if (StringUtils.isBlank(jp.getText()))
        {
            return null;
        }

        DateTimeParseException exception = null;
        for (String format : AcceptedDateFormats.getAcceptedIso8601Formats())
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
        throw new InvalidFormatException(jp, exception != null ? exception.getMessage() : "Invalid Instant format!", jp.getText(),
            Instant.class);
    }


    private Instant tryParseWithFormat(String value, String format)
    {
        if (AcceptedDateFormats.JAVA_STANDARD.equals(format))
        {
            return Instant.parse(value);
        }

        return Instant.from(ZonedDateTime.parse(value, DateTimeFormatter.ofPattern(format)));
    }


    @Override
    public Class<Instant> handledType()
    {
        return Instant.class;
    }
}
