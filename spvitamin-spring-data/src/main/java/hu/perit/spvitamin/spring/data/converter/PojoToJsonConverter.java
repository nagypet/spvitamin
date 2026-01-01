/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.data.converter;

import hu.perit.spvitamin.json.JSonSerializer;
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
