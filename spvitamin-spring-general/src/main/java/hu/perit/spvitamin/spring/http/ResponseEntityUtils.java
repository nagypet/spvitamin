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

package hu.perit.spvitamin.spring.http;

import hu.perit.spvitamin.core.filename.FileNameUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseEntityUtils
{
    public static ResponseEntity<byte[]> createFileDownloadResponse(byte[] fileContent, String fileName)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(FileNameUtils.sanitizeFileName(fileName))
                        .build()
        );
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }


    public static <T> T get(ResponseEntity<T> responseEntity)
    {
        if (responseEntity != null && responseEntity.hasBody())
        {
            return responseEntity.getBody();
        }

        throw new IllegalStateException("Invalid http response!");
    }
}
