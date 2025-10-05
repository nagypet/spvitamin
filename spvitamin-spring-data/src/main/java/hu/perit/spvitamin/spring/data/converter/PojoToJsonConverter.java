package hu.perit.spvitamin.spring.data.converter;

import hu.perit.spvitamin.spring.json.JSonSerializer;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

@Converter
@RequiredArgsConstructor
public class PojoToJsonConverter<T> implements AttributeConverter<T, String>
{
    private final Class<T> type;


    @Override
    public String convertToDatabaseColumn(T object)
    {
        if (object == null)
        {
            return null;
        }
        try
        {
            return JSonSerializer.toJson(object);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(MessageFormat.format("Object cannot be converted to JSON! {0}", object));
        }
    }


    @Override
    public T convertToEntityAttribute(String json)
    {
        if (StringUtils.isBlank(json))
        {
            return null;
        }

        try
        {
            return JSonSerializer.fromJson(json, this.type);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(MessageFormat.format("JSON cannot be converted to {0} ({1})", this.type.getSimpleName(), json));
        }
    }
}
