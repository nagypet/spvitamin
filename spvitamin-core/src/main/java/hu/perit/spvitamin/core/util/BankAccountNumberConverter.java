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

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

@UtilityClass
public class BankAccountNumberConverter
{
    public String toIbanString(String accountNumber)
    {
        return toIban(accountNumber).value();
    }


    // Returns the Iban for any supported account number format (IBAN or GIRO)
    // Hungarian IBAN: 28 characters long (HU + 2 check digits + 24-digit BBAN)
    // 16-digit account number: append 00000000 (8 zeros) to get a 24-digit BBAN
    // 24-digit account number: already the full BBAN
    // Check digit calculation: BBAN + HU00 → convert each letter to a number (A=10..Z=35) → mod 97 → 98 - result
    // Does not validate whether IBAN or GIRO account numbers are valid. Use isValidAccountNum instead.
    public Iban toIban(String accountNumber)
    {
        if (StringUtils.isBlank(accountNumber))
        {
            return Iban.fromString(null);
        }

        // GIRO format: 16 or 24 digits?
        if (GiroAccountNum.isValidFormat(accountNumber))
        {
            return Iban.fromGiro(GiroAccountNum.fromString(accountNumber));
        }

        // IBAN format?
        if (Iban.isValidFormat(accountNumber))
        {
            return Iban.fromString(accountNumber);
        }

        throw new IllegalArgumentException("Unsupported account number format: " + accountNumber);
    }


    // Returns the GiroAccountNum for any supported account number format (IBAN or GIRO)
    public GiroAccountNum toGiro(String accountNumber)
    {
        if (StringUtils.isBlank(accountNumber))
        {
            return GiroAccountNum.fromString(null);
        }

        String cleaned = accountNumber.replaceAll("[\\s\\-]", "");

        // GIRO format: 16 or 24 digits
        if (GiroAccountNum.isValidFormat(cleaned))
        {
            return GiroAccountNum.fromString(cleaned);
        }

        // IBAN format
        return Iban.fromString(cleaned).toGiro();
    }


    public boolean isValidAccountNum(String accountNum)
    {
        try
        {
            String ibanStr = toIbanString(accountNum);
            Iban iban = Iban.fromString(ibanStr);
            if (!iban.isValid())
            {
                return false;
            }
            return !Strings.CI.startsWith(ibanStr, "HU")
                    || iban.toGiro().isValid();
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }
}
