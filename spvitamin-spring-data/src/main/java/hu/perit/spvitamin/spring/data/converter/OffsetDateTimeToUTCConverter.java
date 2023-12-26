package hu.perit.spvitamin.spring.data.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Converter(autoApply = true)
public class OffsetDateTimeToUTCConverter implements AttributeConverter<OffsetDateTime, LocalDateTime>
{
    @Override
    public LocalDateTime convertToDatabaseColumn(OffsetDateTime offsetDateTime)
    {
        if (offsetDateTime == null)
        {
            return null;
        }

        return offsetDateTime.atZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(LocalDateTime localDateTime)
    {
        if (localDateTime == null)
        {
            return null;
        }

        ZonedDateTime utc = localDateTime.atZone(ZoneId.of("UTC"));
        return utc.withZoneSameInstant(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
