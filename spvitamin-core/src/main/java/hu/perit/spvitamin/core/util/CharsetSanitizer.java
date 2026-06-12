package hu.perit.spvitamin.core.util;

import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class CharsetSanitizer
{
    public static final Charset CP852 = Charset.forName("IBM852");

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
        // Speciális többkarakteres cserék – ezek a karakterek this.charset-ben nem szerepelnek
        String preprocessed = input
                .replace("ß", "ss")
                .replace("Æ", "AE")
                .replace("æ", "ae")
                .replace("Ø", "O")
                .replace("ø", "o");

        CharsetEncoder encoder = this.charset.newEncoder();
        StringBuilder result = new StringBuilder();

        for (char c : preprocessed.toCharArray())
        {
            if (encoder.canEncode(c))
            {
                // A karakter this.charset-ben kódolható – megtartjuk (pl. magyar ékezetes betűk)
                result.append(c);
            }
            else
            {
                // Megpróbáljuk NFD normalizálással az alapkaraktert kinyerni
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

        return result.toString();
    }
}
