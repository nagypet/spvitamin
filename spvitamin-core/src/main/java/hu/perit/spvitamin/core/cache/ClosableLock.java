/*
 * Copyright 2020-2023 the original author or authors.
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

import java.util.concurrent.locks.Lock;

public class ClosableLock implements AutoCloseable
{
    private final Lock lock;


    public ClosableLock(Lock lock)
    {
        this.lock = lock;
        if (lock != null)
        {
            lock.lock(); // NOSONAR: Sonar think this lock should be unlocked, but this is done in the close method. :-)
        }
    }


    @Override
    public void close()
    {
        if (this.lock != null)
        {
            this.lock.unlock();
        }
    }
}
