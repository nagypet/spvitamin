/*
 * Copyright 2020-2023 the original author or authors.
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

package hu.perit.spvitamin.spring.json;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.perit.spvitamin.spring.config.Constants;

/**
 * @author Peter Nagy
 */

public final class JSonSerializer
{
    private static ObjectMapper jsonMapper;
    private static ObjectMapper yamlMapper;

	public String toJson(Object object) throws JsonProcessingException
	{
		return getJsonMapper().writeValueAsString(object);
	}


	public String toYaml(Object object) throws JsonProcessingException
	{
		return getYamlMapper().writeValueAsString(object);
	}


	public static <T> T fromJson(String jsonString, Class<T> target) throws IOException
	{
		return getJsonMapper().readValue(jsonString, getJsonMapper().getTypeFactory().constructType(target));
	}


	public static <T> T fromYaml(String jsonString, Class<T> target) throws IOException
	{
		return getYamlMapper().readValue(jsonString, getYamlMapper().getTypeFactory().constructType(target));
	}


    private static synchronized ObjectMapper getJsonMapper()
    {
        if (jsonMapper == null)
        {
            jsonMapper = createMapper(MapperType.JSON);
        }

        return jsonMapper;
    }


    private static synchronized ObjectMapper getYamlMapper()
    {
        if (yamlMapper == null)
        {
            yamlMapper = createMapper(MapperType.YAML);
        }

        return yamlMapper;
    }

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
		SimpleModule module = new SimpleModule();
		module.addSerializer(new CustomLocalDateSerializer());
		module.addSerializer(new CustomLocalDateTimeSerializer());
		module.addSerializer(new CustomMultipartFileSerializer());
		module.addDeserializer(Date.class, new CustomDateDeserializer());
		module.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
		module.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
		mapper.registerModule(module);

		return mapper;
	}
}
