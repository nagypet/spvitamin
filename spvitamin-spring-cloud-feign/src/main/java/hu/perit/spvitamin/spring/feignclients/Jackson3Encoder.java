package hu.perit.spvitamin.spring.feignclients;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;

public class Jackson3Encoder implements Encoder
{
    private final JsonMapper mapper;


    public Jackson3Encoder(JsonMapper mapper)
    {
        this.mapper = mapper;
    }


    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException
    {
        try
        {
            template.body(mapper.writeValueAsBytes(object), null);
        }
        catch (Exception e)
        {
            throw new EncodeException(e.getMessage(), e);
        }
    }
}
