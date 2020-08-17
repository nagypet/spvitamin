/*
 * Copyright 2020-2020 the original author or authors.
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

package hu.perit.spvitamin.spring.keystore;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Peter Nagy
 */

@Getter
@Setter
public class CertInfo
{
    private String issuer;
    private String subject;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    //CN=micsignprod.kozpont.otp, OU=IBF, O=OTP Bank, L=Budapest, ST=Budapest, C=HU
    public String getIssuerCN()
    {
        return this.getCN(this.issuer);
    }

    public String getSubjectCN()
    {
        return this.getCN(this.subject);
    }

    private String getCN(String dn)
    {
        String[] names = dn.split(",");
        if (names != null)
        {
            for (String name : names)
            {
                String[] elements = name.split("=");
                if (elements != null && elements.length == 2 && elements[0].equalsIgnoreCase("CN"))
                {
                    return elements[1];
                }
            }
        }
        return "";
    }

    public boolean isValid()
    {
        if (this.validFrom == null || this.validTo == null) return false;

        LocalDateTime actualTimastamp = LocalDateTime.now();
        return (this.validFrom.isBefore(actualTimastamp) && this.validTo.isAfter(actualTimastamp));
    }
}
