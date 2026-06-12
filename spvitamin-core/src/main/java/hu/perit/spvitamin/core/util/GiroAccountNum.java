package hu.perit.spvitamin.core.util;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class GiroAccountNum
{
    // null or valid 24-digit GIRO number
    private final String giro24;


    public static GiroAccountNum fromString(String value)
    {
        return new GiroAccountNum(value);
    }


    private GiroAccountNum(String accountNumber)
    {
        if (StringUtils.isBlank(accountNumber))
        {
            this.giro24 = null;
            return;
        }

        // Strip spaces and dashes (formatting characters)
        String cleaned = accountNumber.replaceAll("[\\s\\-]", "");

        // GIRO format: 16 or 24 digits
        if (cleaned.matches("\\d{16}"))
        {
            // Pad to 24 digits by appending 8 zeros
            cleaned = cleaned + "00000000";
        }
        this.giro24 = isValidFormat(cleaned) ? cleaned : null;
    }


    public boolean isValid()
    {
        return isValid(this.giro24);
    }


    public String getBankCode()
    {
        if (StringUtils.isBlank(this.giro24))
        {
            return null;
        }
        return this.giro24.substring(0, 8);
    }


    // the account number without the bank code
    public String getAccountNumber()
    {
        if (StringUtils.isBlank(this.giro24))
        {
            return null;
        }
        return this.giro24.substring(8);
    }


    public static boolean isValidFormat(String giro)
    {
        if (StringUtils.isBlank(giro))
        {
            return false;
        }

        String cleaned = giro.replaceAll("[\\s\\-]", "");
        return cleaned.matches("\\d{16}") || cleaned.matches("\\d{24}");
    }


    public static boolean isValid(String giro)
    {
        if (StringUtils.isBlank(giro))
        {
            return false;
        }

        String cleaned = giro.replaceAll("[\\s\\-]", "");
        if (!isValidFormat(cleaned))
        {
            return false;
        }

        // Weights for 7-digit and 15-digit CDV calculations
        int[] weights7  = {9, 7, 3, 1, 9, 7, 3};
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


    @Override
    public String toString()
    {
        return this.giro24;
    }
}
