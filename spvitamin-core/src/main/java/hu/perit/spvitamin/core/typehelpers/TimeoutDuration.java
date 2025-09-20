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

package hu.perit.spvitamin.core.typehelpers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

/**
 * A wrapper around {@link Duration} that also stores the original time unit.
 * This allows for more accurate rounding and formatting operations.
 */
@Getter
@EqualsAndHashCode
@ToString
public class TimeoutDuration implements TemporalAmount, Comparable<TimeoutDuration>, Serializable
{
    @Serial
    private static final long serialVersionUID = 6118464697600561594L;

    private final Duration duration;
    private final ChronoUnit unit;


    /**
     * Creates an AdvancedDuration with the specified duration and original unit.
     *
     * @param duration the underlying duration
     * @param unit     the original time unit used to create the duration
     */
    private TimeoutDuration(Duration duration, ChronoUnit unit)
    {
        this.duration = duration;
        this.unit = unit;
    }


    /**
     * Creates an AdvancedDuration from a standard Duration with a specified original unit.
     *
     * @param duration the duration to wrap
     * @param unit     the original time unit
     * @return a new AdvancedDuration with the specified original unit
     */
    public static TimeoutDuration of(Duration duration, ChronoUnit unit)
    {
        return new TimeoutDuration(duration, unit);
    }


    public static TimeoutDuration of(Long amount, ChronoUnit unit)
    {
        return new TimeoutDuration(Duration.of(amount, unit), unit);
    }


    /**
     * Gets the value of the requested unit.
     *
     * @param unit the unit to get
     * @return the value of the unit
     */
    public long get(TemporalUnit unit)
    {
        return this.duration.get(unit);
    }


    @Override
    public List<TemporalUnit> getUnits()
    {
        return this.duration.getUnits();
    }


    @Override
    public Temporal addTo(Temporal temporal)
    {
        return this.duration.addTo(temporal);
    }


    @Override
    public Temporal subtractFrom(Temporal temporal)
    {
        return this.duration.subtractFrom(temporal);
    }


    public long toHours()
    {
        return this.duration.toHours();
    }


    public long getSeconds()
    {
        return this.duration.getSeconds();
    }


    public long getMillis()
    {
        return this.duration.toMillis();
    }


    @Override
    public int compareTo(TimeoutDuration o)
    {
        return this.duration.compareTo(o.duration);
    }


    public TimeoutDuration elapsed(Duration duration)
    {
        return TimeoutDuration.of(this.duration.minus(duration), this.unit);
    }


    public Duration getRemainingDurationSince(OffsetDateTime timestamp)
    {
        return getRemainingDurationSince(timestamp, 1);
    }


    public Duration getRemainingDurationSince(OffsetDateTime timestamp, int roundToSmallerUnitCount)
    {
        long elapsedMillis = Duration.between(timestamp, OffsetDateTime.now()).toMillis();
        long remainingMillis = this.duration.toMillis() - elapsedMillis;
        return TimeoutDuration.of(Duration.of(remainingMillis, ChronoUnit.MILLIS), this.unit).getRoundedDuration(roundToSmallerUnitCount);
    }


    public Duration getRoundedDuration()
    {
        return this.getRoundedDuration(1);
    }


    public Duration getRoundedDuration(int roundToSmallerUnitCount)
    {
        // Ensure remaining duration is not negative
        long millis = Math.max(0, this.duration.toMillis());

        // Get the unit to round to based on roundToSmallerUnitCount
        ChronoUnit roundToUnit = getNSmallerUnit(this.unit, roundToSmallerUnitCount);

        // Rounding to the appropriate unit
        return DurationUtils.roundDuration(Duration.ofMillis(millis), roundToUnit);
    }


    /**
     * Returns the next smaller ChronoUnit for the given unit.
     *
     * @param unit the original unit
     * @return the next smaller unit
     */
    private static ChronoUnit getSmallerUnit(ChronoUnit unit)
    {
        return switch (unit)
        {
            case YEARS -> ChronoUnit.MONTHS;
            case MONTHS -> ChronoUnit.DAYS;
            case WEEKS -> ChronoUnit.DAYS;
            case DAYS -> ChronoUnit.HOURS;
            case HOURS -> ChronoUnit.MINUTES;
            case MINUTES -> ChronoUnit.SECONDS;
            case SECONDS -> ChronoUnit.MILLIS;
            default -> unit;
        };
    }


    /**
     * Returns a ChronoUnit that is n units smaller than the given unit.
     * If n is 0, returns the original unit.
     * If n is greater than the number of available smaller units, returns the smallest available unit.
     *
     * @param unit the original unit
     * @param n    the number of units to go smaller
     * @return the unit that is n units smaller
     */
    private static ChronoUnit getNSmallerUnit(ChronoUnit unit, int n)
    {
        if (n <= 0)
        {
            return unit;
        }

        ChronoUnit result = unit;
        for (int i = 0; i < n; i++)
        {
            ChronoUnit smaller = getSmallerUnit(result);
            if (smaller == result)
            {
                // We've reached the smallest unit
                break;
            }
            result = smaller;
        }

        return result;
    }
}
