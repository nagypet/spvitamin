/*
 * Copyright (c) 2022. Innodox Technologies Zrt.
 * All rights reserved.
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
