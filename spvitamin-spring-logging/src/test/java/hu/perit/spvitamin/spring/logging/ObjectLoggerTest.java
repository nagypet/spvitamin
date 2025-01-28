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

package hu.perit.spvitamin.spring.logging;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Data
class Job
{
    private final String title;
}

@Data
class Person
{
    private final String name;
    private final int age;
    private final boolean smoker;
    private final Job job;
}


@Slf4j
class ObjectLoggerTest
{

    @Test
    void test()
    {
        Person person = new Person("Peter Nagy", 57, false, new Job("Senior Solution Architect"));
        String string = ObjectLogger.toString(person);
        log.debug(string);
        assertEquals("{\"name\":\"Peter Nagy\",\"age\":57,\"smoker\":false,\"job\":{\"title\":\"Senior Solution Architect\"}}", string);
    }

    @Test
    void testList()
    {
        String string = ObjectLogger.toString(List.of("Alma", "Korte", "Szilva"));
        log.debug(string);
        assertEquals("[\"Alma\",\"Korte\",\"Szilva\"]", string);
    }

}
