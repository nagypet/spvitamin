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

class BankAccountNumberConverterTest
{
    private static final String VALID_GIRO = "100000012000000230000003";
    private static final String VALID_IBAN = "HU21" + VALID_GIRO;

    // -------------------------------------------------------------------------
    // Null és blank bemenetek
    // -------------------------------------------------------------------------


    @Test
    void toIban_String_null_returnsNull()
    {
        assertThat(BankAccountNumberConverter.toIbanString(null)).isNull();
    }


    @Test
    void toIban_String_emptyString_returnsNull()
    {
        assertThat(BankAccountNumberConverter.toIbanString("")).isNull();
    }


    @Test
    void toIban_String_blankWithSpaces_returnsNull()
    {
        assertThat(BankAccountNumberConverter.toIbanString("   ")).isNull();
    }


    @Test
    void toIban_String_blankWithTab_returnsNull()
    {
        assertThat(BankAccountNumberConverter.toIbanString("\t")).isNull();
    }

    // -------------------------------------------------------------------------
    // Már IBAN formátumú bemenetek (2 betű + számjegyek)
    // -------------------------------------------------------------------------


    @Test
    void toIban_alreadyIban_String_returnsUnchanged()
    {
        assertThat(BankAccountNumberConverter.toIbanString("HU09123456781234567800000000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_alreadyIbanStringLowercase_returnsUppercased()
    {
        assertThat(BankAccountNumberConverter.toIbanString("hu09123456781234567800000000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_alreadyIbanStringWithSpaces_stripsSpacesAndUppercases()
    {
        assertThat(BankAccountNumberConverter.toIbanString("HU09 1234 5678 1234 5678 0000 0000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_alreadyIbanStringWithDashes_stripsDashesAndUppercases()
    {
        assertThat(BankAccountNumberConverter.toIbanString("HU09-1234-5678-1234-5678-0000-0000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_nonHungarianIban_String_returnedAsIs()
    {
        // Külföldi IBAN-t nem alakítunk, csak visszaadjuk nagybetűsen
        assertThat(BankAccountNumberConverter.toIbanString("DE89370400440532013000"))
                .isEqualTo("DE89370400440532013000");
    }


    @Test
    void toIban_nonHungarianIbanStringLowercase_returnedUppercased()
    {
        // Csak numerikus BBAN-ú külföldi IBAN ismerhető fel (pl. DE), mert a regex [A-Za-z]{2}\d+ csak 2 betű + kizárólag számjegyeket fogad el
        assertThat(BankAccountNumberConverter.toIbanString("de89370400440532013000"))
                .isEqualTo("DE89370400440532013000");
    }

    // -------------------------------------------------------------------------
    // 16 jegyű hazai számlaszám (a végére 8 nulla kerül)
    // -------------------------------------------------------------------------


    @Test
    void toIban_16Digits_computesCorrectIbanString()
    {
        // 1234567812345678 → 123456781234567800000000 → HU09...
        assertThat(BankAccountNumberConverter.toIbanString("1234567812345678"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_16DigitsAllZeros_computesCorrectIbanString()
    {
        // 16 db nulla → 24 db nulla BBAN → HU49...
        assertThat(BankAccountNumberConverter.toIbanString("0000000000000000"))
                .isEqualTo("HU49000000000000000000000000");
    }


    @Test
    void toIban_16DigitsWithSpaces_stripsAndComputesIbanString()
    {
        assertThat(BankAccountNumberConverter.toIbanString("1234 5678 1234 5678"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_16DigitsWithDashes_stripsAndComputesIbanString()
    {
        assertThat(BankAccountNumberConverter.toIbanString("12345678-12345678"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_16DigitsWithMixedFormatting_stripsAndComputesIbanString()
    {
        assertThat(BankAccountNumberConverter.toIbanString("1234 5678-1234 5678"))
                .isEqualTo("HU09123456781234567800000000");
    }

    // -------------------------------------------------------------------------
    // 24 jegyű hazai számlaszám (teljes BBAN)
    // -------------------------------------------------------------------------


    @Test
    void toIban_24Digits_computesCorrectIbanString()
    {
        // 16+8 nullával padded változat és a 24 jegyű közvetlen → azonos eredmény
        assertThat(BankAccountNumberConverter.toIbanString("123456781234567800000000"))
                .isEqualTo("HU09123456781234567800000000");
    }


    @Test
    void toIban_24DigitsAllOnes_computesCorrectIbanString()
    {
        // 24 db egyes → HU21...
        assertThat(BankAccountNumberConverter.toIbanString("111111111111111111111111"))
                .isEqualTo("HU21111111111111111111111111");
    }


    @Test
    void toIban_24DigitsWithSpaces_stripsAndComputesIbanString()
    {
        assertThat(BankAccountNumberConverter.toIbanString("1234 5678 1234 5678 0000 0000"))
                .isEqualTo("HU09123456781234567800000000");
    }

    // -------------------------------------------------------------------------
    // IBAN struktúra ellenőrzése
    // -------------------------------------------------------------------------


    @Test
    void toIban_String_result_has28Characters()
    {
        assertThat(BankAccountNumberConverter.toIbanString("1234567812345678")).hasSize(28);
        assertThat(BankAccountNumberConverter.toIbanString("123456781234567800000000")).hasSize(28);
    }


    @Test
    void toIban_String_result_startsWithHU()
    {
        assertThat(BankAccountNumberConverter.toIbanString("1234567812345678")).startsWith("HU");
        assertThat(BankAccountNumberConverter.toIbanString("111111111111111111111111")).startsWith("HU");
    }


    @Test
    void toIban_String_16Digits_checkDigitIsValid()
    {
        // Érvényes IBAN esetén a teljes numerikus érték mod 97 == 1
        assertIbanChecksumValid(BankAccountNumberConverter.toIbanString("1234567812345678"));
    }


    @Test
    void toIban_String_24Digits_checkDigitIsValid()
    {
        assertIbanChecksumValid(BankAccountNumberConverter.toIbanString("111111111111111111111111"));
    }


    @Test
    void toIban_String_16DigitsAllZeros_checkDigitIsValid()
    {
        assertIbanChecksumValid(BankAccountNumberConverter.toIbanString("0000000000000000"));
    }

    // -------------------------------------------------------------------------
    // 16 jegyű és 24 jegyű bemenet azonos eredményt ad (a 8 nullás padding miatt)
    // -------------------------------------------------------------------------


    @Test
    void toIban_String_16DigitsPaddedEquals24DigitsResult()
    {
        String from16 = BankAccountNumberConverter.toIbanString("1234567812345678");
        String from24 = BankAccountNumberConverter.toIbanString("123456781234567800000000");
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
    void toIban_String_invalidFormat_throwsIllegalArgumentException(String invalid)
    {
        assertThat(BankAccountNumberConverter.isValidAccountNum(invalid)).isFalse();
        assertThatThrownBy(() -> BankAccountNumberConverter.toIbanString(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalid);
    }


    // -------------------------------------------------------------------------
    // isValidAccountNum
    // -------------------------------------------------------------------------


    @Test
    void isValidAccountNum_null_returnsFalse()
    {
        assertThat(BankAccountNumberConverter.isValidAccountNum(null)).isFalse();
    }


    @Test
    void isValidAccountNum_blank_returnsFalse()
    {
        assertThat(BankAccountNumberConverter.isValidAccountNum("")).isFalse();
    }


    @Test
    void isValidAccountNum_valid24DigitGiro_returnsTrue()
    {
        assertThat(BankAccountNumberConverter.isValidAccountNum(VALID_GIRO)).isTrue();
    }


    @Test
    void isValidAccountNum_valid16DigitWithValidGiroBlocks_returnsTrue()
    {
        // Blokk 1: 10000001, Blokk 2: 20000002, Blokk 3: 00000000 (padding után mindig érvényes)
        assertThat(BankAccountNumberConverter.isValidAccountNum("1000000120000002")).isTrue();
    }


    @Test
    void isValidAccountNum_validHungarianIban_returnsTrue()
    {
        assertThat(BankAccountNumberConverter.isValidAccountNum(VALID_IBAN)).isTrue();
    }


    @Test
    void isValidAccountNum_validForeignIban_returnsTrue()
    {
        // Külföldi IBAN esetén csak az IBAN ellenőrzőjegy kell, GIRO checksum nem
        assertThat(BankAccountNumberConverter.isValidAccountNum("DE89370400440532013000")).isTrue();
    }


    @Test
    void isValidAccountNum_invalidGiroChecksum_returnsFalse()
    {
        // A 24-jegyű GIRO checksumja érvénytelen → false
        assertThat(BankAccountNumberConverter.isValidAccountNum("123456781234567800000000")).isFalse();
    }


    @Test
    void isValidAccountNum_invalidIbanChecksum_returnsFalse()
    {
        assertThat(BankAccountNumberConverter.isValidAccountNum("HU51" + VALID_GIRO)).isFalse();
    }


    @ParameterizedTest
    @ValueSource(strings = {
            VALID_GIRO,
            "1000000120000002",
            VALID_IBAN,
            "DE89370400440532013000",
    })
    void isValidAccountNum_whenTrue_toIbanStringDoesNotThrow(String accountNum)
    {
        // Ha isValidAccountNum true-t ad vissza, convertToIban nem dobhat kivételt ugyanarra a bemenetre
        assertThat(BankAccountNumberConverter.isValidAccountNum(accountNum)).isTrue();
        assertThatCode(() -> BankAccountNumberConverter.toIbanString(accountNum)).doesNotThrowAnyException();
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
