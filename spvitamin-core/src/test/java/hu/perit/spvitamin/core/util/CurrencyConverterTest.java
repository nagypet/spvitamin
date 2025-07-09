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

class CurrencyConverterTest
{
    @Test
    void testGetStandardCodeWithSymbols()
    {
        // Test known currency symbols from the map
        assertThat(CurrencyConverter.getStandardCode("$")).isEqualTo("USD");
        assertThat(CurrencyConverter.getStandardCode("€")).isEqualTo("EUR");
        assertThat(CurrencyConverter.getStandardCode("£")).isEqualTo("GBP");
        assertThat(CurrencyConverter.getStandardCode("¥")).isEqualTo("JPY");
        assertThat(CurrencyConverter.getStandardCode("Ft")).isEqualTo("HUF");
        assertThat(CurrencyConverter.getStandardCode("₽")).isEqualTo("RUB");
        assertThat(CurrencyConverter.getStandardCode("Fr")).isEqualTo("CHF");
    }


    @Test
    void testGetStandardCodeWithCurrencyCodes()
    {
        // Test standard currency codes
        assertThat(CurrencyConverter.getStandardCode("USD")).isEqualTo("USD");
        assertThat(CurrencyConverter.getStandardCode("EUR")).isEqualTo("EUR");
        assertThat(CurrencyConverter.getStandardCode("GBP")).isEqualTo("GBP");
        assertThat(CurrencyConverter.getStandardCode("HUF")).isEqualTo("HUF");
    }


    @Test
    void testGetStandardCodeWithInvalidSymbol()
    {
        // Test invalid currency symbol (should return the original value)
        String invalidSymbol = "XYZ";
        assertThat(CurrencyConverter.getStandardCode(invalidSymbol)).isEqualTo(invalidSymbol);
    }


    @Test
    void testGetStandardCodeWithNull()
    {
        // Test with null input
        // This test verifies the behavior when null is passed
        assertThat(CurrencyConverter.getStandardCode(null)).isNull();
    }


    @Test
    void testGetStandardSyWithSymbols()
    {
        assertThat(CurrencyConverter.getStandardSymbol("USD")).isEqualTo("$");
        assertThat(CurrencyConverter.getStandardSymbol("EUR")).isEqualTo("€");
        assertThat(CurrencyConverter.getStandardSymbol("GBP")).isEqualTo("£");
        assertThat(CurrencyConverter.getStandardSymbol("JPY")).isEqualTo("¥");
        assertThat(CurrencyConverter.getStandardSymbol("HUF")).isEqualTo("Ft");
        assertThat(CurrencyConverter.getStandardSymbol("RUB")).isEqualTo("₽");
        assertThat(CurrencyConverter.getStandardSymbol("CHF")).isEqualTo("Fr");
    }


    @Test
    void testGetStandardSymbolWithInvalidCode()
    {
        // Test invalid currency code (should return the original value)
        String invalidSymbol = "XYZ";
        assertThat(CurrencyConverter.getStandardSymbol(invalidSymbol)).isEqualTo(invalidSymbol);
    }
}
