package hu.perit.spvitamin.core.timeformatter;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class TimeFormatterTest
{

    @Test
    void formattedElapsedTime()
    {
        String s = TimeFormatter.getHumanReadableDuration(11);
        log.debug(s);
        assertThat(s).isEqualTo("11 ms");

        s = TimeFormatter.getHumanReadableDuration(123);
        log.debug(s);
        assertThat(s).isEqualTo("123 ms");

        s = TimeFormatter.getHumanReadableDuration(1234);
        log.debug(s);
        assertThat(s).isEqualTo("1.234 sec");

        s = TimeFormatter.getHumanReadableDuration(12345);
        log.debug(s);
        assertThat(s).isEqualTo("12.345 sec");

        s = TimeFormatter.getHumanReadableDuration(123456);
        log.debug(s);
        assertThat(s).isEqualTo("2:03.456 min");

        s = TimeFormatter.getHumanReadableDuration(1234567);
        log.debug(s);
        assertThat(s).isEqualTo("20:34 min");

        s = TimeFormatter.getHumanReadableDuration(12345678);
        log.debug(s);
        assertThat(s).isEqualTo("3:25:45 hour");

        s = TimeFormatter.getHumanReadableDuration(123456789);
        log.debug(s);
        assertThat(s).isEqualTo("1:10:17:36 day");

        s = TimeFormatter.getHumanReadableDuration(1234567899);
        log.debug(s);
        assertThat(s).isEqualTo("14:06:56:07 day");
    }
}
