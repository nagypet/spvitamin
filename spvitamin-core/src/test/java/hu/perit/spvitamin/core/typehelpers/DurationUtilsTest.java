package hu.perit.spvitamin.core.typehelpers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DurationUtilsTest
{
    @Test
    void testEn()
    {
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(1))).isEqualTo("1 millisecond");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(11))).isEqualTo("11 milliseconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(111))).isEqualTo("111 milliseconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(1111))).isEqualTo("1 second, 111 milliseconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(11111))).isEqualTo("11 seconds, 111 milliseconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(111111))).isEqualTo("1 minute, 51 seconds, 111 milliseconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(1111111))).isEqualTo("18 minutes, 31 seconds, 111 milliseconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(11111111))).isEqualTo("3 hours, 5 minutes, 11 seconds, 111 milliseconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(111111111))).isEqualTo("1 day, 6 hours, 51 minutes, 51 seconds, 111 milliseconds");

        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(1))).isEqualTo("1 second");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(11))).isEqualTo("11 seconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(111))).isEqualTo("1 minute, 51 seconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(1111))).isEqualTo("18 minutes, 31 seconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(11111))).isEqualTo("3 hours, 5 minutes, 11 seconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(111111))).isEqualTo("1 day, 6 hours, 51 minutes, 51 seconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(1111111))).isEqualTo("12 days, 20 hours, 38 minutes, 31 seconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(11111111))).isEqualTo("128 days, 14 hours, 25 minutes, 11 seconds");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(60))).isEqualTo("1 minute");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(3600))).isEqualTo("1 hour");

        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(1))).isEqualTo("1 minute");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(11))).isEqualTo("11 minutes");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(111))).isEqualTo("1 hour, 51 minutes");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(1111))).isEqualTo("18 hours, 31 minutes");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(11111))).isEqualTo("7 days, 17 hours, 11 minutes");

        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofHours(1))).isEqualTo("1 hour");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofHours(11))).isEqualTo("11 hours");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofHours(111))).isEqualTo("4 days, 15 hours");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofHours(1111))).isEqualTo("46 days, 7 hours");
    }


    @Test
    void testHu()
    {
        Locale hu = Locale.of("hu");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(1), hu)).isEqualTo("1 ezredmásodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(11), hu)).isEqualTo("11 ezredmásodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(111), hu)).isEqualTo("111 ezredmásodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(1111), hu)).isEqualTo("1 másodperc, 111 ezredmásodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(11111), hu)).isEqualTo("11 másodperc, 111 ezredmásodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(111111), hu)).isEqualTo("1 perc, 51 másodperc, 111 ezredmásodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(1111111), hu)).isEqualTo("18 perc, 31 másodperc, 111 ezredmásodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(11111111), hu)).isEqualTo("3 óra, 5 perc, 11 másodperc, 111 ezredmásodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMillis(111111111), hu)).isEqualTo("1 nap, 6 óra, 51 perc, 51 másodperc, 111 ezredmásodperc");

        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(1), hu)).isEqualTo("1 másodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(11), hu)).isEqualTo("11 másodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(111), hu)).isEqualTo("1 perc, 51 másodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(1111), hu)).isEqualTo("18 perc, 31 másodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(11111), hu)).isEqualTo("3 óra, 5 perc, 11 másodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(111111), hu)).isEqualTo("1 nap, 6 óra, 51 perc, 51 másodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(1111111), hu)).isEqualTo("12 nap, 20 óra, 38 perc, 31 másodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(11111111), hu)).isEqualTo("128 nap, 14 óra, 25 perc, 11 másodperc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(60), hu)).isEqualTo("1 perc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofSeconds(3600), hu)).isEqualTo("1 óra");

        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(1), hu)).isEqualTo("1 perc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(11), hu)).isEqualTo("11 perc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(111), hu)).isEqualTo("1 óra, 51 perc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(1111), hu)).isEqualTo("18 óra, 31 perc");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofMinutes(11111), hu)).isEqualTo("7 nap, 17 óra, 11 perc");

        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofHours(1), hu)).isEqualTo("1 óra");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofHours(11), hu)).isEqualTo("11 óra");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofHours(111), hu)).isEqualTo("4 nap, 15 óra");
        assertThat(DurationUtils.getHumanReadableDuration(Duration.ofHours(1111), hu)).isEqualTo("46 nap, 7 óra");
    }
}
