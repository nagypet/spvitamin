/*
 * Copyright 2020-2026 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class Currency extends AbstractStringValue<Currency>
{
    public static final Currency HUF = Currency.fromString("HUF");
    public static final Currency USD = Currency.fromString("USD");


    protected Currency(String value)
    {
        super(validate(value));
    }


    public static Currency fromString(String value)
    {
        return new Currency(value);
    }


    public static String get(Currency currency)
    {
        return currency != null ? currency.value : null;
    }


    public boolean isValid()
    {
        return isValid(this.value);
    }


    @JsonIgnore
    public static boolean isValid(String value)
    {
        return StringUtils.isNotBlank(validate(value));
    }


    // returns a valid currency code (uppercase) or null
    private static String validate(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return null;
        }

        try
        {
            java.util.Currency currency = java.util.Currency.getInstance(Case.toUpper(value));
            return currency != null ? currency.getCurrencyCode() : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
