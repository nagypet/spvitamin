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

package hu.perit.spvitamin.core.filename;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class FileNameUtilsTest
{
    @Test
    void testSanitizeFileName()
    {
        String fixedFileName = FileNameUtils.sanitizeFileName("Mieten MV\r\n\n\n: 213104");
        log.debug(fixedFileName);
        assertThat(fixedFileName).isEqualTo("Mieten MV 213104");
    }


    @Test
    void testGetFileExtension()
    {
        assertThat(FileNameUtils.getFileExtension("Mieten MV: 213104.pdf")).isEqualTo("pdf");
        assertThat(FileNameUtils.getFileExtension("Mieten MV: 213104.PDF")).isEqualTo("pdf");
        assertThat(FileNameUtils.getFileExtension("alma")).isEmpty();
        assertThat(FileNameUtils.getFileExtension("alma.korte.szilva")).isEqualTo("szilva");
    }
}
