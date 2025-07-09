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

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j
public final class CurrencyConverter
{
    private static final Map<String, String> SYMBOL_TO_CODE = new HashMap<>();
    private static final Map<String, String> CODE_TO_SYMBOL = new HashMap<>();

    static
    {
        SYMBOL_TO_CODE.put("$", "USD");
        SYMBOL_TO_CODE.put("€", "EUR");
        SYMBOL_TO_CODE.put("£", "GBP");
        SYMBOL_TO_CODE.put("¥", "JPY");
        SYMBOL_TO_CODE.put("Ft", "HUF");
        SYMBOL_TO_CODE.put("₽", "RUB");
        SYMBOL_TO_CODE.put("Fr", "CHF");

        CODE_TO_SYMBOL.put("USD", "$");
        CODE_TO_SYMBOL.put("EUR", "€");
        CODE_TO_SYMBOL.put("GBP", "£");
        CODE_TO_SYMBOL.put("JPY", "¥");
        CODE_TO_SYMBOL.put("HUF", "Ft");
        CODE_TO_SYMBOL.put("RUB", "₽");
        CODE_TO_SYMBOL.put("CHF", "Fr");
    }

    public static String getStandardCode(String symbol)
    {
        if (symbol == null)
        {
            return null;
        }

        // Először próbáljuk a szimbólum térképből
        String code = SYMBOL_TO_CODE.get(symbol);
        if (code != null)
        {
            return code;
        }

        try
        {
            return Currency.getInstance(symbol).getCurrencyCode();
        }
        catch (IllegalArgumentException e)
        {
            log.warn("Could not convert currency symbol '{}' to currency code. Returning original value.", symbol);
            return symbol;
        }
    }


    public static String getStandardSymbol(String code)
    {
        if (code == null)
        {
            return null;
        }

        // Először próbáljuk a szimbólum térképből
        String symbol = CODE_TO_SYMBOL.get(code);
        if (symbol != null)
        {
            return symbol;
        }

        try
        {
            return Currency.getInstance(code).getSymbol();
        }
        catch (IllegalArgumentException e)
        {
            log.warn("Could not convert currency code '{}' to currency symbol. Returning original value.", code);
            return code;
        }
    }
}
