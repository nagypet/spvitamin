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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A utility class for file name and path operations.
 * 
 * <p>This class provides methods for working with file names and paths, including
 * sanitization, extraction of components, and path construction. It builds upon
 * Apache Commons IO's FilenameUtils with additional functionality.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Sanitize file names by removing invalid characters</li>
 *   <li>Extract file names, folders, base names, and extensions from paths</li>
 *   <li>Construct paths from multiple parts with proper separator handling</li>
 *   <li>Normalize file extensions to lowercase</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Sanitize a file name
 * String safe = FileNameUtils.sanitizeFileName("file:with?invalid*chars.txt"); // "filewithinvalidchars.txt"
 * 
 * // Extract components from a path
 * String name = FileNameUtils.getFileName("/path/to/file.txt"); // "file.txt"
 * String folder = FileNameUtils.getFolder("/path/to/file.txt"); // "path/to/"
 * String base = FileNameUtils.getBaseName("/path/to/file.txt"); // "file"
 * String ext = FileNameUtils.getFileExtension("/path/to/file.txt"); // "txt"
 * 
 * // Construct a path
 * String path = FileNameUtils.getPath("path", "to", "file.txt"); // "path/to/file.txt"
 * </pre>
 */
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


    public static String getFileName(String path)
    {
        return FilenameUtils.getName(path);
    }


    public static String getFolder(String path)
    {
        return FilenameUtils.getPath(path);
    }


    public static String getBaseName(String fileName)
    {
        return FilenameUtils.getBaseName(fileName);
    }


    public static String getFileExtension(String fileName)
    {
        return StringUtils.toRootLowerCase(FilenameUtils.getExtension(sanitizeFileName(fileName)));
    }


    public static String getPath(String... parts)
    {
        return Arrays.stream(parts)
                .filter(Objects::nonNull)
                .map(part -> part.replaceAll("^/+", "").replaceAll("/+$", ""))
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining("/"));
    }
}
