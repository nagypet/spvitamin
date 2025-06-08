package hu.perit.spvitamin.core.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCacheTest
{

    private InMemoryCache<String, TestCacheableEntity> cache;
    private InMemoryCache<String, TestCacheableEntity> limitedCache;


    @BeforeEach
    void setUp()
    {
        cache = new InMemoryCache<>();
        limitedCache = new InMemoryCache<>(3); // Limited to 3 entries
    }


    @Test
    void testGetNonExistentKey()
    {
        // Act
        TestCacheableEntity result = cache.get("nonexistent");

        // Assert
        assertThat(result).isNull();
    }


    @Test
    void testUpdateAndGet()
    {
        // Arrange
        String key = "testKey";
        TestCacheableEntity entity = new TestCacheableEntity("value", true);

        // Act
        TestCacheableEntity updatedEntity = cache.update(key, () -> entity);
        TestCacheableEntity retrievedEntity = cache.get(key);

        // Assert
        assertThat(updatedEntity).isSameAs(entity);
        assertThat(retrievedEntity).isSameAs(entity);
    }


    @Test
    void testUpdateWithInvalidEntity()
    {
        // Arrange
        String key = "testKey";
        TestCacheableEntity invalidEntity = new TestCacheableEntity("invalid", false);
        TestCacheableEntity validEntity = new TestCacheableEntity("valid", true);

        // First update with invalid entity
        cache.update(key, () -> invalidEntity);

        // Act - should call supplier again since entity is invalid
        AtomicInteger supplierCallCount = new AtomicInteger(0);
        TestCacheableEntity result = cache.update(key, () -> {
            supplierCallCount.incrementAndGet();
            return validEntity;
        });

        // Assert
        assertThat(supplierCallCount.get()).isEqualTo(1);
        assertThat(result).isSameAs(validEntity);
        assertThat(cache.get(key)).isSameAs(validEntity);
    }


    @Test
    void testUpdateWithValidEntity()
    {
        // Arrange
        String key = "testKey";
        TestCacheableEntity validEntity = new TestCacheableEntity("valid", true);

        // First update with valid entity
        cache.update(key, () -> validEntity);

        // Act - should not call supplier again since entity is valid
        AtomicInteger supplierCallCount = new AtomicInteger(0);
        TestCacheableEntity result = cache.update(key, () -> {
            supplierCallCount.incrementAndGet();
            return new TestCacheableEntity("new", true);
        });

        // Assert
        assertThat(supplierCallCount.get()).isEqualTo(0);
        assertThat(result).isSameAs(validEntity);
    }


    @Test
    void testRemove()
    {
        // Arrange
        String key = "testKey";
        TestCacheableEntity entity = new TestCacheableEntity("value", true);
        cache.update(key, () -> entity);

        // Act
        cache.remove(key);

        // Assert
        assertThat(cache.get(key)).isNull();
    }


    @Test
    void testSize()
    {
        // Arrange
        cache.update("key1", () -> new TestCacheableEntity("value1", true));
        cache.update("key2", () -> new TestCacheableEntity("value2", true));

        // Act & Assert
        assertThat(cache.size()).isEqualTo(2);

        cache.remove("key1");
        assertThat(cache.size()).isEqualTo(1);

        cache.update("key3", () -> new TestCacheableEntity("value3", true));
        assertThat(cache.size()).isEqualTo(2);
    }


    @Test
    void testMaxCapacity()
    {
        // Arrange & Act
        limitedCache.update("key1", () -> new TestCacheableEntity("value1", true));
        limitedCache.update("key2", () -> new TestCacheableEntity("value2", true));

        // Assert - first two entries should be in the cache
        assertThat(limitedCache.size()).isEqualTo(2);
        assertThat(limitedCache.get("key1")).isNotNull();
        assertThat(limitedCache.get("key2")).isNotNull();

        // Add third entry - this will reach the capacity limit
        limitedCache.update("key3", () -> new TestCacheableEntity("value3", true));

        // Assert - the oldest entry (key1) should be removed when capacity is reached
        assertThat(limitedCache.size()).isEqualTo(2);
        assertThat(limitedCache.get("key1")).isNull();
        assertThat(limitedCache.get("key2")).isNotNull();
        assertThat(limitedCache.get("key3")).isNotNull();

        // Add fourth entry - this will also remove the oldest remaining entry
        limitedCache.update("key4", () -> new TestCacheableEntity("value4", true));

        // Assert - the oldest entry (key2) should be removed
        assertThat(limitedCache.size()).isEqualTo(2);
        assertThat(limitedCache.get("key1")).isNull();
        assertThat(limitedCache.get("key2")).isNull();
        assertThat(limitedCache.get("key3")).isNotNull();
        assertThat(limitedCache.get("key4")).isNotNull();
    }


    @Test
    void testConcurrentAccess() throws InterruptedException
    {
        // Arrange
        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        String key = "concurrentKey";
        AtomicInteger supplierCallCount = new AtomicInteger(0);

        // Act - have multiple threads try to update the same key simultaneously
        for (int i = 0; i < numThreads; i++)
        {
            executorService.submit(() -> {
                try
                {
                    cache.update(key, () -> {
                        // Simulate some work
                        try
                        {
                            Thread.sleep(50);
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                        }
                        supplierCallCount.incrementAndGet();
                        return new TestCacheableEntity("concurrent", true);
                    });
                }
                finally
                {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // Assert - supplier should only be called once due to locking
        assertThat(supplierCallCount.get()).isEqualTo(1);
    }


    // Test implementation of CacheableEntity
    private static class TestCacheableEntity implements CacheableEntity
    {
        private final String value;
        private final boolean valid;


        public TestCacheableEntity(String value, boolean valid)
        {
            this.value = value;
            this.valid = valid;
        }


        @Override
        public boolean isValid()
        {
            return valid;
        }


        @Override
        public String toString()
        {
            return "TestCacheableEntity{value='" + value + "', valid=" + valid + '}';
        }
    }
}
