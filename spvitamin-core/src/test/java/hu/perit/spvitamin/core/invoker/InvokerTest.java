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

package hu.perit.spvitamin.core.invoker;

import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * @author Peter Nagy
 */

@Log4j
public class InvokerTest
{
    class Example implements Invoker
    {
        String func1(String text, String title)
        {
            String message = String.format("title: '%s', text: '%s'", title, text);
            log.debug(message);
            return message;
        }

        public List<String> internalQueryDocumentEx(String processID, List<String> documentTypeNames, String documentTypeGroupName, String orderBy, String sqlWhere, int limit)
        {
            log.debug("internalQueryDocumentEx() called!");
            return Collections.emptyList();
        }
    }


    @Test
    public void getMyMethodName() throws InvocationTargetException
    {
        Example example = new Example();

        String message = (String) example.invoke(example, "func1", "alma", null);
        Assertions.assertEquals("title: 'null', text: 'alma'", message);

        example.invoke(example, "internalQueryDocumentEx", "8004075b-359c-4e2e-a6b3-dbcac9a09d73", List.of("ECOL"), null, "ACCNUM ASC", "ACCNUM like '%IOBSTEST%' AND CLINO = '33628543' ", 10);
    }
}