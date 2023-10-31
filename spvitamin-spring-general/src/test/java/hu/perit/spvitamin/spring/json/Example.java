package hu.perit.spvitamin.spring.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
@Slf4j
class Example implements JsonSerializable
{
    private final String name;
    private final int age;
    private final Date date;
    private final LocalDate localDate;
    private final LocalDateTime localDateTime;
    private final ZonedDateTime zonedDateTime;

    public Example()
    {
        this.name = null;
        age = 0;
        date = null;
        localDate = null;
        localDateTime = null;
        zonedDateTime = null;
    }

    @Override
    public void finalizeJsonDeserialization()
    {
        log.debug("finalizeJsonDeserialization() called!");
    }
}
