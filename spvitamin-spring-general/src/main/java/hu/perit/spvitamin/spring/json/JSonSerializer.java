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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import hu.perit.spvitamin.spring.config.Constants;

import java.text.SimpleDateFormat;

/**
 * @author Peter Nagy
 */


public final class JSonSerializer {

    public String toJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // We encode timestamps with millisecond presision
        mapper.setDateFormat(new SimpleDateFormat(Constants.DEFAULT_JACKSON_TIMESTAMPFORMAT));
        SimpleModule module = new SimpleModule();
        module.addSerializer(new CustomLocalDateSerializer());
        module.addSerializer(new CustomLocalDateTimeSerializer());
        mapper.registerModule(module);

        return mapper.writeValueAsString(object);
    }
}
