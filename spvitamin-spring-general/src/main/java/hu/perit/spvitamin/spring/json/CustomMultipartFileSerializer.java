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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class CustomMultipartFileSerializer extends JsonSerializer<MultipartFile>
{
    @Data
    private static class SerializedMultipartFile
    {
        private final String contentType;
        private final String originalFileName;
        private final long size;
    }

    @Override
    public void serialize(MultipartFile value, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        //String stringValue = String.format("{contentType: %s, originalFileName: %s}", value.getContentType(), value.getOriginalFilename());
        if (value == null)
        {
            gen.writeNull();
            return;
        }

        gen.writeObject(new SerializedMultipartFile(value.getContentType(), value.getOriginalFilename(), value.getSize()));

//        if (!stringValue.isEmpty() && !stringValue.equals("null"))
//        {
//            gen.writeString(stringValue);
//        }
//        else
//        {
//            gen.writeNull();
//        }

    }

    @Override
    public Class<MultipartFile> handledType()
    {
        return MultipartFile.class;
    }

}
