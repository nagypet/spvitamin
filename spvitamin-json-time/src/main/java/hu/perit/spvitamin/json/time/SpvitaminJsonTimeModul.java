package hu.perit.spvitamin.json.time;


import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class SpvitaminJsonTimeModul extends SimpleModule
{
    public SpvitaminJsonTimeModul()
    {
        super.addSerializer(new CustomDateSerializer());
        super.addSerializer(new CustomLocalDateSerializer());
        super.addSerializer(new CustomLocalDateTimeSerializer());
        super.addSerializer(new CustomZonedDateTimeSerializer());
        super.addSerializer(new CustomOffsetDateTimeSerializer());
        super.addSerializer(new CustomInstantSerializer());
        super.addDeserializer(Date.class, new CustomDateDeserializer());
        super.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        super.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        super.addDeserializer(ZonedDateTime.class, new CustomZonedDateTimeDeserializer());
        super.addDeserializer(OffsetDateTime.class, new CustomOffsetDateTimeDeserializer());
        super.addDeserializer(Instant.class, new CustomInstantDeserializer());
    }
}
