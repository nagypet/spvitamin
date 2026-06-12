/*
 * Copyright 2020-2025 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IbanConverterTest
{
    private static final String VALID_GIRO = "100000012000000230000003";
    private static final String VALID_IBAN = "HU21" + VALID_GIRO;

    // -------------------------------------------------------------------------
    // Null és blank bemenetek
    // -------------------------------------------------------------------------


    @Test
    void toIban_null_returnsNull()
    {
        assertThat(IbanConverter.toIban(null)).isNull();
    }


    @Test
    void toIban_emptyString_returnsNull()
    {
        assertThat(IbanConverter.toIban("")).isNull();
    }


    @Test
    void toIban_blankWithSpaces_returnsNull()
    {
        assertThat(IbanConverter.toIban("   ")).isNull();
    }


    @Test
    void toIban_blankWithTab_returnsNull()
    {
        assertThat(IbanConverter.toIban("\t")).isNull();
    }

    // -------------------------------------------------------------------------
    // Már IBAN formátumú bemenetek (2 betű + számjegyek)
    // -------------------------------------------------------------------------


    @Test
    void toIban_alreadyIban_returnsUnchanged()
    {
        assertThat(IbanConverter.toIban("HU09123456781234567800000000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_alreadyIbanLowercase_returnsUppercased()
    {
        assertThat(IbanConverter.toIban("hu09123456781234567800000000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_alreadyIbanWithSpaces_stripsSpacesAndUppercases()
    {
        assertThat(IbanConverter.toIban("HU09 1234 5678 1234 5678 0000 0000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_alreadyIbanWithDashes_stripsDashesAndUppercases()
    {
        assertThat(IbanConverter.toIban("HU09-1234-5678-1234-5678-0000-0000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_nonHungarianIban_returnedAsIs()
    {
        // Külföldi IBAN-t nem alakítunk, csak visszaadjuk nagybetűsen
        assertThat(IbanConverter.toIban("DE89370400440532013000"))
                .isEqualTo("DE89370400440532013000");
    }


    @Test
    void toIban_nonHungarianIbanLowercase_returnedUppercased()
    {
        // Csak numerikus BBAN-ú külföldi IBAN ismerhető fel (pl. DE), mert a regex [A-Za-z]{2}\d+ csak 2 betű + kizárólag számjegyeket fogad el
        assertThat(IbanConverter.toIban("de89370400440532013000"))
                .isEqualTo("DE89370400440532013000");
    }

    // -------------------------------------------------------------------------
    // 16 jegyű hazai számlaszám (a végére 8 nulla kerül)
    // -------------------------------------------------------------------------


    @Test
    void toIban_16Digits_computesCorrectIban()
    {
        // 1234567812345678 → 123456781234567800000000 → HU09...
        assertThat(IbanConverter.toIban("1234567812345678"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_16DigitsAllZeros_computesCorrectIban()
    {
        // 16 db nulla → 24 db nulla BBAN → HU49...
        assertThat(IbanConverter.toIban("0000000000000000"))
                .isEqualTo("HU49000000000000000000000000");
    }


    @Test
    void toIban_16DigitsWithSpaces_stripsAndComputesIban()
    {
        assertThat(IbanConverter.toIban("1234 5678 1234 5678"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_16DigitsWithDashes_stripsAndComputesIban()
    {
        assertThat(IbanConverter.toIban("12345678-12345678"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_16DigitsWithMixedFormatting_stripsAndComputesIban()
    {
        assertThat(IbanConverter.toIban("1234 5678-1234 5678"))
                .isEqualTo("HU09123456781234567800000000");
    }

    // -------------------------------------------------------------------------
    // 24 jegyű hazai számlaszám (teljes BBAN)
    // -------------------------------------------------------------------------


    @Test
    void toIban_24Digits_computesCorrectIban()
    {
        // 16+8 nullával padded változat és a 24 jegyű közvetlen → azonos eredmény
        assertThat(IbanConverter.toIban("123456781234567800000000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_24DigitsAllOnes_computesCorrectIban()
    {
        // 24 db egyes → HU21...
        assertThat(IbanConverter.toIban("111111111111111111111111"))
                .isEqualTo("HU21111111111111111111111111");
    }


    @Test
    void toIban_24DigitsWithSpaces_stripsAndComputesIban()
    {
        assertThat(IbanConverter.toIban("1234 5678 1234 5678 0000 0000"))
                .isEqualTo("HU09123456781234567800000000");
    }

    // -------------------------------------------------------------------------
    // IBAN struktúra ellenőrzése
    // -------------------------------------------------------------------------


    @Test
    void toIban_result_has28Characters()
    {
        assertThat(IbanConverter.toIban("1234567812345678")).hasSize(28);
        assertThat(IbanConverter.toIban("123456781234567800000000")).hasSize(28);
    }


    @Test
    void toIban_result_startsWithHU()
    {
        assertThat(IbanConverter.toIban("1234567812345678")).startsWith("HU");
        assertThat(IbanConverter.toIban("111111111111111111111111")).startsWith("HU");
    }


    @Test
    void toIban_16Digits_checkDigitIsValid()
    {
        // Érvényes IBAN esetén a teljes numerikus érték mod 97 == 1
        assertIbanChecksumValid(IbanConverter.toIban("1234567812345678"));
    }


    @Test
    void toIban_24Digits_checkDigitIsValid()
    {
        assertIbanChecksumValid(IbanConverter.toIban("111111111111111111111111"));
    }


    @Test
    void toIban_16DigitsAllZeros_checkDigitIsValid()
    {
        assertIbanChecksumValid(IbanConverter.toIban("0000000000000000"));
    }

    // -------------------------------------------------------------------------
    // 16 jegyű és 24 jegyű bemenet azonos eredményt ad (a 8 nullás padding miatt)
    // -------------------------------------------------------------------------


    @Test
    void toIban_16DigitsPaddedEquals24DigitsResult()
    {
        String from16 = IbanConverter.toIban("1234567812345678");
        String from24 = IbanConverter.toIban("123456781234567800000000");
        assertThat(from16).isEqualTo(from24);
    }

    // -------------------------------------------------------------------------
    // Érvénytelen formátumok → IllegalArgumentException
    // -------------------------------------------------------------------------


    @ParameterizedTest
    @ValueSource(strings = {
            "1",                           // 1 jegy
            "123456789",                   // 9 jegy
            "123456781234567",             // 15 jegy
            "12345678123456789",           // 17 jegy
            "12345678123456789012345",     // 23 jegy
            "1234567812345678901234567",   // 25 jegy
            "123456781234567890123456789", // 27 jegy (nem IBAN, nem 16/24 számjegy)
            "1ABC234567",                  // betűk, de nem IBAN-szerű elején
            "1234-AB-5678",               // kötőjel eltávolítás után betű közben
            "ABCD1234",                   // 4 betű + számjegyek (nem 2+n minta)
    })
    void toIban_invalidFormat_throwsIllegalArgumentException(String invalid)
    {
        assertThat(IbanConverter.isValidAccountNum(invalid)).isFalse();
        assertThatThrownBy(() -> IbanConverter.toIban(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalid);
    }


    // -------------------------------------------------------------------------
    // isValidIban
    // -------------------------------------------------------------------------


    @Test
    void isValidIban_null_returnsFalse()
    {
        assertThat(IbanConverter.isValidIban(null)).isFalse();
    }


    @Test
    void isValidIban_blank_returnsFalse()
    {
        assertThat(IbanConverter.isValidIban("")).isFalse();
    }


    @Test
    void isValidIban_validHungarianIban_returnsTrue()
    {
        assertThat(IbanConverter.isValidIban(VALID_IBAN)).isTrue();
    }


    @Test
    void isValidIban_validHungarianIbanLowercase_returnsTrue()
    {
        assertThat(IbanConverter.isValidIban(VALID_IBAN.toLowerCase())).isTrue();
    }


    @Test
    void isValidIban_validHungarianIbanWithSpaces_returnsTrue()
    {
        assertThat(IbanConverter.isValidIban("HU21 1000 0001 2000 0002 3000 0003")).isTrue();
    }


    @Test
    void isValidIban_validGermanIban_returnsTrue()
    {
        assertThat(IbanConverter.isValidIban("DE89370400440532013000")).isTrue();
    }


    @Test
    void isValidIban_wrongCheckDigits_returnsFalse()
    {
        // HU50 érvényes, HU51 érvénytelen ellenőrzőjegyű
        assertThat(IbanConverter.isValidIban("HU51" + VALID_GIRO)).isFalse();
    }


    @Test
    void isValidIban_startsWithDigit_returnsFalse()
    {
        assertThat(IbanConverter.isValidIban("1234567812345678")).isFalse();
    }


    @Test
    void isValidIban_threeLetterCountryCode_returnsFalse()
    {
        assertThat(IbanConverter.isValidIban("HUN50" + VALID_GIRO)).isFalse();
    }


    @Test
    void isValidIban_onlyCountryCode_returnsFalse()
    {
        assertThat(IbanConverter.isValidIban("HU")).isFalse();
    }

    // -------------------------------------------------------------------------
    // isValidAccountNum
    // -------------------------------------------------------------------------


    @Test
    void isValidAccountNum_null_returnsFalse()
    {
        assertThat(IbanConverter.isValidAccountNum(null)).isFalse();
    }


    @Test
    void isValidAccountNum_blank_returnsFalse()
    {
        assertThat(IbanConverter.isValidAccountNum("")).isFalse();
    }


    @Test
    void isValidAccountNum_valid24DigitGiro_returnsTrue()
    {
        assertThat(IbanConverter.isValidAccountNum(VALID_GIRO)).isTrue();
    }


    @Test
    void isValidAccountNum_valid16DigitWithValidGiroBlocks_returnsTrue()
    {
        // Blokk 1: 10000001, Blokk 2: 20000002, Blokk 3: 00000000 (padding után mindig érvényes)
        assertThat(IbanConverter.isValidAccountNum("1000000120000002")).isTrue();
    }


    @Test
    void isValidAccountNum_validHungarianIban_returnsTrue()
    {
        assertThat(IbanConverter.isValidAccountNum(VALID_IBAN)).isTrue();
    }


    @Test
    void isValidAccountNum_validForeignIban_returnsTrue()
    {
        // Külföldi IBAN esetén csak az IBAN ellenőrzőjegy kell, GIRO checksum nem
        assertThat(IbanConverter.isValidAccountNum("DE89370400440532013000")).isTrue();
    }


    @Test
    void isValidAccountNum_invalidGiroChecksum_returnsFalse()
    {
        // A 24-jegyű GIRO checksumja érvénytelen → false
        assertThat(IbanConverter.isValidAccountNum("123456781234567800000000")).isFalse();
    }


    @Test
    void isValidAccountNum_invalidIbanChecksum_returnsFalse()
    {
        assertThat(IbanConverter.isValidAccountNum("HU51" + VALID_GIRO)).isFalse();
    }


    @ParameterizedTest
    @ValueSource(strings = {
            VALID_GIRO,
            "1000000120000002",
            VALID_IBAN,
            "DE89370400440532013000",
    })
    void isValidAccountNum_whenTrue_toIbanDoesNotThrow(String accountNum)
    {
        // Ha isValidAccountNum true-t ad vissza, convertToIban nem dobhat kivételt ugyanarra a bemenetre
        assertThat(IbanConverter.isValidAccountNum(accountNum)).isTrue();
        assertThatCode(() -> IbanConverter.toIban(accountNum)).doesNotThrowAnyException();
    }

    // -------------------------------------------------------------------------
    // Segédmetódus: ISO 7064 mod-97-10 ellenőrzés
    // -------------------------------------------------------------------------


    /**
     * Ellenőrzi, hogy az IBAN checksum érvényes-e (teljes numerikus érték mod 97 == 1).
     */
    private static void assertIbanChecksumValid(String iban)
    {
        // BBAN + országkód + ellenőrzőjegyek sorrendbe rendezve
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray())
        {
            if (Character.isLetter(c))
            {
                numeric.append(Character.getNumericValue(c));
            }
            else
            {
                numeric.append(c);
            }
        }
        int remainder = new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue();
        assertThat(remainder)
                .as("Az IBAN ellenőrzőjegy érvénytelen (mod 97 != 1): %s", iban)
                .isEqualTo(1);
    }
}
