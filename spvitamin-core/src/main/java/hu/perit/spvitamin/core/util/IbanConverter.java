package hu.perit.spvitamin.core.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.math.BigInteger;

@UtilityClass
public class IbanConverter
{
    // Hungarian IBAN: 28 characters long (HU + 2 check digits + 24-digit BBAN)
    // 16-digit account number: append 00000000 (8 zeros) to get a 24-digit BBAN
    // 24-digit account number: already the full BBAN
    // Check digit calculation: BBAN + HU00 → convert each letter to a number (A=10..Z=35) → mod 97 → 98 - result
    // Does not validate whether IBAN or GIRO account numbers are valid. Use isValidAccountNum instead.
    public String toIban(String accountNumber)
    {
        if (StringUtils.isBlank(accountNumber))
        {
            return null;
        }

        // Strip spaces and dashes (formatting characters)
        String cleaned = accountNumber.replaceAll("[\\s\\-]", "");

        // Already an IBAN: starts with two letters followed by digits
        if (cleaned.matches("[A-Za-z]{2}\\d+"))
        {
            return cleaned.toUpperCase();
        }

        // GIRO format: 16 or 24 digits
        if (GiroAccountNum.isValidFormat(cleaned))
        {
            cleaned = GiroAccountNum.fromString(cleaned).toString();
        }
        else
        {
            throw new IllegalArgumentException("Unsupported account number format: " + accountNumber);
        }

        // Calculate IBAN check digits using ISO 7064 mod-97-10:
        // Step 1: move country code + "00" to the end: BBAN + "HU00"
        // Step 2–3: convert letters to digits and compute mod 97
        int remainder = isoMod97(cleaned + "HU00");

        // Step 4: check digits = 98 - remainder, zero-padded to 2 digits
        int checkDigits = 98 - remainder;
        return String.format("HU%02d%s", checkDigits, cleaned);
    }


    // Returns the 24-digit BBAN extracted from a Hungarian IBAN, or null if invalid
    public GiroAccountNum toGiro(String iban)
    {
        if (StringUtils.isBlank(iban))
        {
            return GiroAccountNum.fromString(null);
        }

        String cleaned = iban.replaceAll("[\\s\\-]", "").toUpperCase();

        // Hungarian IBAN: HU + 2 check digits + 24-digit BBAN = 28 characters
        if (!cleaned.matches("HU\\d{26}"))
        {
            return GiroAccountNum.fromString(null);
        }

        return GiroAccountNum.fromString(cleaned.substring(4));
    }


    public boolean isValidAccountNum(String accountNum)
    {
        try
        {
            String iban = toIban(accountNum);
            if (!isValidIban(iban))
            {
                return false;
            }
            return !Strings.CI.startsWith(iban, "HU")
                    || IbanConverter.toGiro(iban).isValid();
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }


    public boolean isValidIban(String iban)
    {
        if (StringUtils.isBlank(iban))
        {
            return false;
        }

        String cleaned = iban.replaceAll("[\\s\\-]", "").toUpperCase();

        // General IBAN: 2 letters + 2 digits + BBAN (at least 1 character)
        if (!cleaned.matches("[A-Z]{2}\\d{2}[A-Z0-9]+"))
        {
            return false;
        }

        // ISO 7064 mod-97-10 check: move the first 4 characters to the end
        return isoMod97(cleaned.substring(4) + cleaned.substring(0, 4)) == 1;
    }


    // ISO 7064 mod-97-10: converts letters to numbers (A=10..Z=35), then computes mod 97
    private static int isoMod97(String alphanumeric)
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
}
