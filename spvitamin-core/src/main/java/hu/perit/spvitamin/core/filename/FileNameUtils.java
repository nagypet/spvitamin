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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileNameUtils
{
    public static String sanitizeFileName(String fileName)
    {
        if (fileName == null)
        {
            return null;
        }

        return fileName.replaceAll("[\\\\/:*?\"<>|\r\n]", "");
    }


    public static String getBaseName(String fileName)
    {
        return FilenameUtils.getBaseName(fileName);
    }


    public static String getFileExtension(String fileName)
    {
        return StringUtils.toRootLowerCase(FilenameUtils.getExtension(sanitizeFileName(fileName)));
    }
}
