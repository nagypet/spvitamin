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

package hu.perit.spvitamin.core.softenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class SoftEnumTest
{

    // A simple subclass of SoftEnum for testing
    public static final class TestEnum extends SoftEnum<TestEnum>
    {
        public static final TestEnum ACTIVE = new TestEnum("Active");
        public static final TestEnum BLOCKED = new TestEnum("Blocked");
        public static final TestEnum CLOSED = new TestEnum("Closed");


        private TestEnum(String value)
        {
            super(value, TestEnum.class);
        }


        @JsonCreator
        public static TestEnum fromValue(String value)
        {
            return SoftEnum.fromValue(value, TestEnum.class, TestEnum::new);
        }


        public static Collection<TestEnum> values()
        {
            return SoftEnum.knownValues(TestEnum.class);
        }
    }


    @Test
    void testFromValue()
    {
        // Test with a predefined value
        TestEnum status = TestEnum.fromValue("Active");
        assertThat(status).isSameAs(TestEnum.ACTIVE);

        // Test with a custom value
        TestEnum customStatus = TestEnum.fromValue("Custom");
        assertThat(customStatus).isNotNull();
        assertThat(customStatus.getValue()).isEqualTo("Custom");
        assertThat(customStatus).isNotSameAs(TestEnum.ACTIVE);
        assertThat(customStatus).isNotSameAs(TestEnum.BLOCKED);
        assertThat(customStatus).isNotSameAs(TestEnum.CLOSED);
    }


    @Test
    void testValues()
    {
        // Test that values() returns all predefined values
        Collection<TestEnum> values = TestEnum.values();
        assertThat(values).containsExactlyInAnyOrder(TestEnum.ACTIVE, TestEnum.BLOCKED, TestEnum.CLOSED, TestEnum.fromValue("Custom"));
    }


    @Test
    void testEquals()
    {
        // Test that equals works correctly
        TestEnum active1 = TestEnum.fromValue("Active");
        TestEnum active2 = TestEnum.fromValue("Active");
        TestEnum blocked = TestEnum.fromValue("Blocked");

        assertThat(active1).isSameAs(active2);
        assertThat(active1).isNotEqualTo(blocked);
    }


    @Test
    void testHashCode()
    {
        // Test that hashCode works correctly
        TestEnum active1 = TestEnum.fromValue("Active");
        TestEnum active2 = TestEnum.fromValue("Active");
        TestEnum blocked = TestEnum.fromValue("Blocked");

        assertThat(active1).hasSameHashCodeAs(active2);
        assertThat(active1.hashCode()).isNotEqualTo(blocked.hashCode());
    }


    @Test
    void testToString()
    {
        // Test that toString works correctly
        TestEnum active = TestEnum.fromValue("Active");
        assertThat(active).hasToString("Active");
    }
}
