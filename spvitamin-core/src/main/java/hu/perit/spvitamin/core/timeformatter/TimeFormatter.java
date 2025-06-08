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
import org.apache.commons.lang3.time.DurationFormatUtils;

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
        double sec = 1000.0;
        double min = 60 * sec;
        double hour = 60.0 * min;
        if (duration < 1 * sec)
        {
            return String.format("%d ms", duration);
        }
        else if (duration < 1 * min)
        {
            return DurationFormatUtils.formatDuration(duration, "s.S") + " sec";
        }
        else if (duration < 10 * min)
        {
            return DurationFormatUtils.formatDuration(duration, "m:ss.S") + " min";
        }
        else if (duration < 1 * hour)
        {
            return DurationFormatUtils.formatDuration(duration, "m:ss") + " min";
        }
        else if (duration < 24 * hour)
        {
            return DurationFormatUtils.formatDuration(duration, "H:mm:ss") + " hour";
        }

        return DurationFormatUtils.formatDuration(duration, "d:HH:mm:ss") + " day";
    }
}
