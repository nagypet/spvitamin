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

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class EventTest
{
	private static final Event2<String> EVENT = new Event2<>();

	private static class Subscriber
	{
		Subscriber()
		{
			EVENT.subscribe(this::eventHandler);
		}


		void eventHandler(String eventParam)
		{
			log.debug(String.format("%s event handler called with parameter '%s'", this, eventParam));
		}
	}

	@Test
	void testEvent()
	{
		Subscriber subscriber1 = new Subscriber();
		Subscriber subscriber2 = new Subscriber();
		Subscriber subscriber3 = new Subscriber();

		EVENT.fire("1st event");

		subscriber2 = null;

		log.debug("Calling System.gc()");
		System.gc();

		EVENT.fire("2nd event");
		
		log.debug("Calling System.gc()");
		System.gc();
		
		EVENT.fire("3rd event");
	}


	@Test
	void testWeakReference()
	{
		Subscriber subscriber1 = new Subscriber();
		Subscriber subscriber2 = new Subscriber();
		Subscriber subscriber3 = new Subscriber();

//		Object target1 = Event2.Reference.getTarget(subscriber1::eventHandler);
//		Object target2 = Event2.Reference.getTarget(subscriber2::eventHandler);

//		WeakReference<Object> weakReference1 = new WeakReference<>(target1);
//		WeakReference<Object> weakReference2 = new WeakReference<>(target2);

		Event2.Reference<String> reference1 = new Event2.Reference<>(subscriber1::eventHandler);
		Event2.Reference<String> reference2 = new Event2.Reference<>(subscriber2::eventHandler);

//		WeakReference<Subscriber> weakReference3 = new WeakReference<>(subscriber1);
//		WeakReference<Subscriber> weakReference4 = new WeakReference<>(subscriber2);

//		log.debug(String.format("weakReference1 is available: %b", weakReference1.get() != null));
//		log.debug(String.format("weakReference2 is available: %b", weakReference2.get() != null));
		log.debug(String.format("reference1 is available: %b", reference1.get() != null));
		log.debug(String.format("reference2 is available: %b", reference2.get() != null));

		subscriber2 = null;
		//target2 = null;
		// ref2 = null;
		// ref2.targetRef = null;
		// ref2.consumerRef = null;

		log.debug("Calling System.gc()");
		System.gc();

//		log.debug(String.format("weakReference1 is available: %b", weakReference1.get() != null));
//		log.debug(String.format("weakReference2 is available: %b", weakReference2.get() != null));
//		log.debug(String.format("weakReference3 is available: %b", weakReference3.get() != null));
//		log.debug(String.format("weakReference4 is available: %b", weakReference4.get() != null));
		log.debug(String.format("reference1 is available: %b", reference1.get() != null));
		log.debug(String.format("reference2 is available: %b", reference2.get() != null));

	}

}
