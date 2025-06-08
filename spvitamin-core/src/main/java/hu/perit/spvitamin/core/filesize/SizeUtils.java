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

package hu.perit.spvitamin.core.filesize;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A utility class for converting between byte sizes and human-readable size formats.
 * 
 * <p>This class provides methods to convert file sizes between raw byte counts and
 * human-readable string representations (e.g., "1.5 GB", "500 kB"). It supports
 * standard binary size units (B, kB, MB, GB, TB) with 1024-based conversions.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Convert byte counts to human-readable size strings</li>
 *   <li>Parse human-readable size strings back to byte counts</li>
 *   <li>Support for all common size units (B, kB, MB, GB, TB)</li>
 *   <li>Proper handling of decimal values in size strings</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Convert bytes to human-readable format
 * String readable = SizeUtils.convertToHumanReadable(1536000L); // "1.5 MB"
 * 
 * // Convert human-readable format to bytes
 * long bytes = SizeUtils.convertToBytes("2.5 GB"); // 2684354560
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SizeUtils
{
    private static final Map<String, Long> TO_BYTES = new LinkedHashMap<>();

    static
    {
        TO_BYTES.put("TB", 1024L * 1024 * 1024 * 1024);
        TO_BYTES.put("GB", 1024L * 1024 * 1024);
        TO_BYTES.put("MB", 1024L * 1024);
        TO_BYTES.put("kB", 1024L);
        TO_BYTES.put("B", 1L);
    }


    public static String convertToHumanReadable(Long bytes)
    {
        if (bytes == null)
        {
            return null;
        }
        return FileUtils.byteCountToDisplaySize(bytes);
    }


    public static long convertToBytes(String size)
    {
        if (size == null)
        {
            return 0;
        }

        size = size.strip();

        for (Map.Entry<String, Long> entry : TO_BYTES.entrySet())
        {
            if (size.endsWith(entry.getKey()))
            {
                String numberPart = size.substring(0, size.length() - entry.getKey().length()).strip();
                return (long) (Double.parseDouble(numberPart) * entry.getValue());
            }
        }
        throw new IllegalArgumentException("Unknown size format: " + size);
    }
}
