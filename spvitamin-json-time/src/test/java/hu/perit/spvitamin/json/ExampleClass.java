/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.json;

import hu.perit.spvitamin.json.time.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.StringJoiner;

@AllArgsConstructor
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
        //log.debug("finalizeJsonDeserialization() called!");
    }


    @Override
    public String toString()
    {
        return new StringJoiner(", ", ExampleClass.class.getSimpleName() + "[", "]")
            .add("name='" + name + "'")
            .add("age=" + age)
            .add("date=" + formatDate(date))
            .add("localDate=" + localDate)
            .add("localDateTime=" + localDateTime)
            .add("zonedDateTime=" + zonedDateTime)
            .add("offsetDateTime=" + offsetDateTime)
            .add("instant=" + instant)
            .toString();
    }


    private String formatDate(Date timestamp)
    {
        if (timestamp == null)
        {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DEFAULT_JACKSON_ZONEDTIMESTAMPFORMAT);
        return simpleDateFormat.format(date);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ExampleClass that = (ExampleClass) o;

        Instant thisOffsetInstant = offsetDateTime != null ? offsetDateTime.toInstant() : null;
        Instant thatOffsetInstant = that.offsetDateTime != null ? that.offsetDateTime.toInstant() : null;

        return new EqualsBuilder()
                .append(age, that.age)
                .append(name, that.name)
                .append(date, that.date)
                .append(localDate, that.localDate)
                .append(localDateTime, that.localDateTime)
                .append(zonedDateTime, that.zonedDateTime)
                .append(thisOffsetInstant, thatOffsetInstant)
                .append(instant, that.instant)
                .isEquals();
    }


    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(age)
                .append(date)
                .append(localDate)
                .append(localDateTime)
                .append(zonedDateTime)
                .append(offsetDateTime)
                .append(instant)
                .toHashCode();
    }
}
