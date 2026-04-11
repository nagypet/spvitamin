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

package hu.perit.spvitamin.core.timeformatter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A utility class for formatting time durations in a human-readable format.
 *
 * <p>This class provides methods to convert time durations (in milliseconds) into
 * human-readable strings with appropriate units. It automatically selects the most
 * appropriate time unit (milliseconds, seconds, minutes, hours, or days) based on
 * the magnitude of the duration.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Format elapsed time since a start timestamp</li>
 *   <li>Convert raw millisecond durations to human-readable format</li>
 *   <li>Automatic unit selection based on duration magnitude</li>
 *   <li>Appropriate precision for different duration ranges</li>
 * </ul>
 *
 * <p>Example outputs:</p>
 * <ul>
 *   <li>500 ms</li>
 *   <li>2.5 sec</li>
 *   <li>3:45.2 min</li>
 *   <li>1:23:45 hour</li>
 *   <li>2:05:30:00 day</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeFormatter
{
    public static String formattedElapsedTime(long start)
    {
        return getHumanReadableDuration(System.currentTimeMillis() - start);
    }


    public static String getHumanReadableDuration(long duration)
    {
        long sec = 1_000L;
        long min = 60L * sec;
        long hour = 60L * min;
        long day = 24L * hour;

        if (duration < sec)
        {
            return String.format("%d ms", duration);
        }
        else if (duration < min)
        {
            long seconds = duration / sec;
            long hundredths = (duration % sec) / 10L;
            return hundredths == 0
                    ? String.format("%d sec", seconds)
                    : String.format("%d.%02d sec", seconds, hundredths);
        }
        else if (duration < 10 * min)
        {
            long minutes = duration / min;
            long seconds = (duration % min) / sec;
            return String.format("%d:%02d min", minutes, seconds);
        }
        else if (duration < hour)
        {
            long minutes = duration / min;
            long seconds = (duration % min) / sec;
            return String.format("%d:%02d min", minutes, seconds);
        }
        else if (duration < day)
        {
            long hours = duration / hour;
            long minutes = (duration % hour) / min;
            long seconds = (duration % min) / sec;
            return String.format("%d:%02d:%02d hour", hours, minutes, seconds);
        }

        long days = duration / day;
        long hours = (duration % day) / hour;
        long minutes = (duration % hour) / min;
        long seconds = (duration % min) / sec;
        return String.format("%d:%02d:%02d:%02d day", days, hours, minutes, seconds);
    }
}
