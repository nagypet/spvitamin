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

import static org.assertj.core.api.Assertions.assertThat;

class IbanTest
{
    private static final String VALID_GIRO = "100000012000000230000003";
    private static final String VALID_IBAN = "HU21" + VALID_GIRO;

    // -------------------------------------------------------------------------
    // isValidIban
    // -------------------------------------------------------------------------


    @Test
    void isValidIban_null_returnsFalse()
    {
        assertThat(Iban.isValidIban(null)).isFalse();
    }


    @Test
    void isValidIban_blank_returnsFalse()
    {
        assertThat(Iban.isValidIban("")).isFalse();
    }


    @Test
    void isValidIban_validHungarianIban_returnsTrue()
    {
        assertThat(Iban.isValidIban(VALID_IBAN)).isTrue();
    }


    @Test
    void isValidIban_validHungarianIbanLowercase_returnsTrue()
    {
        assertThat(Iban.isValidIban(VALID_IBAN.toLowerCase())).isTrue();
    }


    @Test
    void isValidIban_validHungarianIbanWithSpaces_returnsTrue()
    {
        assertThat(Iban.isValidIban("HU21 1000 0001 2000 0002 3000 0003")).isTrue();
    }


    @Test
    void isValidIban_validGermanIban_returnsTrue()
    {
        assertThat(Iban.isValidIban("DE89370400440532013000")).isTrue();
    }


    @Test
    void isValidIban_wrongCheckDigits_returnsFalse()
    {
        // HU21 érvényes, HU51 érvénytelen ellenőrzőjegyű
        assertThat(Iban.isValidIban("HU51" + VALID_GIRO)).isFalse();
    }


    @Test
    void isValidIban_startsWithDigit_returnsFalse()
    {
        assertThat(Iban.isValidIban("1234567812345678")).isFalse();
    }


    @Test
    void isValidIban_threeLetterCountryCode_returnsFalse()
    {
        assertThat(Iban.isValidIban("HUN50" + VALID_GIRO)).isFalse();
    }


    @Test
    void isValidIban_onlyCountryCode_returnsFalse()
    {
        assertThat(Iban.isValidIban("HU")).isFalse();
    }
}
