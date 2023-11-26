package hu.perit.spvitamin.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import hu.perit.spvitamin.json.time.Constants;
import hu.perit.spvitamin.json.time.SpvitaminJsonTimeModul;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpvitaminObjectMapper
{
    public enum MapperType
    {
        JSON, YAML
    }

    public static ObjectMapper createMapper(MapperType type)
    {
        ObjectMapper mapper = MapperType.JSON.equals(type) ? new ObjectMapper() : new ObjectMapper(new YAMLFactory());
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // We encode timestamps with millisecond precision
        mapper.setDateFormat(new SimpleDateFormat(Constants.DEFAULT_JACKSON_TIMESTAMPFORMAT));
        mapper.registerModule(new SpvitaminJsonTimeModul());

        return mapper;
    }
}
