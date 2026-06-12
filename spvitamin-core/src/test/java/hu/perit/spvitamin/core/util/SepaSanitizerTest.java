package hu.perit.spvitamin.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SepaSanitizerTest
{
    // -------------------------------------------------------------------------
    // Alap ASCII – változatlan marad
    // -------------------------------------------------------------------------


    @Test
    void sanitize_asciiOnly_returnsUnchanged()
    {
        assertThat(SepaSanitizer.sanitize("Hello World 123")).isEqualTo("Hello World 123");
    }


    @Test
    void sanitize_allowedSpecialChars_returnsUnchanged()
    {
        assertThat(SepaSanitizer.sanitize("/-?:(). ,'+")).isEqualTo("/-?:(). ,'+");
    }


    @Test
    void sanitize_emptyString_returnsEmpty()
    {
        assertThat(SepaSanitizer.sanitize("")).isEqualTo("");
    }

    // -------------------------------------------------------------------------
    // Magyar és német ékezetes betűk
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "á, a",
            "é, e",
            "ä, ae",
            "í, i",
            "ó, o",
            "ö, oe",
            "ő, o",
            "ú, u",
            "ü, ue",
            "ű, u",
            "Á, A",
            "É, E",
            "Ä, Ae",
            "Í, I",
            "Ó, O",
            "Ö, Oe",
            "Ő, O",
            "Ú, U",
            "Ü, Ue",
            "Ű, U",
    })
    void sanitize_withTransliteration(String input, String expected)
    {
        assertThat(SepaSanitizer.sanitize(input)).isEqualTo(expected);
    }


    @Test
    void sanitize_german_accentStripped()
    {
        assertThat(SepaSanitizer.sanitize("Jürgen Müller")).isEqualTo("Juergen Mueller");
    }

    // -------------------------------------------------------------------------
    // Speciális többkarakteres helyettesítések
    // -------------------------------------------------------------------------


    @Test
    void sanitize_germanEszett_replacedWithSs()
    {
        assertThat(SepaSanitizer.sanitize("Straße")).isEqualTo("Strasse");
    }


    @Test
    void sanitize_AELigature_replacedCorrectly()
    {
        assertThat(SepaSanitizer.sanitize("Æ")).isEqualTo("AE");
        assertThat(SepaSanitizer.sanitize("æ")).isEqualTo("ae");
    }


    @Test
    void sanitize_OSlash_replacedCorrectly()
    {
        assertThat(SepaSanitizer.sanitize("Ø")).isEqualTo("O");
        assertThat(SepaSanitizer.sanitize("ø")).isEqualTo("o");
    }


    @Test
    void sanitize_scandinavianWord_replacedCorrectly()
    {
        // Æ→AE, r, ø→o
        assertThat(SepaSanitizer.sanitize("Ærø")).isEqualTo("AEro");
    }

    // -------------------------------------------------------------------------
    // Nem kódolható karakterek – '?' helyettesítés
    // -------------------------------------------------------------------------


    @Test
    void sanitize_chineseChar_replacedWithQuestionMark()
    {
        assertThat(SepaSanitizer.sanitize("中")).isEqualTo("?");
    }


    @Test
    void sanitize_greekChar_replacedWithQuestionMark()
    {
        assertThat(SepaSanitizer.sanitize("α")).isEqualTo("?");
    }


    @Test
    void sanitize_mixedSepaAndNonSepa_replacesOnlyInvalid()
    {
        assertThat(SepaSanitizer.sanitize("AB中CD")).isEqualTo("AB?CD");
    }

    // -------------------------------------------------------------------------
    // validate
    // -------------------------------------------------------------------------


    @Test
    void validate_validSepaString_noException()
    {
        SepaSanitizer.validate("Invoice 2025/01-ABC (ref: 42)");
    }


    @Test
    void validate_hungarianAccentedChar_throwsException()
    {
        assertThatThrownBy(() -> SepaSanitizer.validate("Január"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SEPA charset");
    }


    @Test
    void validate_chineseChar_throwsException()
    {
        assertThatThrownBy(() -> SepaSanitizer.validate("ABC中DEF"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("U+4E2D");
    }
}
