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

class PersonNameTest
{
    @Test
    void testFromWithNull()
    {
        PersonName personName = PersonName.from(null);

        assertThat(personName).isNotNull();
        assertThat(personName.getGivenName()).isNull();
        assertThat(personName.getAdditionalGivenNames()).isNull();
        assertThat(personName.getFamilyName()).isNull();
        assertThat(personName.getName()).isEmpty();
    }


    @Test
    void testFromWithEmptyString()
    {
        PersonName personName = PersonName.from("");

        assertThat(personName).isNotNull();
        assertThat(personName.getGivenName()).isNull();
        assertThat(personName.getAdditionalGivenNames()).isNull();
        assertThat(personName.getFamilyName()).isNull();
        assertThat(personName.getName()).isEmpty();
    }


    @Test
    void testFromWithSingleName()
    {
        PersonName personName = PersonName.from("John");

        assertThat(personName).isNotNull();
        assertThat(personName.getGivenName()).isEqualTo("John");
        assertThat(personName.getAdditionalGivenNames()).isNull();
        assertThat(personName.getFamilyName()).isNull();
        assertThat(personName.getName()).isEqualTo("John");
    }


    @Test
    void testFromWithTwoNames()
    {
        PersonName personName = PersonName.from("John Doe");

        assertThat(personName).isNotNull();
        assertThat(personName.getGivenName()).isEqualTo("John");
        assertThat(personName.getAdditionalGivenNames()).isNull();
        assertThat(personName.getFamilyName()).isEqualTo("Doe");
        assertThat(personName.getName()).isEqualTo("John Doe");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("Doe, John");
    }


    @Test
    void testFromWithThreeNames()
    {
        PersonName personName = PersonName.from("John Middle Doe");

        assertThat(personName).isNotNull();
        assertThat(personName.getGivenName()).isEqualTo("John");
        assertThat(personName.getFamilyName()).isEqualTo("Doe");
        assertThat(personName.getAdditionalGivenNames()).isEqualTo("Middle");
        assertThat(personName.getName()).isEqualTo("John Middle Doe");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("Doe, John Middle");
    }


    @Test
    void testFromWithMultipleMiddleNames()
    {
        PersonName personName = PersonName.from("John Middle1 Middle2 Doe");

        assertThat(personName).isNotNull();
        assertThat(personName.getGivenName()).isEqualTo("John");
        assertThat(personName.getFamilyName()).isEqualTo("Doe");
        assertThat(personName.getAdditionalGivenNames()).isEqualTo("Middle1 Middle2");
        assertThat(personName.getName()).isEqualTo("John Middle1 Middle2 Doe");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("Doe, John Middle1 Middle2");
    }


    @Test
    void testFromWithExtraSpaces()
    {
        PersonName personName = PersonName.from("  John   Middle   Doe  ");

        assertThat(personName).isNotNull();
        assertThat(personName.getGivenName()).isEqualTo("John");
        assertThat(personName.getFamilyName()).isEqualTo("Doe");
        assertThat(personName.getAdditionalGivenNames()).isEqualTo("Middle");
        assertThat(personName.getName()).isEqualTo("John Middle Doe");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("Doe, John Middle");
    }


    @Test
    void testFromWithSimpleHungarianNameEasternOrder()
    {
        PersonName personName = PersonName.from("Nagy János", PersonName.NameOrder.EASTERN);

        assertThat(personName).isNotNull();
        assertThat(personName.getFamilyName()).isEqualTo("Nagy");
        assertThat(personName.getGivenName()).isEqualTo("János");
        assertThat(personName.getAdditionalGivenNames()).isNull();
        assertThat(personName.getName()).isEqualTo("Nagy János");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("János Nagy");
    }


    @Test
    void testFromWithCompoundHungarianFamilyNameEasternOrder()
    {
        PersonName personName = PersonName.from("Kovács-Nagy János", PersonName.NameOrder.EASTERN);

        assertThat(personName).isNotNull();
        assertThat(personName.getFamilyName()).isEqualTo("Kovács-Nagy");
        assertThat(personName.getGivenName()).isEqualTo("János");
        assertThat(personName.getAdditionalGivenNames()).isNull();
        assertThat(personName.getName()).isEqualTo("Kovács-Nagy János");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("János Kovács-Nagy");
    }


