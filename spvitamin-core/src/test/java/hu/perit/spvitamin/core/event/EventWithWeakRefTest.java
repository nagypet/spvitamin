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
