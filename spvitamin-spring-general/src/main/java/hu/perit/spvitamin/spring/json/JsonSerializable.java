/*
 * Copyright 2020-2020 the original author or authors.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Peter Nagy
 */

public interface JsonSerializable {

    default String toJson() throws JsonProcessingException {
        return new JSonSerializer().toJson(this);
    }

    static <T> T fromJson(String jsonString, Class<?> target) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new CustomDateDeserializer());
        module.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        module.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        mapper.registerModule(module);

        T obj = mapper.readValue(jsonString, mapper.getTypeFactory().constructType(target));
        if (obj instanceof JsonSerializable) {
            ((JsonSerializable) obj).finalizeJsonDeserialization();
        }
        return obj;
    }

    default void finalizeJsonDeserialization() {
        // Override if you want to some post processing. E.g. if you have a member containing a data type information
        // you may want to case the data member to have the real data type.
    }
}


/*
// Requirements:
// - a default constructor
// - Getter methods or @JsonProperty annotation
class Example implements JsonSerializable
{
    @JsonProperty
    private final String name;
    @JsonProperty
    private final int age;

    public Example()
    {
        this.name = null;
        age = 0;
    }
}
*/