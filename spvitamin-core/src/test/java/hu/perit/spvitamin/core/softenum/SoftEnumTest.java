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
