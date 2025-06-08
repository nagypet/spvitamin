package hu.perit.spvitamin.core.cache;

import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ClosableLockTest
{

    @Test
    void testLockAndUnlock()
    {
        // Arrange
        TestLock testLock = new TestLock();

        // Act
        try (ClosableLock closableLock = new ClosableLock(testLock))
        {
            // Assert lock was acquired
            assertThat(testLock.isLocked()).isTrue();
        }

        // Assert lock was released after try-with-resources block
        assertThat(testLock.isLocked()).isFalse();
    }


    @Test
    void testWithNullLock()
    {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            try (ClosableLock closableLock = new ClosableLock(null))
            {
                // No operation
            }
        });
    }


    // Test implementation of Lock that tracks lock state
    private static class TestLock implements Lock
    {
        private boolean locked = false;


        @Override
        public void lock()
        {
            locked = true;
        }


        @Override
        public void unlock()
        {
            locked = false;
        }


        public boolean isLocked()
        {
            return locked;
        }


        // Unused methods from Lock interface
        @Override
        public void lockInterruptibly()
        {
        }


        @Override
        public boolean tryLock()
        {
            return false;
        }


        @Override
        public boolean tryLock(long time, java.util.concurrent.TimeUnit unit)
        {
            return false;
        }


        @Override
        public java.util.concurrent.locks.Condition newCondition()
        {
            return null;
        }
    }
}
