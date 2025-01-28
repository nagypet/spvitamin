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

package hu.perit.spvitamin.spring.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.perit.spvitamin.json.SpvitaminObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpvitaminSpringObjectMapper
{
    private static final ObjectMapper jsonMapper = internalCreateMapper(SpvitaminObjectMapper.MapperType.JSON);
    private static final ObjectMapper yamlMapper = internalCreateMapper(SpvitaminObjectMapper.MapperType.YAML);

    public static ObjectMapper createMapper(SpvitaminObjectMapper.MapperType type)
    {
        return switch (type)
        {
            case JSON -> jsonMapper;
            case YAML -> yamlMapper;
        };
    }

    private static ObjectMapper internalCreateMapper(SpvitaminObjectMapper.MapperType type)
    {
        ObjectMapper mapper = SpvitaminObjectMapper.createMapper(type);
        // Register additional modules for use within the Spring framework
        mapper.registerModule(new SpvitaminJsonSpringModule());

        return mapper;
    }
}
