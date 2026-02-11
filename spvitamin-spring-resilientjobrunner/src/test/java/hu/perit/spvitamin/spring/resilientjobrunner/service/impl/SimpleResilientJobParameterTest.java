package hu.perit.spvitamin.spring.resilientjobrunner.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class SimpleResilientJobParameterTest
{
    @Test
    void testStringData()
    {
        SimpleResilientJobParameter<String> origParam = SimpleResilientJobParameter.of("test");
        String json = origParam.toJson();
        log.debug(json);
        SimpleResilientJobParameter<String> decodedParam = SimpleResilientJobParameter.fromJson(json, String.class);
        log.debug(decodedParam.getData());
        assertThat(decodedParam).isEqualTo(origParam);
    }


    @Test
    void testLongData()
    {
        SimpleResilientJobParameter<Long> origParam = SimpleResilientJobParameter.of(1234L);
        String json = origParam.toJson();
        log.debug(json);
        SimpleResilientJobParameter<Long> decodedParam = SimpleResilientJobParameter.fromJson(json, Long.class);
        assertThat(decodedParam.getData()).isNotNull();
        log.debug(decodedParam.getData().toString());
        assertThat(decodedParam).isEqualTo(origParam);
    }


    @Test
    void testMismatchedData()
    {
        SimpleResilientJobParameter<String> origParam = SimpleResilientJobParameter.of("alma");
        String json = origParam.toJson();
        log.debug(json);
        assertThatThrownBy(() -> SimpleResilientJobParameter.fromJson(json, Long.class)).isInstanceOf(ClassCastException.class);
    }
}
