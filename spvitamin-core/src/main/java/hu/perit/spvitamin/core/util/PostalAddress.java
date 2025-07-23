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

import com.neovisionaries.i18n.CountryCode;
import hu.perit.spvitamin.core.typehelpers.ListUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.List;

@Data
public class PostalAddress
{
    private static final String DEFAULT_REGION_CODE = "hu";

    private String fullAddress;
    private String countryCode;
    private String zip;
    private String city;
    private String address1;
    private String address2;


    public static PostalAddress fromAddressParts(String countryCode, String zip, String city, String address1, String address2)
    {
        PostalAddress address = new PostalAddress();
        address.setCountryCode(getCountryCode(countryCode));
        address.setZip(zip);
        address.setCity(city);
        address.setAddress1(address1);
        address.setAddress2(address2);
        return address;
    }


    private static String getCountryCode(String countryCode)
    {
        if (StringUtils.isBlank(countryCode))
        {
            return null;
        }
        // Is this a valid country code?
        CountryCode cc = CountryCode.getByCode(countryCode);
        if (cc != null)
        {
            return cc.name();
        }

        // Find by country name
        List<CountryCode> byName = CountryCode.findByName(countryCode);
        if (!byName.isEmpty())
        {
            return ListUtils.first(byName).name();
        }

        if (countryCode.length() == 2)
        {
            return countryCode;
        }

        // Region code is unknown
        return null;
    }


    public static PostalAddress fromFullAddress(String fullAddress)
    {
        PostalAddress address = new PostalAddress();
        address.setFullAddress(fullAddress);
        return address;
    }


    /**
     * zip city (regionCode), address1, address2
     *
     * @return
     */
    public String getFullAddress()
    {
        if (this.fullAddress != null)
        {
            return this.fullAddress;
        }

        String regionCodeString = countryCode == null || StringUtils.equalsIgnoreCase(DEFAULT_REGION_CODE, countryCode) ? "" : String.format(" (%s)", countryCode);
        if (address2 == null)
        {
            return MessageFormat.format("{0} {1}{2}, {3}", zip, city, regionCodeString, address1);
        }
        return MessageFormat.format("{0} {1}{2}, {3}, {4}", zip, city, regionCodeString, address1, address2);
    }
}