    @Test
    void testFromWithHungarianNameWithMiddleNameEasternOrder()
    {
        PersonName personName = PersonName.from("Nagy Béla János", PersonName.NameOrder.EASTERN);

        assertThat(personName).isNotNull();
        assertThat(personName.getFamilyName()).isEqualTo("Nagy");
        assertThat(personName.getGivenName()).isEqualTo("Béla");
        assertThat(personName.getAdditionalGivenNames()).isEqualTo("János");
        assertThat(personName.getName()).isEqualTo("Nagy Béla János");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("Béla János Nagy");
    }


    @Test
    void testFromWithComplexHungarianNameEasternOrder()
    {
        PersonName personName = PersonName.from("Kovács-Nagy Béla János", PersonName.NameOrder.EASTERN);

        assertThat(personName).isNotNull();
        assertThat(personName.getFamilyName()).isEqualTo("Kovács-Nagy");
        assertThat(personName.getGivenName()).isEqualTo("Béla");
        assertThat(personName.getAdditionalGivenNames()).isEqualTo("János");
        assertThat(personName.getName()).isEqualTo("Kovács-Nagy Béla János");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("Béla János Kovács-Nagy");
    }


    @Test
    void testGetNameWithWesternOrder()
    {
        PersonName personName = new PersonName(PersonName.NameOrder.WESTERN);
        personName.setGivenName("John");
        personName.setAdditionalGivenNames("Middle");
        personName.setFamilyName("Doe");

        assertThat(personName.getName()).isEqualTo("John Middle Doe");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("John Middle Doe");
    }


    @Test
    void testGetNameWithEasternOrder()
    {
        PersonName personName = new PersonName(PersonName.NameOrder.WESTERN);
        personName.setGivenName("John");
        personName.setAdditionalGivenNames("Middle");
        personName.setFamilyName("Doe");

        assertThat(personName.getName()).isEqualTo("John Middle Doe");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("Doe, John Middle");
    }


    @Test
    void testGetNameWithOnlyGivenName()
    {
        PersonName personName = new PersonName(PersonName.NameOrder.WESTERN);
        personName.setGivenName("John");

        assertThat(personName.getName()).isEqualTo("John");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("John");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("John");
    }


    @Test
    void testGetNameWithOnlyFamilyName()
    {
        PersonName personName = new PersonName(PersonName.NameOrder.WESTERN);
        personName.setFamilyName("Doe");

        assertThat(personName.getName()).isEqualTo("Doe");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("Doe");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("Doe");
    }


    @Test
    void testGetNameWithGivenNameAndFamilyNameWesternOrder()
    {
        PersonName personName = new PersonName(PersonName.NameOrder.WESTERN);
        personName.setGivenName("John");
        personName.setFamilyName("Doe");

        assertThat(personName.getName()).isEqualTo("John Doe");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("John Doe");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("Doe, John");
    }


    @Test
    void testGetNameWithGivenNameAndFamilyNameEasternOrder()
    {
        PersonName personName = new PersonName(PersonName.NameOrder.EASTERN);
        personName.setFamilyName("Kovács");
        personName.setGivenName("Béla");

        assertThat(personName.getName()).isEqualTo("Kovács Béla");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("Béla Kovács");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("Kovács Béla");
    }


    @Test
    void testGetNameWithGivenNameAndAdditionalGivenNames()
    {
        PersonName personName = new PersonName(PersonName.NameOrder.WESTERN);
        personName.setGivenName("John");
        personName.setAdditionalGivenNames("Middle");

        assertThat(personName.getName()).isEqualTo("John Middle");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("John Middle");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("John Middle");
    }


    @Test
    void testGetNameWithHungarianName()
    {
        PersonName personName = PersonName.from("Kovács-Nagy Béla János", PersonName.NameOrder.EASTERN);

        assertThat(personName.getName()).isEqualTo("Kovács-Nagy Béla János");
        assertThat(personName.getName(PersonName.NameOrder.WESTERN)).isEqualTo("Béla János Kovács-Nagy");
        assertThat(personName.getName(PersonName.NameOrder.EASTERN)).isEqualTo("Kovács-Nagy Béla János");
    }
}
