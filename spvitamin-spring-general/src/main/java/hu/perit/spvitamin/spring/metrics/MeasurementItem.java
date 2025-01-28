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

import org.apache.commons.lang3.time.StopWatch;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Nagy
 */

public class MeasurementItem
{
    private LocalDateTime start;
    private StopWatch timer = new StopWatch();
    private long duration;

    public MeasurementItem()
    {
        this.start = LocalDateTime.now();
        this.timer.start();
        this.duration = 0;
    }

    // Only for testing
    public MeasurementItem(LocalDateTime start, long duration)
    {
        this.start = start;
        this.duration = duration;
    }

    public void stop()
    {
        if (this.timer.isStarted() && !this.timer.isStopped())
        {
            this.timer.stop();
            this.duration = this.timer.getTime(TimeUnit.MILLISECONDS);
        }
    }

    public LocalDateTime getStartTime()
    {
        return this.start;
    }

    public LocalDateTime getEndTime()
    {
        if (!this.timer.isStopped())
        {
            throw new IllegalStateException("Measuring is in progress!");
        }

        return start.plus(this.getDuration(), ChronoUnit.MILLIS);
    }

    // Returns the execution time in ms.
    public long getDuration()
    {
        return this.duration;
    }

    @Override
    public String toString()
    {
        return "ExecutionTimeForMetrics{" +
                "start=" + start +
                ", duration=" + duration +
                '}';
    }
}
