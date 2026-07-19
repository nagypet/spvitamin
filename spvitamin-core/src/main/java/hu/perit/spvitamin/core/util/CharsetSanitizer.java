package hu.perit.spvitamin.core.util;

import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class CharsetSanitizer
{
    public static final Charset CP852 = Charset.forName("IBM852");

    private static final Map<Character, String> REPLACEMENT_MAP;

    static
    {
        REPLACEMENT_MAP = new HashMap<>();
        REPLACEMENT_MAP.put('ß', "ss");
        REPLACEMENT_MAP.put('Æ', "AE");
        REPLACEMENT_MAP.put('æ', "ae");
        REPLACEMENT_MAP.put('Ø', "O");
        REPLACEMENT_MAP.put('ø', "o");
    }

    private final Charset charset;


    public static CharsetSanitizer of(Charset charset)
    {
        return new CharsetSanitizer(charset);
    }


    public void validateCharset(String value)
    {
        byte[] encoded = value.getBytes(this.charset);
        String decoded = new String(encoded, this.charset);
        if (!decoded.equals(value))
        {
            for (int i = 0; i < value.length(); i++)
            {
                char c = value.charAt(i);
                String s = String.valueOf(c);
                if (!new String(s.getBytes(this.charset), this.charset).equals(s))
                {
                    throw new IllegalArgumentException(
                            "Character '" + c + "' (U+" + String.format("%04X", (int) c) +
                                    ") at index " + i + " in value is not representable in " + this.charset.name() + " charset");
                }
            }
        }
    }


    public String sanitize(String input)
    {
        if (input == null)
        {
            return null;
        }

        CharsetEncoder encoder = this.charset.newEncoder();
        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray())
        {
            if (encoder.canEncode(c))
            {
                // The character can be encoded in the target charset - keep it
                result.append(c);
            }
            else
            {
                // Multi-character replacements
                if (REPLACEMENT_MAP.containsKey(c))
                {
                    result.append(REPLACEMENT_MAP.get(c));
                }
                else
                {
                    // Trying to get the base character by NFD normalization
                    String decomposed = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
                    String baseChars = decomposed.replaceAll("\\p{M}", "");
                    if (!baseChars.isEmpty() && encoder.canEncode(baseChars.charAt(0)))
                    {
                        result.append(baseChars.charAt(0));
                    }
                    else
                    {
                        result.append('?');
                    }
                }
            }
        }

        return result.toString();
    }
}
