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


    @Test
    void testNullValue()
    {
        // Test creating a Thing from a null value
        Thing thing = Thing.from(null);
        assertThat(thing).isInstanceOf(Value.class);
        assertThat(((Value) thing).getValue()).isNull();

        // Test isEmpty() method
        assertThat(thing.isEmpty()).isTrue();

        // Test JSON representation
        String dump = dump(thing);
        assertThat(dump).isEqualTo("null");
    }


    @Test
    void testPrimitiveTypes()
    {
        // Test with integer
        Thing intThing = Thing.from(42);
        assertThat(intThing).isInstanceOf(Value.class);
        assertThat(((Value) intThing).getValue()).isEqualTo(42);
        assertThat(dump(intThing)).isEqualTo("\"42\"");

        // Test with boolean
        Thing boolThing = Thing.from(true);
        assertThat(boolThing).isInstanceOf(Value.class);
        assertThat(((Value) boolThing).getValue()).isEqualTo(true);
        assertThat(dump(boolThing)).isEqualTo("\"true\"");

        // Test with double
        Thing doubleThing = Thing.from(3.14);
        assertThat(doubleThing).isInstanceOf(Value.class);
        assertThat(((Value) doubleThing).getValue()).isEqualTo(3.14);
        assertThat(dump(doubleThing)).isEqualTo("\"3.14\"");
    }


    @Test
    void testEmptyCollections()
    {
        // Test with empty list
        List<String> emptyList = List.of();
        Thing emptyListThing = Thing.from(emptyList);
        assertThat(emptyListThing).isInstanceOf(ValueList.class);
        assertThat(((ValueList) emptyListThing).getElements()).isEmpty();
        assertThat(emptyListThing.isEmpty()).isTrue();
        assertThat(dump(emptyListThing)).isEqualTo("[]");

        // Test with empty map
        Map<String, String> emptyMap = Map.of();
        Thing emptyMapThing = Thing.from(emptyMap);
        assertThat(emptyMapThing).isInstanceOf(ValueMap.class);
        assertThat(((ValueMap) emptyMapThing).getProperties()).isEmpty();
        assertThat(emptyMapThing.isEmpty()).isTrue();
        assertThat(dump(emptyMapThing)).isEqualTo("{}");
    }


    @Test
    void testArray()
    {
        // Test with string array
        String[] stringArray = {"apple", "banana", "cherry"};
        Thing arrayThing = Thing.from(stringArray);
        assertThat(arrayThing).isInstanceOf(ValueList.class);
        assertThat(((ValueList) arrayThing).getElements()).hasSize(3);
        assertThat(dump(arrayThing)).isEqualTo("""
                [
                  "apple",
                  "banana",
                  "cherry"
                ]""");

        // Test with int array
        int[] intArray = {1, 2, 3};
        Thing intArrayThing = Thing.from(intArray);
        assertThat(intArrayThing).isInstanceOf(ValueList.class);
        assertThat(((ValueList) intArrayThing).getElements()).hasSize(3);
        assertThat(dump(intArrayThing)).isEqualTo("""
                [
                  "1",
                  "2",
                  "3"
                ]""");
    }


    @Test
    void testNestedStructures()
    {
        // Create a complex nested structure
        Map<String, Object> nestedMap = new LinkedHashMap<>();
        nestedMap.put("name", "John");
        nestedMap.put("age", 30);
        nestedMap.put("hobbies", List.of("reading", "swimming", "coding"));

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("street", "123 Main St");
        address.put("city", "Anytown");
        address.put("zipCode", "12345");

        nestedMap.put("address", address);

        // Convert to Thing
        Thing thing = Thing.from(nestedMap);
        assertThat(thing).isInstanceOf(ValueMap.class);
        assertThat(((ValueMap) thing).getProperties()).hasSize(4);

        // Check JSON representation
        String dump = dump(thing);
        assertThat(dump).isEqualTo("""
                {
                  "name":"John",
                  "age":"30",
                  "hobbies":[
                    "reading",
                    "swimming",
                    "coding"
                  ],
                  "address":{
                    "street":"123 Main St",
                    "city":"Anytown",
                    "zipCode":"12345"
                  }
                }""");
    }


    @Test
    void testPrinterVisitorOptions()
    {
        // Create a test object
        CreateDocumentRequest request = getCreateDocumentRequest();
        Thing thing = Thing.from(request);

        // Test with hidePasswords=false
        PrinterVisitor printerVisitor = new PrinterVisitor(PrinterVisitor.Options.builder()
                .prettyPrint(true)
                .hidePasswords(false)
                .build());
        thing.accept(printerVisitor);
        String json = printerVisitor.getJson();

        // Passwords should be visible
        assertThat(json).contains("\"password\":\"my secret password\"");
        assertThat(json).contains("\"alma\"");
        assertThat(json).contains("\"körte\"");
    }


    @Test
    void testSpecialCharacters()
    {
        // Test with string containing special characters
        String specialChars = "Special chars: \n \r\n \t \" \\ ";
        Thing thing = Thing.from(specialChars);

        // Check that special characters are properly escaped in JSON
        String dump = dump(thing);
        assertThat(dump).isEqualTo("\"Special chars: \\n \\r\\n \\t \\\" \\\\ \"");
    }


    @Test
    void testLongString()
    {
        // Create a very long string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("Long string test. ");
        }
        String longString = sb.toString();

        // Convert to Thing
        Thing thing = Thing.from(longString);

        // Check that the string is abbreviated in the JSON
        String dump = dump(thing);
        assertThat(dump).startsWith("\"String of size");
        assertThat(dump).contains("beginning with:");
    }


    @Test
    void testEnum()
    {
        // Test with enum value
        Thing thing = Thing.from(Types.KORTE);

        // Check type and value
        assertThat(thing).isInstanceOf(Value.class);
        assertThat(((Value) thing).getValue()).isEqualTo(Types.KORTE);

        // Check JSON representation
        String dump = dump(thing);
        assertThat(dump).isEqualTo("\"KORTE\"");
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
