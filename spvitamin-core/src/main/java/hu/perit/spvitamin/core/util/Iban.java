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

import java.math.BigInteger;

public class Iban extends AbstractStringValue<Iban>
{
    public static Iban fromString(String iban)
    {
        return new Iban(iban);
    }


    public static Iban fromGiro(GiroAccountNum giro)
    {
        if (giro == null || giro.value == null)
        {
            return new Iban(null);
        }

        String giro24 = giro.value;

        // Calculate IBAN check digits using ISO 7064 mod-97-10:
        // Step 1: move country code + "00" to the end: BBAN + "HU00"
        // Step 2–3: convert letters to digits and compute mod 97
        int remainder = isoMod97(giro24 + "HU00");

        // Step 4: check digits = 98 - remainder, zero-padded to 2 digits
        int checkDigits = 98 - remainder;
        return new Iban(String.format("HU%02d%s", checkDigits, giro24));
    }


    private Iban(String iban)
    {
        super(validate(iban));
    }


    // returns a valid IBAN string (uppercase, no spaces/dashes) or null
    private static String validate(String iban)
    {
        if (StringUtils.isBlank(iban))
        {
            return null;
        }

        String cleaned = sanitizeIban(iban);
        return isValidFormat(cleaned) ? cleaned : null;
    }


    @Nonnull
    private static String sanitizeIban(@Nonnull String iban)
    {
        return iban.replaceAll("[\\s\\-]", "").toUpperCase();
    }


    public boolean isValid()
    {
        return isValidIban(this.value);
    }


    // Returns the GIRO account number extracted from a Hungarian IBAN, or a null-wrapping instance if not applicable
    public GiroAccountNum toGiro()
    {
        if (StringUtils.isBlank(value) || !value.matches("HU\\d{26}"))
        {
            return GiroAccountNum.fromString(null);
        }
        return GiroAccountNum.fromString(value.substring(4));
    }


    // Checks general IBAN format: 2 letters + 2 digits + alphanumeric BBAN
    public static boolean isValidFormat(String iban)
    {
        if (StringUtils.isBlank(iban))
        {
            return false;
        }
        String cleaned = sanitizeIban(iban);
        return cleaned.matches("[A-Z]{2}\\d{2}[A-Z0-9]+");
    }


    // ISO 7064 mod-97-10 validation
    public static boolean isValidIban(String iban)
    {
        if (!isValidFormat(iban))
        {
            return false;
        }

        // Move the first 4 characters to the end, then verify mod 97 == 1
        String cleaned = sanitizeIban(iban);
        return isoMod97(cleaned.substring(4) + cleaned.substring(0, 4)) == 1;
    }


    // ISO 7064 mod-97-10: converts letters to numbers (A=10..Z=35), then computes mod 97
    static int isoMod97(String alphanumeric)
    {
        StringBuilder numeric = new StringBuilder();
        for (char ch : alphanumeric.toCharArray())
        {
            if (Character.isLetter(ch))
            {
                numeric.append(Character.getNumericValue(ch));
            }
            else
            {
                numeric.append(ch);
            }
        }
        return new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue();
    }


    public String format()
    {
        if (value == null)
        {
            return "";
        }
        // Format as groups of 4 characters separated by spaces: HU42 1177 3016 ...
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i += 4)
        {
            if (!sb.isEmpty())
            {
                sb.append(' ');
            }
            sb.append(value, i, Math.min(i + 4, value.length()));
        }
        return sb.toString();
    }
}
