
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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Peter Nagy
 */

@NoArgsConstructor
public class FutureMap<T>
{
    public enum Status
    {
        QUEUED,
        RUNNING,
        STOPPING
    }

    private Map<T, FutureHolder> itemsMapById = new ConcurrentHashMap<>();
    private Map<Future<?>, T> itemsMapByFuture = new ConcurrentHashMap<>();

    public synchronized void put(T id, Future<?> future)
    {
        this.itemsMapById.put(id, new FutureHolder(future, Status.QUEUED));
        this.itemsMapByFuture.put(future, id);
    }

    public synchronized void remove(T id)
    {
        if (this.itemsMapById.containsKey(id))
        {
            Future<?> futureToRemove = this.itemsMapById.get(id).getFuture();
            this.itemsMapById.remove(id);
            this.itemsMapByFuture.remove(futureToRemove);
        }
    }


    public synchronized boolean contains(T id)
    {
        return this.itemsMapById.containsKey(id);
    }


    public synchronized Future<?> get(T id)
    {
        if (this.itemsMapById.containsKey(id))
        {
            return this.itemsMapById.get(id).getFuture();
        }

        return null;
    }

    public synchronized T get(Future<?> future)
    {
        return this.itemsMapByFuture.get(future);
    }


    public synchronized int size()
    {
        return this.itemsMapById.size();
    }

    @Deprecated
    public synchronized Set<T> getRunningJobs()
    {
        return this.itemsMapById.keySet();
    }

    public synchronized Set<T> keySet()
    {
        return itemsMapById.keySet();
    }

    public synchronized Status getStatus(T id)
    {
        if (this.itemsMapById.containsKey(id))
        {
            return this.itemsMapById.get(id).getStatus();
        }

        return null;
    }


    public synchronized void setStatus(T id, Status status)
    {
        if (this.itemsMapById.containsKey(id))
        {
            this.itemsMapById.get(id).setStatus(status);
        }
    }

    public synchronized long getCountByStatus(Status status)
    {
        return this.itemsMapById.values().stream().filter(i -> i.getStatus() == status).count();
    }

    @Getter
    @Setter
    public static class FutureHolder
    {
        private final Future<?> future;
        private Status status;

        public FutureHolder(Future<?> future, Status status)
        {
            this.future = future;
            this.status = status;
        }
    }
}
