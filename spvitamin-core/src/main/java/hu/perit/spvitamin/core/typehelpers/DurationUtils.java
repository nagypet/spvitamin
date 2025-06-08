package hu.perit.spvitamin.core.typehelpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A utility class for working with Java's Duration objects.
 * 
 * <p>This class provides methods for formatting, analyzing, and manipulating Duration
 * objects. It includes functionality for human-readable formatting with localization
 * support, detecting the original time unit of a duration, and rounding durations to
 * specific time units.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Convert durations to human-readable strings</li>
 *   <li>Support for English and Hungarian localization</li>
 *   <li>Detect the most likely original time unit of a duration</li>
 *   <li>Round durations to days, hours, minutes, seconds, or milliseconds</li>
 *   <li>Proper handling of singular/plural forms in formatted output</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Format a duration as a human-readable string
 * Duration duration = Duration.ofHours(25).plusMinutes(30);
 * String readable = DurationUtils.getHumanReadableDuration(duration); // "1 day, 1 hour, 30 minutes"
 * 
 * // Detect the original time unit
 * ChronoUnit unit = DurationUtils.detectOriginalUnit(Duration.ofMinutes(60)); // HOURS
 * 
 * // Round a duration to a specific unit
 * Duration rounded = DurationUtils.roundDuration(duration, ChronoUnit.HOURS); // 25 hours
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DurationUtils
{

    private static final Map<String, String[]> LABELS_EN = Map.of(
            "day", new String[]{"day", "days"},
            "hour", new String[]{"hour", "hours"},
            "minute", new String[]{"minute", "minutes"},
            "second", new String[]{"second", "seconds"},
            "millisecond", new String[]{"millisecond", "milliseconds"}
    );

    private static final Map<String, String[]> LABELS_HU = Map.of(
            "day", new String[]{"nap", "nap"},
            "hour", new String[]{"óra", "óra"},
            "minute", new String[]{"perc", "perc"},
            "second", new String[]{"másodperc", "másodperc"},
            "millisecond", new String[]{"ezredmásodperc", "ezredmásodperc"}
    );

    public static String getHumanReadableDuration(Duration duration)
    {
        return getHumanReadableDuration(duration, Locale.US);
    }

    public static String getHumanReadableDuration(Duration duration, Locale locale)
    {
        long totalMillis = duration.toMillis();
        long days = totalMillis / 86_400_000;
        long hours = (totalMillis % 86_400_000) / 3_600_000;
        long minutes = (totalMillis % 3_600_000) / 60_000;
        long seconds = (totalMillis % 60_000) / 1_000;
        long millis = totalMillis % 1_000;

        Map<String, String[]> labels = locale.getLanguage().equals("hu") ? LABELS_HU : LABELS_EN;

        List<String> parts = new ArrayList<>();
        if (days > 0)
        {
            parts.add(days + " " + getLabel(labels, "day", days));
        }
        if (hours > 0)
        {
            parts.add(hours + " " + getLabel(labels, "hour", hours));
        }
        if (minutes > 0)
        {
            parts.add(minutes + " " + getLabel(labels, "minute", minutes));
        }
        if (seconds > 0)
        {
            parts.add(seconds + " " + getLabel(labels, "second", seconds));
        }
        if (millis > 0)
        {
            parts.add(millis + " " + getLabel(labels, "millisecond", millis));
        }

        return parts.isEmpty() ? (locale.getLanguage().equals("hu") ? "0 ezredmásodperc" : "0 milliseconds") :
                String.join(", ", parts);
    }


    public static ChronoUnit detectOriginalUnit(Duration duration)
    {
        long seconds = duration.getSeconds();
        long millis = duration.toMillis();

        if (seconds % 86400 == 0)
        {
            return ChronoUnit.DAYS;
        }
        if (seconds % 3600 == 0)
        {
            return ChronoUnit.HOURS;
        }
        if (seconds % 60 == 0)
        {
            return ChronoUnit.MINUTES;
        }
        if (seconds > 0)
        {
            return ChronoUnit.SECONDS;
        }
        if (millis % 1 == 0)
        {
            return ChronoUnit.MILLIS;
        }

        return ChronoUnit.NANOS;
    }


    public static Duration roundDuration(Duration inputDuration, ChronoUnit roundTo)
    {
        long roundedValue;

        return switch (roundTo)
        {
            case DAYS ->
            {
                roundedValue = inputDuration.toDays();
                yield Duration.ofDays(roundedValue);
            }
            case HOURS ->
            {
                roundedValue = inputDuration.toHours();
                yield Duration.ofHours(roundedValue);
            }
            case MINUTES ->
            {
                roundedValue = inputDuration.toMinutes();
                yield Duration.ofMinutes(roundedValue);
            }
            case SECONDS ->
            {
                roundedValue = inputDuration.getSeconds();
                yield Duration.ofSeconds(roundedValue);
            }
            case MILLIS ->
            {
                roundedValue = inputDuration.toMillis();
                yield Duration.ofMillis(roundedValue);
            }
            default -> throw new IllegalArgumentException("Not supported time unit: " + roundTo);
        };
    }


    private static String getLabel(Map<String, String[]> labels, String key, long value)
    {
        return labels.get(key)[value == 1 ? 0 : 1];
    }
}
