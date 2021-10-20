/*
 * Copyright 2020-2021 the original author or authors.
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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import hu.perit.spvitamin.core.StackTracer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@Slf4j
public class Event2<T>
{

	private final AtomicInteger lastId = new AtomicInteger(0);
	private final Map<Integer, Reference<T>> subscribers = new HashMap<>();

	public Subscription subscribe(Consumer<T> subscriber)
	{
		Subscription subscription = new Subscription(this.lastId.incrementAndGet());
		synchronized (this)
		{
			this.subscribers.put(subscription.id, new Reference<>(subscriber));
		}
		return subscription;
	}


	public synchronized void fire(T args)
	{
		try
		{
			List<Integer> gcdSubscribers = new ArrayList<>();

			for (Entry<Integer, Reference<T>> entry : this.subscribers.entrySet())
			{
				if (!entry.getValue().accept(args))
				{
					// The subscriber has been GCd
					gcdSubscribers.add(entry.getKey());
				}
			}

			// Remove stale subscriptions
			if (!gcdSubscribers.isEmpty())
			{
				synchronized (this)
				{
					for (Integer id : gcdSubscribers)
					{
						log.debug(String.format("Removing subscription %d", id));
						this.subscribers.remove(id);
					}
				}
			}
		}
		catch (Exception ex)
		{
			log.error(StackTracer.toString(ex));
		}
	}

	// ------------------------------------------------------------------------------------------------------------------
	// Reference
	// ------------------------------------------------------------------------------------------------------------------

	@Getter
	static class Reference<T> extends WeakReference<Object>
	{
		Consumer<T> consumer;
		// WeakReference<Object> targetRef;
		// Method actionMethod;

		public Reference(Consumer<T> consumer)
		{
			super(getTarget(consumer));
			this.consumer = consumer;
//			Object target = getTarget(consumer);
//			Objects.requireNonNull(target);
//			this.targetRef = new WeakReference<>(target);
//
//			Method action = getAction(consumer);
//			Objects.requireNonNull(action);
//			this.actionMethod = action;
		}


		public boolean accept(T t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
		{
			if (this.get() != null)
			{
				this.consumer.accept(t);
				// this.actionMethod.invoke(this.targetRef, t);
				return true;
			}

			return false;
		}


		static <T> Object getTarget(Consumer<T> consumer)
		{
			Field[] declaredFields = consumer.getClass().getDeclaredFields();
			for (Field field : declaredFields)
			{
				if ("arg$1".equals(field.getName()))
				{
					try
					{
						field.setAccessible(true);
						return field.get(consumer);
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
						log.error(e.toString());
					}
				}
			}

			return null;
		}


		static <T> Method getAction(Consumer<T> consumer)
		{
			Method[] declaredMethods = consumer.getClass().getDeclaredMethods();
			for (Method method : declaredMethods)
			{
				if ("accept".equals(method.getName()))
				{
					return method;
				}
			}

			return null;
		}
	}

	// ------------------------------------------------------------------------------------------------------------------
	// Subscription
	// ------------------------------------------------------------------------------------------------------------------
	public class Subscription implements AutoCloseable
	{

		private int id;

		public Subscription(int id)
		{
			this.id = id;
		}


		@Override
		public void close() /* throws Exception */
		{
			subscribers.remove(this.id);
		}
	}
}
