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

package hu.perit.spvitamin.core.jobexecutor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static org.assertj.core.api.Assertions.assertThat;

class FutureMapTest {

    private FutureMap<String> futureMap;
    private Future<Void> future1;
    private Future<Void> future2;
    private Future<Void> future3;

    @BeforeEach
    void setUp() {
        futureMap = new FutureMap<>();
        
        // Create some test futures
        Callable<Void> noOpCallable = () -> null;
        future1 = new FutureTask<>(noOpCallable);
        future2 = new FutureTask<>(noOpCallable);
        future3 = new FutureTask<>(noOpCallable);
    }

    @Test
    void testPutAndGet() {
        // Act
        futureMap.put("job1", future1);
        
        // Assert
        assertThat(futureMap.get("job1")).isSameAs(future1);
        assertThat(futureMap.get(future1)).isEqualTo("job1");
    }

    @Test
    void testGetNonExistentId() {
        // Act & Assert
        assertThat(futureMap.get("nonExistentJob")).isNull();
    }

    @Test
    void testGetNonExistentFuture() {
        // Act & Assert
        assertThat(futureMap.get(future1)).isNull();
    }

    @Test
    void testContains() {
        // Arrange
        futureMap.put("job1", future1);
        
        // Act & Assert
        assertThat(futureMap.contains("job1")).isTrue();
        assertThat(futureMap.contains("nonExistentJob")).isFalse();
    }

    @Test
    void testRemove() {
        // Arrange
        futureMap.put("job1", future1);
        
        // Act
        futureMap.remove("job1");
        
        // Assert
        assertThat(futureMap.contains("job1")).isFalse();
        assertThat(futureMap.get("job1")).isNull();
        assertThat(futureMap.get(future1)).isNull();
    }

    @Test
    void testRemoveNonExistentId() {
        // Act
        futureMap.remove("nonExistentJob");
        
        // Assert - should not throw an exception
        assertThat(futureMap.size()).isEqualTo(0);
    }

    @Test
    void testSize() {
        // Arrange
        futureMap.put("job1", future1);
        futureMap.put("job2", future2);
        
        // Act & Assert
        assertThat(futureMap.size()).isEqualTo(2);
        
        futureMap.remove("job1");
        assertThat(futureMap.size()).isEqualTo(1);
        
        futureMap.remove("job2");
        assertThat(futureMap.size()).isEqualTo(0);
    }

    @Test
    void testKeySet() {
        // Arrange
        futureMap.put("job1", future1);
        futureMap.put("job2", future2);
        
        // Act & Assert
        assertThat(futureMap.keySet()).containsExactlyInAnyOrder("job1", "job2");
    }

    @Test
    void testGetRunningJobs() {
        // Arrange
        futureMap.put("job1", future1);
        futureMap.put("job2", future2);
        
        // Act & Assert
        assertThat(futureMap.getRunningJobs()).containsExactlyInAnyOrder("job1", "job2");
    }

    @Test
    void testGetStatus() {
        // Arrange
        futureMap.put("job1", future1);
        
        // Act & Assert
        assertThat(futureMap.getStatus("job1")).isEqualTo(FutureMap.Status.QUEUED); // Default status is QUEUED
        assertThat(futureMap.getStatus("nonExistentJob")).isNull();
    }

    @Test
    void testSetStatus() {
        // Arrange
        futureMap.put("job1", future1);
        
        // Act
        futureMap.setStatus("job1", FutureMap.Status.RUNNING);
        
        // Assert
        assertThat(futureMap.getStatus("job1")).isEqualTo(FutureMap.Status.RUNNING);
    }

    @Test
    void testSetStatusNonExistentId() {
        // Act
        futureMap.setStatus("nonExistentJob", FutureMap.Status.RUNNING);
        
        // Assert - should not throw an exception
        assertThat(futureMap.size()).isEqualTo(0);
    }

    @Test
    void testGetCountByStatus() {
        // Arrange
        futureMap.put("job1", future1);
        futureMap.put("job2", future2);
        futureMap.put("job3", future3);
        
        // All jobs start with QUEUED status
        assertThat(futureMap.getCountByStatus(FutureMap.Status.QUEUED)).isEqualTo(3);
        assertThat(futureMap.getCountByStatus(FutureMap.Status.RUNNING)).isEqualTo(0);
        assertThat(futureMap.getCountByStatus(FutureMap.Status.STOPPING)).isEqualTo(0);
        
        // Act
        futureMap.setStatus("job1", FutureMap.Status.RUNNING);
        futureMap.setStatus("job2", FutureMap.Status.STOPPING);
        
        // Assert
        assertThat(futureMap.getCountByStatus(FutureMap.Status.QUEUED)).isEqualTo(1);
        assertThat(futureMap.getCountByStatus(FutureMap.Status.RUNNING)).isEqualTo(1);
        assertThat(futureMap.getCountByStatus(FutureMap.Status.STOPPING)).isEqualTo(1);
    }

    @Test
    void testFutureHolder() {
        // Arrange
        FutureMap.FutureHolder holder = new FutureMap.FutureHolder(future1, FutureMap.Status.QUEUED);
        
        // Act & Assert
        assertThat(holder.getFuture()).isSameAs(future1);
        assertThat(holder.getStatus()).isEqualTo(FutureMap.Status.QUEUED);
        
        // Test setter
        holder.setStatus(FutureMap.Status.RUNNING);
        assertThat(holder.getStatus()).isEqualTo(FutureMap.Status.RUNNING);
    }
}
