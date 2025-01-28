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

package hu.perit.spvitamin.spring.metrics;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static hu.perit.spvitamin.spring.metrics.LocalDateTimeEx.isBetween;
import static hu.perit.spvitamin.spring.metrics.LocalDateTimeEx.max;
import static hu.perit.spvitamin.spring.metrics.LocalDateTimeEx.min;


/**
 * @author Peter Nagy
 */

@Getter
public class MeasurementSession
{
    private LocalDateTime start;
    private LocalDateTime end;
    private int count = 0;

    public boolean isOverlapping(MeasurementItem item)
    {
        if (this.start == null || this.end == null)
        {
            return true;
        }

        return isBetween(item.getStartTime(), this.start, this.end) || isBetween(item.getEndTime(), this.start, this.end);
    }

    public boolean isOverlapping(MeasurementSession other)
    {
        if (this.start == null || this.end == null || other == null || other.start == null || other.end == null)
        {
            return false;
        }

        if (other == this)
        {
            return false;
        }

        return isBetween(other.start, this.start, this.end) || isBetween(other.end, this.start, this.end);
    }

    public void addItem(MeasurementItem item)
    {
        if (this.start == null || item.getStartTime().isBefore(this.start))
        {
            this.start = item.getStartTime();
        }

        if (this.end == null || item.getEndTime().isAfter(this.end))
        {
            this.end = item.getEndTime();
        }

        count++;
    }

    public long getDuration()
    {
        if (this.start == null || this.end == null)
        {
            return 0;
        }

        return start.until(end, ChronoUnit.MILLIS);
    }


    public void merge(MeasurementSession other)
    {
        this.count += other.count;
        this.start = min(this.start, other.start);
        this.end = max(this.end, other.end);
    }

    @Override
    public String toString()
    {
        return "MeasurementSession{" +
                "start=" + start +
                ", end=" + end +
                ", count=" + count +
                '}';
    }
}
