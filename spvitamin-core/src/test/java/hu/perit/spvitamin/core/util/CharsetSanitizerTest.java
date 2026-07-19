package hu.perit.spvitamin.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static hu.perit.spvitamin.core.util.CharsetSanitizer.CP852;
import static org.assertj.core.api.Assertions.assertThat;

class CharsetSanitizerTest
{
    // -------------------------------------------------------------------------
    // Alap ASCII – változatlan marad
    // -------------------------------------------------------------------------


    @Test
    void sanitize_asciiOnly_returnsUnchanged()
    {
        assertThat(CharsetSanitizer.of(CP852).sanitize("Hello World 123!")).isEqualTo("Hello World 123!");
    }


    @Test
    void sanitize_emptyString_returnsEmpty()
    {
        assertThat(CharsetSanitizer.of(CP852).sanitize("")).isEqualTo("");
    }

    // -------------------------------------------------------------------------
    // Magyar ékezetes betűk – CP852-ben szerepelnek, változatlanul megmaradnak
    // -------------------------------------------------------------------------


    @ParameterizedTest
    @CsvSource({
            "á, á",
            "é, é",
            "í, í",
            "ó, ó",
            "ö, ö",
            "ő, ő",
            "ú, ú",
            "ü, ü",
            "ű, ű",
            "Á, Á",
            "É, É",
            "Í, Í",
            "Ó, Ó",
            "Ö, Ö",
            "Ő, Ő",
            "Ú, Ú",
            "Ü, Ü",
            "Ű, Ű",
    })
    void sanitize_hungarianAccentedChar_remainsUnchanged(String input, String expected)
    {
        assertThat(CharsetSanitizer.of(CP852).sanitize(input)).isEqualTo(expected);
    }


    @Test
    void sanitize_hungarianPangram_remainsUnchanged()
    {
        // Minden magyar ékezetes karakter megtalálható a CP852-ben
        assertThat(CharsetSanitizer.of(CP852).sanitize("Árvíztűrő tükörfúrógép"))
                .isEqualTo("Árvíztűrő tükörfúrógép");
    }

    // -------------------------------------------------------------------------
    // Speciális helyettesítések
    // -------------------------------------------------------------------------


    @Test
    void sanitize_germanEszett_replacedWithSs()
    {
        assertThat(CharsetSanitizer.of(StandardCharsets.US_ASCII).sanitize("Straße")).isEqualTo("Strasse");
    }


    @Test
    void sanitize_AELigature_replacedCorrectly()
    {
        assertThat(CharsetSanitizer.of(CP852).sanitize("Æ")).isEqualTo("AE");
        assertThat(CharsetSanitizer.of(CP852).sanitize("æ")).isEqualTo("ae");
    }


    @Test
    void sanitize_OSlash_replacedCorrectly()
    {
        assertThat(CharsetSanitizer.of(CP852).sanitize("Ø")).isEqualTo("O");
        assertThat(CharsetSanitizer.of(CP852).sanitize("ø")).isEqualTo("o");
    }


    @Test
    void sanitize_scandinavianWord_replacedCorrectly()
    {
        // Æ→AE, r, ø→o
        assertThat(CharsetSanitizer.of(CP852).sanitize("Ærø")).isEqualTo("AEro");
    }

    // -------------------------------------------------------------------------
    // CP852-ben nem kódolható karakterek – '?' helyettesítés
    // -------------------------------------------------------------------------


    @Test
    void sanitize_chineseChar_replacedWithQuestionMark()
    {
        assertThat(CharsetSanitizer.of(CP852).sanitize("中")).isEqualTo("?");
    }


    @Test
    void sanitize_greekChar_replacedWithQuestionMark()
    {
        assertThat(CharsetSanitizer.of(CP852).sanitize("α")).isEqualTo("?");
    }


    @Test
    void sanitize_mixedCp852AndNonCp852_replacesOnlyNonEncodable()
    {
        assertThat(CharsetSanitizer.of(CP852).sanitize("AB中CD")).isEqualTo("AB?CD");
    }

    // -------------------------------------------------------------------------
    // Kombinált eset – valós fizetési közlemény
    // -------------------------------------------------------------------------


    @Test
    void sanitize_realWorldPaymentText_sanitizedCorrectly()
    {
        // Minden karakter CP852-ben kódolható – változatlan marad
        assertThat(CharsetSanitizer.of(CP852).sanitize("Január 2025 - számla díja"))
                .isEqualTo("Január 2025 - számla díja");
    }
}
