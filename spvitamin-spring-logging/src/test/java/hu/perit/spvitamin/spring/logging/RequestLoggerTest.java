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

package hu.perit.spvitamin.spring.logging;

import hu.perit.spvitamin.spring.restmethodlogger.Arguments;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RequestLoggerTest
{
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
    }

    @Data
    public static class CreateDocumentRequest
    {
        private String documentTypeName;
        private List<Keyword> keywords;
        private ContentStream content;
        private LocalDate documentDate;
        private String comment;
    }

    @Test
    void testSingleRequest()
    {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setDocumentTypeName("testDocumentType");
        request.setKeywords(getTestKeywords());
        request.setContent(getTestContent());
        request.setDocumentDate(LocalDate.of(2024, 11, 3));
        request.setComment("very very very very very very very very very very very very very very very very very very long comment");

        String subject = RequestLogger.toSubject(request);
        assertThat(subject).isEqualTo("{\"comment\":\"String of size 102 beginning with: very very very very very very very very very very very very very very very very very very long co...\",\"content\":{\"bytes\":\"byte[] of length: 164 bytes\",\"fileName\":\"alma.txt\"},\"documentDate\":\"2024-11-03\",\"documentTypeName\":\"testDocumentType\",\"keywords\":[{\"name\":\"testKeyword1\",\"value\":\"testValue1\"},{\"name\":\"testKeyword2\",\"value\":\"testValue2\"}]}");
    }

    private static List<Keyword> getTestKeywords()
    {
        return List.of(new Keyword("testKeyword1", "testValue1"), new Keyword("testKeyword2", "testValue2"));
    }


    private static ContentStream getTestContent()
    {
        ContentStream contentStream = new ContentStream();
        contentStream.setBytes("PHNvYXBlbnY6RW52ZWxvcGUgeG1sbnM6c29hcGVudj0iaHR0cDovL3NjaGVtYXMueG1sc29hcC5vcmcvc29hcC9lbnZlbG9wZS8iIHhtbG5zOm9uYj0iaHR0cDovL2lubm9kb3guY29tL29uYmFzZXNlcnZpY2UiPg0K".getBytes());
        contentStream.setFileName("alma.txt");
        return contentStream;
    }


    @Test
    void testArguments()
    {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setDocumentTypeName("testDocumentType");
        request.setKeywords(getTestKeywords());
        request.setContent(getTestContent());
        request.setDocumentDate(LocalDate.of(2024, 11, 3));
        request.setComment("very very very very very very very very very very very very very very very very very very long comment");

        String subject = RequestLogger.toSubject(Arguments.create(List.of("request", "username", "password", "processID"), request, "IDXAPI", "Obama", "123"));
        log.debug(subject);
        assertThat(subject).isEqualTo("{\"request\":{\"comment\":\"String of size 102 beginning with: very very very very very very very very very very very very very very very very very very long co...\",\"content\":{\"bytes\":\"byte[] of length: 164 bytes\",\"fileName\":\"alma.txt\"},\"documentDate\":\"2024-11-03\",\"documentTypeName\":\"testDocumentType\",\"keywords\":[{\"name\":\"testKeyword1\",\"value\":\"testValue1\"},{\"name\":\"testKeyword2\",\"value\":\"testValue2\"}]},\"username\":\"IDXAPI\",\"password\":\"*** [hidden]\",\"processID\":\"123\"}");
    }
}
