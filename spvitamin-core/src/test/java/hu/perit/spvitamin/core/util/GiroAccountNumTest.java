package hu.perit.spvitamin.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class GiroAccountNumTest
{
    // Érvényes tesztadat: 3 db 8-jegyű blokk, mindegyik helyes ellenőrzőjeggyel:
    //   Blokk 1: 10000001  (1*9=9,  check=(10-9)%10=1)
    //   Blokk 2: 20000002  (2*9=18, check=(10-8)%10=2)
    //   Blokk 3: 30000003  (3*9=27, check=(10-7)%10=3)
    private static final String VALID_GIRO = "100000012000000230000003";
    private static final String VALID_GIRO_FORMATTED = "10000001-20000002-30000003";


    @Test
    void isValidGiro_null_returnsFalse()
    {
        assertThat(GiroAccountNum.isValid(null)).isFalse();
    }


    @Test
    void isValidGiro_blank_returnsFalse()
    {
        assertThat(GiroAccountNum.isValid("")).isFalse();
    }


    @Test
    void isValidGiro_spacesOnly_returnsFalse()
    {
        assertThat(GiroAccountNum.isValid("   ")).isFalse();
    }


    @Test
    void isValidGiro_valid24Digits_returnsTrue()
    {
        assertThat(GiroAccountNum.isValid(VALID_GIRO)).isTrue();
        assertThat(GiroAccountNum.fromString(VALID_GIRO)).hasToString(VALID_GIRO);
        assertThat(GiroAccountNum.fromString(VALID_GIRO).value()).isEqualTo(VALID_GIRO);
        assertThat(GiroAccountNum.fromString(VALID_GIRO).format()).isEqualTo(VALID_GIRO_FORMATTED);
    }


    @Test
    void isValidGiro_valid16Digits_returnsTrue()
    {
        // 2 blokk: 10000001 20000002
        assertThat(GiroAccountNum.isValid("1000000120000002")).isTrue();
    }


    @Test
    void isValidGiro_valid16DigitsWithDash_returnsTrue()
    {
        assertThat(GiroAccountNum.isValid("10000001-20000002")).isTrue();
    }


    @Test
    void isValidGiro_allZeros_returnsTrue()
    {
        // Minden blokk: 00000000 → összeg=0, ellenőrzőjegy=(10-0)%10=0
        assertThat(GiroAccountNum.isValid("000000000000000000000000")).isTrue();
    }


    @Test
    void isValidGiro_validWithSpaces_returnsTrue()
    {
        assertThat(GiroAccountNum.isValid("10000001 20000002 30000003")).isTrue();
    }


    @Test
    void isValidGiro_validWithDashes_returnsTrue()
    {
        assertThat(GiroAccountNum.isValid("10000001-20000002-30000003")).isTrue();
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "1",
            "12345678",                    // 8 jegy (túl rövid)
            "1234567890123456789012345",   // 25 jegy (túl hosszú)
            "ABCDEFGH12345678ABCDEFGH",   // betűket tartalmaz
    })
    void isValidGiro_wrongLengthOrFormat_returnsFalse(String giro)
    {
        assertThat(GiroAccountNum.isValid(giro)).isFalse();
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "12001008-00238600-00400005",
            "10032000-01076019",
            "10032000-01076019-00000000",
            "10032000-06056353",
            "10032000-01076868",
    })
    void isValidGiro_validHungarianGiroWithDashes_returnsTrue(String giro)
    {
        assertThat(GiroAccountNum.isValid(giro)).isTrue();
    }
}
