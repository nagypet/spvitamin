package hu.perit.spvitamin.spring.resilientjobrunner.service.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ResilientJobParameterTest
{
    @Test
    void testToStringV1()
    {
        ParameterV1 request = ParameterV1.of(1L, 120000L, "file.txt");
        assertThat(request.getVersion()).isEqualTo(1);
        String json = request.toJson();
        log.debug(json);
        assertThat(json).isEqualTo("{\"docId\":1,\"fileSize\":120000,\"originalFileName\":\"file.txt\"}");
    }


    @Test
    void testToStringV2()
    {
        UUID uuid = UUID.fromString("b35c29bd-5194-47b3-a65e-6cca89e0855d");
        ParameterV2 request = ParameterV2.of(1L, 120000L, uuid);
        assertThat(request.getVersion()).isEqualTo(2);
        String json = request.toJson();
        log.debug(json);
        assertThat(json).isEqualTo("{\"docId\":1,\"fileSize\":120000,\"originalFileName\":\"b35c29bd-5194-47b3-a65e-6cca89e0855d\"}");
    }


    @Test
    void testToValueV1FromParameterV1()
    {
        String jsonV1 = "{\"docId\":1,\"fileSize\":120000,\"originalFileName\":\"file.txt\"}";
        ParameterV1 expected = ParameterV1.of(1L, 120000L, "file.txt");
        assertThat(ParameterV1.fromJson(jsonV1, 1, ParameterV1.class)).isEqualTo(expected);
    }


    @Test
    void testToValueV2FromParameterV2()
    {
        String jsonV2 = "{\"docId\":1,\"fileSize\":120000,\"originalFileName\":\"b35c29bd-5194-47b3-a65e-6cca89e0855d\"}";
        UUID uuid = UUID.fromString("b35c29bd-5194-47b3-a65e-6cca89e0855d");
        ParameterV2 expected = ParameterV2.of(1L, 120000L, uuid);
        assertThat(ParameterV2.fromJson(jsonV2, 2, ParameterV2.class)).isEqualTo(expected);
    }


    @Test
    void testToValueV2FromParameterV1()
    {
        String jsonV1 = "{\"docId\":1,\"fileSize\":120000,\"originalFileName\":\"file.txt\"}";
        ParameterV2 expected = ParameterV2.of(1L, 120000L, null);
        assertThat(ParameterV2.fromJson(jsonV1, 1, ParameterV2.class)).isEqualTo(expected);
    }
}
