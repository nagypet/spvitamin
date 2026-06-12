package hu.perit.spvitamin.core.util;

import lombok.experimental.UtilityClass;

import java.text.Normalizer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * SEPA karakterkészlet szerinti szanitizáló és validátor.
 * <p>
 * A SEPA (EPC) szabvány az alábbi karaktereket engedélyezi fizetési mezőkben:
 * a-z, A-Z, 0-9, szóköz, valamint: / - ? : ( ) . , ' +
 */
@UtilityClass
public final class SepaSanitizer
{
    private static final Set<Character> ALLOWED_CHARS = buildAllowedChars();


    private static Set<Character> buildAllowedChars()
    {
        Set<Character> chars = new HashSet<>();
        for (char c : "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789/-?:(). ,'+".toCharArray())
        {
            chars.add(c);
        }
        return Collections.unmodifiableSet(chars);
    }


    public void validate(String value)
    {
        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            if (!ALLOWED_CHARS.contains(c))
            {
                throw new IllegalArgumentException(
                        "Character '" + c + "' (U+" + String.format("%04X", (int) c) +
                                ") at index " + i + " in value is not representable in SEPA charset");
            }
        }
    }


    public String sanitize(String input)
    {
        // Speciális többkarakteres cserék – NFD-vel nem bonthatók le alapkarakterre
        String preprocessed = input
                .replace("Ä", "Ae")
                .replace("ä", "ae")
                .replace("Ö", "Oe")
                .replace("ö", "oe")
                .replace("Ü", "Ue")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replace("Æ", "AE")
                .replace("æ", "ae")
                .replace("Ø", "O")
                .replace("ø", "o");

        StringBuilder result = new StringBuilder();

        for (char c : preprocessed.toCharArray())
        {
            if (ALLOWED_CHARS.contains(c))
            {
                result.append(c);
            }
            else
            {
                // Megpróbáljuk NFD normalizálással az alapkaraktert kinyerni (pl. á→a, Ő→O)
                String decomposed = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
                String baseChars = decomposed.replaceAll("\\p{M}", "");
                if (!baseChars.isEmpty() && ALLOWED_CHARS.contains(baseChars.charAt(0)))
                {
                    result.append(baseChars.charAt(0));
                }
                else
                {
                    result.append('?');
                }
            }
        }

        return result.toString();
    }
}
