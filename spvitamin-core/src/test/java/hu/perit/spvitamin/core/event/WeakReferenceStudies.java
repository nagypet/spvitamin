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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
class WeakReferenceStudies
{
    private static class Subscriber
    {
        Consumer<String> myEventHandler = this::eventHandler;

        void eventHandler(String eventParam)
        {
            log.debug(String.format("%s event handler called with parameter '%s'", this, eventParam));
        }
    }


    @Test
    void testWeakReferenceToObject()
    {
        Object object1 = new Object();
        Object object2 = new Object();

        WeakReference<Object> weakReference1 = new WeakReference<>(object1);
        WeakReference<Object> weakReference2 = new WeakReference<>(object2);

        log.debug(String.format("object1 is available: %b", weakReference1.get() != null));
        log.debug(String.format("object2 is available: %b", weakReference2.get() != null));

        object2 = null;

        log.debug("Calling System.gc()");
        System.gc();

        log.debug(String.format("object1 is available: %b", weakReference1.get() != null));
        log.debug(String.format("object2 is available: %b", weakReference2.get() != null));
    }


    private class SubscriberRef
    {
        private final WeakReference<Consumer<String>> consumer;

        SubscriberRef(Consumer<String> consumer)
        {
            this.consumer = new WeakReference<>(consumer);
        }
    }

    @Test
    void testWeakReferenceToConsumer1()
    {
        Subscriber subscriber1 = new Subscriber();
        Subscriber subscriber2 = new Subscriber();

        Map<Integer, SubscriberRef> map = new HashMap<>();

        map.put(1, new SubscriberRef(subscriber1::eventHandler));
        map.put(2, new SubscriberRef(subscriber2::eventHandler));

        for (Map.Entry<Integer, SubscriberRef> entry : map.entrySet())
        {
            log.debug(String.format("consumer %d is available: %b", entry.getKey(), entry.getValue().consumer.get() != null));
        }

        subscriber2 = null;

        log.debug("Calling System.gc()");
        System.gc();

        for (Map.Entry<Integer, SubscriberRef> entry : map.entrySet())
        {
            log.debug(String.format("consumer %d is available: %b", entry.getKey(), entry.getValue().consumer.get() != null));
        }
    }


    @Test
    void testWeakReferenceToConsumer2()
    {
        Subscriber subscriber1 = new Subscriber();
        Subscriber subscriber2 = new Subscriber();

        Map<Integer, SubscriberRef> map = new HashMap<>();

        map.put(1, new SubscriberRef(subscriber1.myEventHandler));
        map.put(2, new SubscriberRef(subscriber2.myEventHandler));

        for (Map.Entry<Integer, SubscriberRef> entry : map.entrySet())
        {
            log.debug(String.format("consumer %d is available: %b", entry.getKey(), entry.getValue().consumer.get() != null));
        }

        subscriber2 = null;

        log.debug("Calling System.gc()");
        System.gc();

        for (Map.Entry<Integer, SubscriberRef> entry : map.entrySet())
        {
            log.debug(String.format("consumer %d is available: %b", entry.getKey(), entry.getValue().consumer.get() != null));
        }
    }
}
