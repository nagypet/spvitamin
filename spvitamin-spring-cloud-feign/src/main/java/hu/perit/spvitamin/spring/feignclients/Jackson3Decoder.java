package hu.perit.spvitamin.spring.feignclients;

import feign.Response;
import feign.codec.Decoder;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.lang.reflect.Type;

public class Jackson3Decoder implements Decoder
{
    private final JsonMapper mapper;


    public Jackson3Decoder(JsonMapper mapper)
    {
        this.mapper = mapper;
    }


    @Override
    public Object decode(Response response, Type type) throws IOException
    {
        if (response.body() == null)
        {
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        try (var reader = response.body().asReader(response.charset()))
        {
            return mapper.readValue(reader, javaType);
        }
    }
}
