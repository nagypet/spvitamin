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

package hu.perit.spvitamin.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NpCollections} class
 */
class NpCollectionsTest
{

    @Test
    void isArrayListEmpty_withNullList_returnsTrue()
    {
        // Arrange
        List<String> nullList = null;

        // Act
        boolean result = NpCollections.isArrayListEmpty(nullList);

        // Assert
        assertThat(result).isTrue();
    }


    @Test
    void isArrayListEmpty_withEmptyList_returnsTrue()
    {
        // Arrange
        List<String> emptyList = new ArrayList<>();

        // Act
        boolean result = NpCollections.isArrayListEmpty(emptyList);

        // Assert
        assertThat(result).isTrue();
    }


    @Test
    void isArrayListEmpty_withListContainingNullElement_returnsTrue()
    {
        // Arrange
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add(null);

        // Act
        boolean result = NpCollections.isArrayListEmpty(listWithNull);

        // Assert
        assertThat(result).isTrue();
    }


    @Test
    void isArrayListEmpty_withListContainingEmptyString_returnsTrue()
    {
        // Arrange
        List<String> listWithEmptyString = new ArrayList<>();
        listWithEmptyString.add("");

        // Act
        boolean result = NpCollections.isArrayListEmpty(listWithEmptyString);

        // Assert
        assertThat(result).isTrue();
    }


    @Test
    void isArrayListEmpty_withListContainingNonEmptyString_returnsFalse()
    {
        // Arrange
        List<String> listWithString = new ArrayList<>();
        listWithString.add("test");

        // Act
        boolean result = NpCollections.isArrayListEmpty(listWithString);

        // Assert
        assertThat(result).isFalse();
    }


    @Test
    void isArrayListEmpty_withListContainingMultipleStrings_returnsFalse()
    {
        // Arrange
        List<String> listWithMultipleStrings = Arrays.asList("test1", "test2");

        // Act
        boolean result = NpCollections.isArrayListEmpty(listWithMultipleStrings);

        // Assert
        assertThat(result).isFalse();
    }


    @Test
    void isArrayListEmpty_withListContainingEmptyStringAndNonEmptyString_returnsFalse()
    {
        // Arrange
        List<String> mixedList = Arrays.asList("", "test");

        // Act
        boolean result = NpCollections.isArrayListEmpty(mixedList);

        // Assert
        assertThat(result).isFalse();
    }


    @Test
    void newArrayListFromString_withNullString_returnsEmptyList()
    {
        // Arrange
        String nullString = null;

        // Act
        List<String> result = NpCollections.newArrayListFromString(nullString);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }


    @Test
    void newArrayListFromString_withEmptyString_returnsEmptyList()
    {
        // Arrange
        String emptyString = "";

        // Act
        List<String> result = NpCollections.newArrayListFromString(emptyString);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }


    @Test
    void newArrayListFromString_withNonEmptyString_returnsListWithString()
    {
        // Arrange
        String testString = "test";

        // Act
        List<String> result = NpCollections.newArrayListFromString(testString);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testString);
    }
}
