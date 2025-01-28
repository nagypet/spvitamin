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

import java.util.function.Consumer;

@Slf4j
public class EventWithWeakRefTest
{
    private static final EventWithWeakRef<String> EVENT = new EventWithWeakRef<>();

    private static class Subscriber
    {
		// This property holds a strong reference to the listener to keep it in memory as long the Subscriber object exists.
		private Consumer<String> myEventHandler = this::eventHandler;

		Subscriber()
		{
			EVENT.subscribe(this.myEventHandler);
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

		subscriber3 = null;
		log.debug("Calling System.gc()");
		System.gc();

		EVENT.fire("3rd event");
	}
}
