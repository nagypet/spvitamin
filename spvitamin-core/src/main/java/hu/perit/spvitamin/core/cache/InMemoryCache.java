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

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * A thread-safe in-memory cache implementation for storing and retrieving objects that implement the CacheableEntity interface.
 * 
 * <p>This cache provides the following features:
 * <ul>
 *   <li>Thread-safe operations using locks to ensure data consistency during concurrent access</li>
 *   <li>Automatic validation of cached entities using the {@link CacheableEntity#isValid()} method</li>
 *   <li>Lazy loading of values through supplier functions</li>
 *   <li>Optional maximum capacity with LRU (Least Recently Used) eviction policy</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 * // Create an unlimited cache
 * InMemoryCache<String, MyEntity> cache = new InMemoryCache<>();
 * 
 * // Create a cache with maximum capacity of 100 entries
 * InMemoryCache<String, MyEntity> limitedCache = new InMemoryCache<>(100);
 * 
 * // Get a value (returns null if not found)
 * MyEntity entity = cache.get("myKey");
 * 
 * // Update or add a value using a supplier function
 * MyEntity updatedEntity = cache.update("myKey", () -> {
 *     // This supplier function is only called if:
 *     // 1. The key doesn't exist in the cache, or
 *     // 2. The existing entity's isValid() method returns false
 *     return new MyEntity();
 * });
 * 
 * // Remove a value
 * cache.remove("myKey");
 * 
 * // Get the current size of the cache
 * int size = cache.size();
 * </pre>
 * 
 * <p>Note: All values stored in this cache must implement the {@link CacheableEntity} interface,
 * which provides the {@code isValid()} method to determine if a cached entity is still valid or needs to be refreshed.
 * 
 * @param <K> The type of keys maintained by this cache
 * @param <V> The type of cached values, must implement {@link CacheableEntity}
 */
@Slf4j
public class InMemoryCache<K, V extends CacheableEntity>
{
    private final Map<K, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final Map<K, V> values;


    public InMemoryCache()
    {
        this.values = Collections.synchronizedMap(new LinkedHashMap<>());
    }


    public InMemoryCache(Integer maxCapacity)
    {
        this.values = Collections.synchronizedMap(new LinkedHashMap<>()
        {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
            {
                boolean toRemove = this.size() >= maxCapacity;
                if (toRemove)
                {
                    log.debug("Max capacity reached, removing {}", eldest.getKey());
                }
                return toRemove;
            }
        });
    }


    public V get(K key)
    {
        try (var l = new ClosableLock(this.locks.get(key)))
        {
            return this.values.get(key);
        }
    }


    public V update(K key, Supplier<V> supplier)
    {
        // Create a new lock for the current entry, to block subsequent get() operations during the update.
        ReentrantLock lock = this.locks.computeIfAbsent(key, k -> new ReentrantLock());
        try (var l = new ClosableLock(lock))
        {
            // This check is necessary when more than one thread are waiting on this lock, to avoid calling the supplier more than ones. The first thread
            // calls the supplier function and puts the refreshed value into the map. The next one gets the value from the map, and finds - most probably -
            // that this is already valid.
            V cachedValue = this.values.get(key);
            if (isEntityValid(cachedValue))
            {
                return cachedValue;
            }

            log.info("--> Calling supplier function for {} ...", key);
            V value = supplier.get();
            log.info("<-- value supplied: {}", value);
            this.values.put(key, value);
            return value;
        }
        finally
        {
            this.locks.remove(key);
        }
    }


    public void remove(K key)
    {
        // Create a new lock for the current entry, to block subsequent get() and update() operations.
        ReentrantLock lock = this.locks.computeIfAbsent(key, k -> new ReentrantLock());

        try (var l = new ClosableLock(lock))
        {
            this.values.remove(key);
        }
        finally
        {
            this.locks.remove(key);
        }
    }


    public int size()
    {
        return this.values.size();
    }


    private boolean isEntityValid(V value)
    {
        if (value == null)
        {
            return false;
        }

        return value.isValid();
    }
}
