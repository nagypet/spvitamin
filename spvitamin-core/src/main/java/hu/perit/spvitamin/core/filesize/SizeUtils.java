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
