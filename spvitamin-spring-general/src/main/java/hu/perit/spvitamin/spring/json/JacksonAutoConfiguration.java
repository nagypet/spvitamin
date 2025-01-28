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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Extends JacksonAutoConfiguration with custom serializers and deserializers
 */

@Configuration
public class JacksonAutoConfiguration
{
    @Bean
    public ObjectMapper objectMapper()
    {
        ObjectMapper mapper = SpvitaminSpringObjectMapper.createMapper(SpvitaminObjectMapper.MapperType.JSON);
        TypeRegistry.registerTypesInMapper(mapper);
        return mapper;
    }
}
