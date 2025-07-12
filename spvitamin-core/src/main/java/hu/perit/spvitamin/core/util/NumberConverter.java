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

package hu.perit.spvitamin.core.util;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class NumberConverter
{
    public static BigDecimal fromText(String text)
    {
        if (text == null)
        {
            return null;
        }

        // Handle different number formats
        text = text.replace(" ", "");

        String normalizedText = getNormalizedText(text);

        return new BigDecimal(normalizedText);
    }


    private static String getNormalizedText(String text)
    {
        // Check if the text contains both period and comma
        if (text.contains(".") && text.contains(","))
        {
            // Determine which is the decimal separator based on position
            int lastPeriodPos = text.lastIndexOf(".");
            int lastCommaPos = text.lastIndexOf(",");

            if (lastPeriodPos > lastCommaPos)
            {
                // Format like "1,234.56" - period is decimal separator
                return text.replace(",", "");
            }
            else
            {
                // Format like "1.234,56" - comma is decimal separator
                return text.replace(".", "").replace(",", ".");
            }
        }
        else if (text.contains(","))
        {
            // Only comma present, treat as decimal separator
            return text.replace(",", ".");
        }

        return text;
    }
}
