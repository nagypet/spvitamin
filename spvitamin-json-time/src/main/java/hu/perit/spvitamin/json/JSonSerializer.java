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

package hu.perit.spvitamin.json;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Peter Nagy
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JSonSerializer
{
    public static String toJson(Object object) throws JacksonException
    {
        return SpvitaminObjectMapper.getJsonMapper().writeValueAsString(object);
    }


    public static String toYaml(Object object) throws JacksonException
    {
        return SpvitaminObjectMapper.getYamlMapper().writeValueAsString(object);
    }


    public static <T> T fromJson(String jsonString, Class<T> target) throws JacksonException
    {
        String fixedJson = fixRoorLevelObjects(jsonString, target);
        JsonMapper mapper = SpvitaminObjectMapper.getJsonMapper();
        return mapper.readValue(fixedJson, mapper.getTypeFactory().constructType(target));
    }


    // Root level objects must be put into quotes in order to trigger custom deserializers
    private static <T> String fixRoorLevelObjects(final String json, Class<T> clazz)
    {
        String retval = json;
        if (clazz == LocalDate.class
                || clazz == LocalDateTime.class
                || clazz == OffsetDateTime.class
                || clazz == ZonedDateTime.class
                || clazz == Instant.class
                || clazz == Date.class
        )
        {
            if (!retval.startsWith("\""))
            {
                retval = "\"" + retval;
            }
            if (!retval.endsWith("\""))
            {
                retval = retval + "\"";
            }
        }
        return retval;
    }


    public static <T> T fromYaml(String jsonString, Class<T> target) throws JacksonException
    {
        YAMLMapper mapper = SpvitaminObjectMapper.getYamlMapper();
        return mapper.readValue(jsonString, mapper.getTypeFactory().constructType(target));
    }
}
