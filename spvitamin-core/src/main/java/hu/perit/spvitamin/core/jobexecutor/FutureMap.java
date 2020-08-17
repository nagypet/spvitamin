
/*
 * Copyright 2020-2020 the original author or authors.
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

import lombok.NoArgsConstructor;

import java.util.*;
import java.util.concurrent.Future;

/**
 * @author Peter Nagy
 */

@NoArgsConstructor
public class FutureMap<T> {

    private Map<T, Future> queueItemsMap = new HashMap<>();
    private Map<Future, T> queueItemsMapBackwards = new HashMap<>();

    public synchronized void put(T id, Future future) {
        this.queueItemsMap.put(id, future);
        this.queueItemsMapBackwards.put(future, id);
    }


    public synchronized void remove(T id) {
        if (this.queueItemsMap.containsKey(id)) {
            Future futureToRemove = this.queueItemsMap.get(id);
            this.queueItemsMap.remove(id);
            this.queueItemsMapBackwards.remove(futureToRemove);
        }
    }


    public synchronized boolean contains(T id) {
        return this.queueItemsMap.containsKey(id);
    }


    public synchronized Future get(T id) {
        return this.queueItemsMap.get(id);
    }

    public synchronized T get(Future future) {
        return this.queueItemsMapBackwards.get(future);
    }


    public synchronized int size() {
        return this.queueItemsMap.size();
    }

    public synchronized Set<T> getRunningJobs() {
        return this.queueItemsMap.keySet();
    }
}
