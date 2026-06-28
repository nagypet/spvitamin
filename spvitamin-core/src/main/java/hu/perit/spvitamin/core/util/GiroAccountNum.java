/*
 * Copyright 2020-2026 the original author or authors.
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

import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;

public class GiroAccountNum extends AbstractStringValue<GiroAccountNum>
{
    public static GiroAccountNum fromString(String value)
    {
        return new GiroAccountNum(value);
    }


    private GiroAccountNum(String accountNumber)
    {
        super(validate(accountNumber));
    }


    // returns a valid GIRO string (no spaces/dashes) or null
    private static String validate(String accountNumber)
    {
        if (StringUtils.isBlank(accountNumber))
        {
            return null;
        }

        // Strip spaces and dashes (formatting characters)
        String cleaned = sanitize(accountNumber);

        // GIRO format: 16 or 24 digits
        if (cleaned.matches("\\d{16}"))
        {
            // Pad to 24 digits by appending 8 zeros
            cleaned = cleaned + "00000000";
        }
        return isValidFormat(cleaned) ? cleaned : null;
    }


    @Nonnull
    private static String sanitize(@Nonnull String accountNumber)
    {
        return accountNumber.replaceAll("[\\s\\-]", "");
    }


    public boolean isValid()
    {
        return isValid(this.value);
    }


    public String getBankCode()
    {
        if (StringUtils.isBlank(this.value))
        {
            return null;
        }
        return this.value.substring(0, 8);
    }


    // the account number without the bank code
    public String getAccountNumber()
    {
        if (StringUtils.isBlank(this.value))
        {
            return null;
        }
        return this.value.substring(8);
    }


    public static boolean isValidFormat(String giro)
    {
        if (StringUtils.isBlank(giro))
        {
            return false;
        }

        String cleaned = sanitize(giro);
        return cleaned.matches("\\d{16}") || cleaned.matches("\\d{24}");
    }


    public static boolean isValid(String giro)
    {
        if (!isValidFormat(giro))
        {
            return false;
        }
        String cleaned = sanitize(giro);

        // Weights for 7-digit and 15-digit CDV calculations
        int[] weights7 = {9, 7, 3, 1, 9, 7, 3};
        int[] weights15 = {9, 7, 3, 1, 9, 7, 3, 1, 9, 7, 3, 1, 9, 7, 3};

        // CDV at position 8, calculated from positions 1-7
        // {10 - {(p1*9 + p2*7 + p3*3 + p4*1 + p5*9 + p6*7 + p7*3) mod 10}} mod 10
        if (calculateCdv(cleaned, 0, weights7) != (cleaned.charAt(7) - '0'))
        {
            return false;
        }

        if (cleaned.length() == 16)
        {
            // CDV at position 16, calculated from positions 9-15
            // {10 - {(p9*9 + p10*7 + p11*3 + p12*1 + p13*9 + p14*7 + p15*3) mod 10}} mod 10
            return calculateCdv(cleaned, 8, weights7) == (cleaned.charAt(15) - '0');
        }
        else
        {
            // CDV at position 24, calculated from positions 9-23
            // {10 - {(p9*9 + ... + p23*3) mod 10}} mod 10
            return calculateCdv(cleaned, 8, weights15) == (cleaned.charAt(23) - '0');
        }
    }


    private static int calculateCdv(String s, int startIndex, int[] weights)
    {
        int sum = 0;
        for (int i = 0; i < weights.length; i++)
        {
            sum += (s.charAt(startIndex + i) - '0') * weights[i];
        }
        return (10 - (sum % 10)) % 10;
    }


    public String format()
    {
        if (this.value == null)
        {
            return "";
        }
        return this.value.substring(0, 8) + "-" + this.value.substring(8, 16) + "-" + this.value.substring(16);
    }
}
