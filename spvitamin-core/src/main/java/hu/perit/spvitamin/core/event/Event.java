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

package hu.perit.spvitamin.core.event;

import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 *  This is an implementation of the observer pattern. The class holds a strong reference to the Consumer subscribed
 *  to changes, preventing subscribers to be garbage collected. Please make sure to close the returned Subscription object
 *  to prevent memory leaks.
 *
 * @author Peter Nagy
 */

@Slf4j
public class Event<T>
{

    private final AtomicInteger lastId = new AtomicInteger(0);
    private final Map<Integer, Consumer<T>> subscribers = new HashMap<>();

    public Subscription subscribe(Consumer<T> subscriber)
    {
        Subscription subscription = new Subscription(this, this.lastId.incrementAndGet());
        synchronized (this)
        {
            this.subscribers.put(subscription.id, subscriber);
        }
        return subscription;
    }


    public synchronized void fire(T args)
    {
        List<Exception> exceptionList = new ArrayList<>();
        for (Consumer<T> subscriber : this.subscribers.values())
        {
            try
            {
                subscriber.accept(args);
            }
            catch (Exception ex)
            {
                exceptionList.add(ex);
                log.error(StackTracer.toString(ex));
            }
        }
        if (!exceptionList.isEmpty())
        {
            throw new EventFireException(exceptionList);
        }
    }


    public class Subscription implements AutoCloseable
    {

        private Event<T> event;
        private int id;

        public Subscription(Event<T> event, int id)
        {
            this.event = event;
            this.id = id;
        }

        @Override
        public void close() /*throws Exception*/
        {
            event.subscribers.remove(this.id);
        }
    }
}
