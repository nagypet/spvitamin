package hu.perit.spvitamin.spring.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.perit.spvitamin.json.SpvitaminObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpvitaminSpringObjectMapper
{
    public static ObjectMapper createMapper(SpvitaminObjectMapper.MapperType type)
    {
        ObjectMapper mapper = SpvitaminObjectMapper.createMapper(type);
        // Register additional modules for use within the Spring framework
        mapper.registerModule(new SpvitaminJsonSpringModule());

        return mapper;
    }
}
