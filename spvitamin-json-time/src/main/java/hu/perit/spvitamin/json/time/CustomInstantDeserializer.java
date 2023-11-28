package hu.perit.spvitamin.json.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomInstantDeserializer extends JsonDeserializer<Instant>
{
    @Override
    public Instant deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {

        if (StringUtils.isBlank(jp.getText()))
        {
            return null;
        }

        for (String pattern : AdditionalDateFormats.getPatterns())
        {
            try
            {
                return this.tryParseWithPattern(jp.getText(), pattern);
            }
            catch (Exception ex)
            {
                // not succeeded to parse with this format => trying the next
            }
        }

        // Try to read with time zone offset
        try
        {
            OffsetDateTime offsetDateTime = InstantDeserializer.OFFSET_DATE_TIME.deserialize(jp, ctxt);
            return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toInstant();
        }
        catch (Exception ex)
        {
            // not succeeded to parse with this format => trying the next
        }

        // Failed with custom formats, try default
        return InstantDeserializer.INSTANT.deserialize(jp, ctxt);
    }


    private Instant tryParseWithPattern(String value, String pattern)
    {
        return Instant.from(ZonedDateTime.parse(value, DateTimeFormatter.ofPattern(pattern)));
    }


    @Override
    public Class<Instant> handledType()
    {
        return Instant.class;
    }
}
