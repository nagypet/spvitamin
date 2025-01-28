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

package hu.perit.spvitamin.core.thing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ThingTest
{
    public enum Types
    {
        ALMA,
        KORTE
    }

    @Data
    public static class Keyword
    {
        private final String name;
        private final String value;
    }

    @Data
    public static class ContentStream
    {
        private byte[] bytes = null;
        private String fileName = "";
        private InputStream stream = new ByteArrayInputStream("alma".getBytes());
    }

    @Data
    public static class CreateDocumentRequest
    {
        private String documentTypeName;
        private List<Keyword> keywords;
        private ContentStream content;
        private LocalDate documentDate;
        private String password;
        private List<String> passwords;
        private String comment;
        private Set<Keyword> keywordsSet;
        private Map<String, Keyword> keywordsMap;
        private Types type = Types.ALMA;
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private String privateWithoutGetter = "privateWithoutGetter";
    }


    @Test
    void testSimpleType()
    {
        Thing thing = Thing.from("alma");
        assertThat(thing).isInstanceOf(Value.class);

        String dump = dump(thing);
        assertThat(dump).isEqualTo("\"alma\"");
    }


    @Test
    void testComplexObject()
    {
        CreateDocumentRequest request = getCreateDocumentRequest();

        Thing thing = Thing.from(request);
        assertThat(thing).isInstanceOf(ValueMap.class);
        assertThat(((ValueMap) thing).getProperties()).hasSize(10);

        String dump = dump(thing);
        assertThat(dump).isEqualTo("""
                {
                  "comment":"String of size 72 beginning with: very very very ve...",
                  "content":{
                    "bytes":"byte[] of length: 164 bytes",
                    "fileName":"alma.txt",
                    "stream":"java.io.ByteArrayInputStream of size 4"
                  },
                  "documentDate":"2024-11-03",
                  "documentTypeName":"testDocumentType",
                  "keywords":[
                    {
                      "name":"test-keyword",
                      "value":"keyword value"
                    },
                    {
                      "name":"password-keyword",
                      "value":"testPassword"
                    }
                  ],
                  "keywordsMap":{
                    "test-keyword":{
                      "name":"test-keyword",
                      "value":"keyword value"
                    },
                    "password-keyword":{
                      "name":"password-keyword",
                      "value":"testPassword"
                    }
                  },
                  "keywordsSet":[
                    {
                      "name":"password-keyword",
                      "value":"testPassword"
                    },
                    {
                      "name":"test-keyword",
                      "value":"keyword value"
                    }
                  ],
                  "password":"*** [hidden]",
                  "passwords":[
                    "*** [hidden]",
                    "*** [hidden]"
                  ],
                  "type":"ALMA"
                }""");
    }


    private static CreateDocumentRequest getCreateDocumentRequest()
    {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setDocumentTypeName("testDocumentType");
        request.setKeywords(getTestKeywords());
        request.setContent(getTestContent());
        request.setDocumentDate(LocalDate.of(2024, 11, 3));
        request.setPassword("my secret password");
        request.setPasswords(List.of("alma", "körte"));
        request.setComment("very very very very very very very very very very very very long comment");
        request.setKeywordsSet(new HashSet<>(getTestKeywords()));
        request.setKeywordsMap(getTestKeywords().stream().collect(Collectors.toMap(k -> k.getName(), v -> v)));
        return request;
    }


    @Test
    void testPropertyMap()
    {
        CreateDocumentRequest request = getCreateDocumentRequest();

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("request", request);
        properties.put("username", "IDXAPI");
        properties.put("password", "my password");
        properties.put("traceID", "123");

        Thing thing = Thing.from(properties);
        assertThat(thing).isInstanceOf(ValueMap.class);
        assertThat(((ValueMap) thing).getProperties()).hasSize(4);

        String dump = dump(thing);
        assertThat(dump).isEqualTo("""
                {
                  "request":{
                    "comment":"String of size 72 beginning with: very very very ve...",
                    "content":{
                      "bytes":"byte[] of length: 164 bytes",
                      "fileName":"alma.txt",
                      "stream":"java.io.ByteArrayInputStream of size 4"
                    },
                    "documentDate":"2024-11-03",
                    "documentTypeName":"testDocumentType",
                    "keywords":[
                      {
                        "name":"test-keyword",
                        "value":"keyword value"
                      },
                      {
                        "name":"password-keyword",
                        "value":"testPassword"
                      }
                    ],
                    "keywordsMap":{
                      "test-keyword":{
                        "name":"test-keyword",
                        "value":"keyword value"
                      },
                      "password-keyword":{
                        "name":"password-keyword",
                        "value":"testPassword"
                      }
                    },
                    "keywordsSet":[
                      {
                        "name":"password-keyword",
                        "value":"testPassword"
                      },
                      {
                        "name":"test-keyword",
                        "value":"keyword value"
                      }
                    ],
                    "password":"*** [hidden]",
                    "passwords":[
                      "*** [hidden]",
                      "*** [hidden]"
                    ],
                    "type":"ALMA"
                  },
                  "username":"IDXAPI",
                  "password":"*** [hidden]",
                  "traceID":"123"
                }""");
    }


    @Test
    void testVisitor() throws JsonProcessingException
    {
        CreateDocumentRequest request = getCreateDocumentRequest();

        Thing thing = Thing.from(request, true);
        String dump = dump(thing);
        assertThat(dump).isEqualTo("""
                {
                  "privateWithoutGetter":"privateWithoutGetter",
                  "comment":"String of size 72 beginning with: very very very ve...",
                  "content":{
                    "bytes":"byte[] of length: 164 bytes",
                    "fileName":"alma.txt",
                    "stream":"java.io.ByteArrayInputStream of size 4"
                  },
                  "documentDate":"2024-11-03",
                  "documentTypeName":"testDocumentType",
                  "keywords":[
                    {
                      "name":"test-keyword",
                      "value":"keyword value"
                    },
                    {
                      "name":"password-keyword",
                      "value":"testPassword"
                    }
                  ],
                  "keywordsMap":{
                    "test-keyword":{
                      "name":"test-keyword",
                      "value":"keyword value"
                    },
                    "password-keyword":{
                      "name":"password-keyword",
                      "value":"testPassword"
                    }
                  },
                  "keywordsSet":[
                    {
                      "name":"password-keyword",
                      "value":"testPassword"
                    },
                    {
                      "name":"test-keyword",
                      "value":"keyword value"
                    }
                  ],
                  "password":"*** [hidden]",
                  "passwords":[
                    "*** [hidden]",
                    "*** [hidden]"
                  ],
                  "type":"ALMA"
                }""");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        log.debug(objectMapper.writeValueAsString(thing));
    }

    private static String dump(Thing thing)
    {
        PrinterVisitor printerVisitor = new PrinterVisitor(PrinterVisitor.Options.builder().prettyPrint(true).hidePasswords(true).maxStringLength(20).build());
        thing.accept(printerVisitor);
        String json = printerVisitor.getJson();
        log.debug(json);
        return json;
    }


    private static List<Keyword> getTestKeywords()
    {
        return List.of(new Keyword("test-keyword", "keyword value"), new Keyword("password-keyword", "testPassword"));
    }


    private static ContentStream getTestContent()
    {
        ContentStream contentStream = new ContentStream();
        contentStream.setBytes("PHNvYXBlbnY6RW52ZWxvcGUgeG1sbnM6c29hcGVudj0iaHR0cDovL3NjaGVtYXMueG1sc29hcC5vcmcvc29hcC9lbnZlbG9wZS8iIHhtbG5zOm9uYj0iaHR0cDovL2lubm9kb3guY29tL29uYmFzZXNlcnZpY2UiPg0K".getBytes());
        contentStream.setFileName("alma.txt");
        return contentStream;
    }
}
