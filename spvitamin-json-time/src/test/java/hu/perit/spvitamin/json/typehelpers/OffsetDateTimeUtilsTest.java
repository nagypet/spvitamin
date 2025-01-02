package hu.perit.spvitamin.json.typehelpers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OffsetDateTimeUtilsTest
{
    @Test
    void test()
    {
        assertThat(OffsetDateTimeUtils.fromString("2024.12.09")).isEqualTo("2024-12-09T00:00+01:00");
        assertThat(OffsetDateTimeUtils.fromString("2024-12-09")).isEqualTo("2024-12-09T00:00+01:00");
        assertThat(OffsetDateTimeUtils.fromString("2024-12-09 10:00:00")).isEqualTo("2024-12-09T10:00+01:00");
        assertThat(OffsetDateTimeUtils.fromString("2024-12-09T10:00:00Z")).isEqualTo("2024-12-09T11:00+01:00");
        // RFC_1123_DATE_TIME
        assertThat(OffsetDateTimeUtils.fromString("Mon, 9 Dec 2024 09:07:54 +0100")).isEqualTo("2024-12-09T09:07:54+01:00");
        assertThat(OffsetDateTimeUtils.fromString("Mon, 9 Dec 2024 09:07:54 GMT")).isEqualTo("2024-12-09T10:07:54+01:00");
        assertThat(OffsetDateTimeUtils.fromString("Mon, 9 Dec 2024 09:07:54 UTC")).isEqualTo("2024-12-09T10:07:54+01:00");
    }

    @Test
    void testFromLocalDateTime()
    {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 12, 13, 10, 0, 0);
        assertThat(OffsetDateTimeUtils.fromLocalDateTime(localDateTime)).isEqualTo("2024-12-13T10:00+01:00");
    }


    @Test
    void testFromTimestamp()
    {
        Timestamp timestamp = new Timestamp(124, 11, 13, 10, 0, 0, 0);
        assertThat(OffsetDateTimeUtils.fromTimestamp(timestamp)).isEqualTo("2024-12-13T10:00+01:00");
    }
}
