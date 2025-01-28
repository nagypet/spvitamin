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

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This is an implementation of the observer pattern. Subscribers will be referenced by WeakReference to avoid
 * memory leak. When using this class the object where the listener is defined must keep a strong reference to
 * the method provided as a Consumer with the subscribe method.
 *
 * private static final EventWithWeakRef<String> EVENT = new EventWithWeakRef<>();
 *
 * private static class Subscriber
 * {
 * 	   // This property holds a strong reference to the listener to keep it in memory as long the Subscriber object exists.
 *     private Consumer<String> myEventHandler = this::eventHandler;
 *
 *     Subscriber()
 *     {
 *         EVENT.subscribe(this.myEventHandler);
 *     }
 *
 *     void eventHandler(String eventParam)
 *     {
 *         log.debug(String.format("%s event handler called with parameter '%s'", this, eventParam));
 *     }
 * }
 *
 * @author Peter Nagy
 */

@Slf4j
public class EventWithWeakRef<T>
{

    private final AtomicInteger lastId = new AtomicInteger(0);
    private final Map<Integer, ConsumerWeakRef<T>> subscribers = new HashMap<>();

    public Subscription subscribe(Consumer<T> subscriber)
    {
        Subscription subscription = new Subscription(this, this.lastId.incrementAndGet());
        synchronized (this)
        {
            this.subscribers.put(subscription.id, new ConsumerWeakRef<>(subscriber));
        }
        return subscription;
    }


    public synchronized void fire(T args)
    {
        List<Exception> exceptionList = new ArrayList<>();
        Iterator<Entry<Integer, ConsumerWeakRef<T>>> iterator = this.subscribers.entrySet().iterator();
        while (iterator.hasNext())
        {
            try
            {
                Entry<Integer, ConsumerWeakRef<T>> entry = iterator.next();
                if (!entry.getValue().isGarbageCollected())
                {
                    entry.getValue().accept(args);
                }
                else
                {
                    // Subscriber has been garbage collected
                    iterator.remove();
                }
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


    //------------------------------------------------------------------------------------------------------------------
    // Subscription
    //------------------------------------------------------------------------------------------------------------------
    public class Subscription implements AutoCloseable
    {

        private EventWithWeakRef<T> event;
        private int id;

        public Subscription(EventWithWeakRef<T> event, int id)
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


    //------------------------------------------------------------------------------------------------------------------
    // ConsumerWeakRef
    //------------------------------------------------------------------------------------------------------------------
    protected class ConsumerWeakRef<T> implements Consumer<T>
    {
        private final WeakReference<Consumer<T>> consumer;

        public ConsumerWeakRef(Consumer<T> consumer)
        {
            this.consumer = new WeakReference<>(consumer);
        }

        public boolean isGarbageCollected()
        {
            return this.consumer.get() == null;
        }

        @Override
        public void accept(T t)
        {
            if (this.consumer.get() != null)
            {
                this.consumer.get().accept(t);
            }
        }
    }
}
