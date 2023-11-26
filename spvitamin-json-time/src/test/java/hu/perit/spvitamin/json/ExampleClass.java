package hu.perit.spvitamin.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
@Slf4j
public class ExampleClass implements JsonSerializable
{
    private final String name;
    private final int age;
    private final Date date;
    private final LocalDate localDate;
    private final LocalDateTime localDateTime;
    private final ZonedDateTime zonedDateTime;
    private final OffsetDateTime offsetDateTime;
    private final Instant instant;

    public ExampleClass()
    {
        this.name = null;
        this.age = 0;
        this.date = null;
        this.localDate = null;
        this.localDateTime = null;
        this.zonedDateTime = null;
        this.offsetDateTime = null;
        this.instant = null;
    }

    @Override
    public void finalizeJsonDeserialization()
    {
        log.debug("finalizeJsonDeserialization() called!");
    }
}
