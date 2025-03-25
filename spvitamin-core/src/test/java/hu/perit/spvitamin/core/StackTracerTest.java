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

package hu.perit.spvitamin.core;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import hu.perit.spvitamin.core.exception.ServerExceptionProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@Slf4j
class StackTracerTest
{
    @Test
    void testNullPointerException()
    {
        try
        {
            Object alma = null;
            alma.toString();
        }
        catch (Exception ex)
        {
            String message = StackTracer.toString(ex);
            log.error(message);
            Assertions.assertTrue(message.startsWith("java.lang.NullPointerException: Cannot invoke \"Object.toString()\" because \"alma\" is null\n" +
                    "    => 1: hu.perit.spvitamin.core.StackTracerTest.testNullPointerException(StackTracerTest.java:"));
        }
        log.debug("next line");
    }

    @Test
    void testRuntimeException()
    {
        try
        {
            throw new RuntimeException("hello");
        }
        catch (Exception ex)
        {
            String message = StackTracer.toString(ex);
            Assertions.assertTrue(message.startsWith("java.lang.RuntimeException: hello\n" +
                    "    => 1: hu.perit.spvitamin.core.StackTracerTest.testRuntimeException(StackTracerTest.java:"));
            log.error(StackTracer.toString(ex));
        }
        log.debug("next line");
    }

    class MyVerySpecialException extends Exception
    {
        public MyVerySpecialException(String message)
        {
            super(message);
        }

        public MyVerySpecialException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    @Test
    void testCheckedException()
    {
        try
        {
            throw new MyVerySpecialException("hello");
        }
        catch (Exception ex)
        {
            String message = StackTracer.toString(ex);
            Assertions.assertTrue(message.startsWith("hu.perit.spvitamin.core.StackTracerTest$MyVerySpecialException: hello\n" +
                    "    => 1: hu.perit.spvitamin.core.StackTracerTest.testCheckedException(StackTracerTest.java:"));
            log.error(StackTracer.toString(ex));
        }
        log.debug("next line");
    }

    @Test
    void testEmbeddedException()
    {
        try
        {
            try
            {
                throw new RuntimeException("original cause");
            }
            catch (Exception ex)
            {
                throw new MyVerySpecialException("hello", ex);
            }
        }
        catch (Exception ex)
        {
            ExceptionWrapper exception = ExceptionWrapper.of(ex);
            Assertions.assertTrue(exception.causedBy(RuntimeException.class));
            Assertions.assertTrue(exception.causedBy(MyVerySpecialException.class));
            Optional<RuntimeException> fromCauseChain = exception.getFromCauseChain(RuntimeException.class);
            Assertions.assertEquals("original cause", fromCauseChain.get().getMessage());
        }
        log.debug("next line");
    }


    @Test
    void testEmptyStackTrace()
    {
        try
        {
            try
            {
                String alma = null;
                alma.toUpperCase();
            }
            catch (Exception ex1) {
                try {
                    throw new RuntimeException("original cause", ex1);
                }
                catch (Exception ex2) {
                    throw new MyVerySpecialException("hello", ex2);
                }
            }
        }
        catch (Exception ex)
        {
            ServerExceptionProperties exceptionProperties = new ServerExceptionProperties(ex);
            Exception convertedException = exceptionProperties.toException();

            log.debug(StackTracer.toString(convertedException));
        }
        log.debug("next line");
    }


    @Test
    void testEmptyStackTrace2()
    {
        try
        {
            try
            {
                throw new RuntimeException("root cause");
            }
            catch (Exception ex1) {
                try {
                    throw new RuntimeException("second cause", ex1);
                }
                catch (Exception ex2) {
                    throw new MyVerySpecialException("hello", ex2);
                }
            }
        }
        catch (Exception ex)
        {
            ServerExceptionProperties exceptionProperties = new ServerExceptionProperties(ex);
            Exception convertedException = exceptionProperties.toException();

            log.debug(StackTracer.toString(convertedException));
        }
        log.debug("next line");
    }
}
