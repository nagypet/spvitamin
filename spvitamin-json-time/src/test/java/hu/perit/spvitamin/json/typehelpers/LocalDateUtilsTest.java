package hu.perit.spvitamin.json.typehelpers;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateUtilsTest
{
    @Test
    void test()
    {
        assertThat(LocalDateUtils.fromString("2024.12.09")).isEqualTo("2024-12-09");
        assertThat(LocalDateUtils.fromString("09.12.2024")).isEqualTo("2024-12-09");
        assertThat(LocalDateUtils.fromString("12/09/2024")).isEqualTo("2024-12-09");
        assertThat(LocalDateUtils.fromString("2024-12-09")).isEqualTo("2024-12-09");
        assertThat(LocalDateUtils.fromString("2024-12-09 10:00:00")).isEqualTo("2024-12-09");
        assertThat(LocalDateUtils.fromString("2024-12-09T10:00:00Z")).isEqualTo("2024-12-09");
        // RFC_1123_DATE_TIME
        assertThat(LocalDateUtils.fromString("Mon, 9 Dec 2024 09:07:54 +0100")).isEqualTo("2024-12-09");
        assertThat(LocalDateUtils.fromString("Mon, 9 Dec 2024 09:07:54 GMT")).isEqualTo("2024-12-09");
        assertThat(LocalDateUtils.fromString("Mon, 9 Dec 2024 09:07:54 UTC")).isEqualTo("2024-12-09");
    }


    @Test
    void testFromLocalDateTime()
    {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 12, 13, 10, 0, 0);
        assertThat(LocalDateUtils.fromLocalDateTime(localDateTime)).isEqualTo("2024-12-13");
    }


    @Test
    void testFromTimestamp()
    {
        Timestamp timestamp = new Timestamp(124, 11, 13, 10, 0, 0, 0);
        assertThat(LocalDateUtils.fromTimestamp(timestamp)).isEqualTo("2024-12-13");
    }

}
