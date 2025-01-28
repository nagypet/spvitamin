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

package hu.perit.spvitamin.json.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@Slf4j
public class JsonAbstractClassTest
{
	@Test
	void toJsonRoundtrip() throws IOException
	{
		try
		{
			ObjectContainer originalObject = new ObjectContainer();
			originalObject.getList().add(new TextComponent("my text"));
			originalObject.getList().add(new ButtonComponent("my label"));

			String json = originalObject.toJson();
			log.debug(json);

			ObjectContainer decodedObject = ObjectContainer.fromJson(json);
	        log.debug("original: " + originalObject.toString());
	        log.debug("decoded:  " + decodedObject.toString());
			assertEquals(originalObject, decodedObject);
		}
		catch (Exception e)
		{
			log.error(StackTracer.toString(e));
			fail();
		}
	}
}
