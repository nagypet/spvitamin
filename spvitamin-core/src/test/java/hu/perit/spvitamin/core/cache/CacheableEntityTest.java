package hu.perit.spvitamin.core.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CacheableEntityTest
{

    @Test
    void testValidImplementation()
    {
        // Arrange
        CacheableEntity validEntity = new TestCacheableEntity(true);
        CacheableEntity invalidEntity = new TestCacheableEntity(false);

        // Act & Assert
        assertThat(validEntity.isValid()).isTrue();
        assertThat(invalidEntity.isValid()).isFalse();
    }


    // Test implementation of CacheableEntity
    private static class TestCacheableEntity implements CacheableEntity
    {
        private final boolean valid;


        public TestCacheableEntity(boolean valid)
        {
            this.valid = valid;
        }


        @Override
        public boolean isValid()
        {
            return valid;
        }
    }
}
