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

class PostalAddressTest
{
    @Test
    void testWithCorrectRegionCode()
    {
        assertThat(PostalAddress.fromAddressParts(null, "1012", "Budapest", "Árnyas u. 12", null).getFullAddress())
                .isEqualTo("1012 Budapest, Árnyas u. 12");
        assertThat(PostalAddress.fromAddressParts(null, "1012", "Budapest", "Árnyas u. 12", "2. em. 3.").getFullAddress())
                .isEqualTo("1012 Budapest, Árnyas u. 12, 2. em. 3.");

        assertThat(PostalAddress.fromAddressParts("NY", "10001", "New York", "123 Main Street", null).getFullAddress())
                .isEqualTo("10001 New York (NY), 123 Main Street");
        assertThat(PostalAddress.fromAddressParts("NY", "10001", "New York", "123 Main Street", "Apartment 4B").getFullAddress())
                .isEqualTo("10001 New York (NY), 123 Main Street, Apartment 4B");

        assertThat(PostalAddress.fromAddressParts("DE", "80333", "München", "Musterstraße 12", null).getFullAddress())
                .isEqualTo("80333 München (DE), Musterstraße 12");
        assertThat(PostalAddress.fromAddressParts("DE", "80333", "München", "Musterstraße 12", "2. Stock").getFullAddress())
                .isEqualTo("80333 München (DE), Musterstraße 12, 2. Stock");
    }


    @Test
    void testWithIncorrectRegionCode()
    {
        assertThat(PostalAddress.fromAddressParts("Hungary", "1012", "Budapest", "Árnyas u. 12", null).getFullAddress())
                .isEqualTo("1012 Budapest, Árnyas u. 12");

        assertThat(PostalAddress.fromAddressParts("Germany", "80333", "München", "Musterstraße 12", "2. Stock").getFullAddress())
                .isEqualTo("80333 München (DE), Musterstraße 12, 2. Stock");

        assertThat(PostalAddress.fromAddressParts("Bergengócia", "80333", "München", "Musterstraße 12", "2. Stock").getFullAddress())
                .isEqualTo("80333 München, Musterstraße 12, 2. Stock");
    }
}
