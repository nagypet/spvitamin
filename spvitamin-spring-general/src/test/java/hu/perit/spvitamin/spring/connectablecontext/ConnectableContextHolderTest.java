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

package hu.perit.spvitamin.spring.connectablecontext;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import hu.perit.spvitamin.core.connectablecontext.ThreadContextKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@Slf4j
public class ConnectableContextHolderTest
{
    @Test
    public void test() throws InterruptedException
    {
        ThreadSpecificContextHolder threadContextHolder = ThreadSpecificContextHolder.getInstance();

        long startTime = System.currentTimeMillis();

        IntStream.range(0, 10).parallel().forEach(i ->
        {
            ThreadSpecificContext threadContext = threadContextHolder.getContext(new ThreadContextKey());
            log.debug(threadContext.toString());
        });

        // should finish in 5 seconds
        while (threadContextHolder.size() > 0 || (System.currentTimeMillis() - startTime) > 5000)
        {
            threadContextHolder.cleanup(true);
            TimeUnit.MILLISECONDS.sleep(200);
        }

        Assertions.assertTrue(threadContextHolder.size() == 0);
    }
}
