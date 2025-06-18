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
