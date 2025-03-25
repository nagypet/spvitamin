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

package hu.perit.spvitamin.core.exception;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@Slf4j
class ServerExceptionPropertiesTest {

    @Test
    void testWithStackTraceDisabled() {
        ServerExceptionProperties.setStackTraceEnabled(false);

        RuntimeException ex = new RuntimeException("some problem", new RuntimeException(new NullPointerException()));
        ServerExceptionProperties exceptionProperties = new ServerExceptionProperties(ex);
        Exception exception = exceptionProperties.toException();
        Assertions.assertEquals(1, exception.getStackTrace().length);

        ServerException serverException = new ServerException(exceptionProperties);
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(serverException);
        Optional<NullPointerException> npeOptional = exceptionWrapper.getFromCauseChain(NullPointerException.class);
        Assertions.assertTrue(npeOptional.isPresent());

        log.debug(StackTracer.toString(npeOptional.get()));

        String result = StackTracer.toString(npeOptional.get());
        Assertions.assertTrue(result.startsWith("java.lang.NullPointerException: java.lang.NullPointerException\n" +
                "    => 1: hu.perit.spvitamin.core.exception.ServerExceptionPropertiesTest.testWithStackTraceDisabled(ServerExceptionPropertiesTest.java:"));
    }


    @Test
    void testWithStackTraceEnabled() {
        ServerExceptionProperties.setStackTraceEnabled(true);

        RuntimeException ex = new RuntimeException("some problem", new RuntimeException(new NullPointerException()));
        ServerExceptionProperties exceptionProperties = new ServerExceptionProperties(ex);
        //Exception exception = exceptionProperties.toException();
        //this.compare(ex, exception);

        ServerException serverException = new ServerException(exceptionProperties);
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(serverException);
        Optional<NullPointerException> npeOptional = exceptionWrapper.getFromCauseChain(NullPointerException.class);
        Assertions.assertTrue(npeOptional.isPresent());

        log.debug(StackTracer.toString(npeOptional.get()));

        String result = StackTracer.toString(npeOptional.get());
        Assertions.assertTrue(result.startsWith("java.lang.NullPointerException: java.lang.NullPointerException\n" +
                "    => 1: hu.perit.spvitamin.core.exception.ServerExceptionPropertiesTest.testWithStackTraceEnabled(ServerExceptionPropertiesTest.java:"));
    }


    private void compare(Throwable ex1, Throwable ex2) {
        if (ex1.getCause() != null) {
            this.compare(ex1.getCause(), ex2.getCause());
        }
        if (!ex1.getClass().equals(ex2.getClass()) || (!(ex1 instanceof NullPointerException) && !StringUtils.equals(ex1.getMessage(), ex2.getMessage()))) {
            Assertions.fail(String.format("'%s' != '%s'", ex1.getMessage(), ex2.getMessage()));
        }
        if (ex1 instanceof NullPointerException) {
            Assertions.assertEquals(ex1.getStackTrace().length, ex2.getStackTrace().length);
            for (int i = 0; i < ex1.getStackTrace().length; i++) {
                Assertions.assertEquals(ex1.getStackTrace()[i], ex2.getStackTrace()[i]);
            }
        }
    }


    @Test
    void test3() {
        ServerExceptionProperties.setStackTraceEnabled(false);

        ServerExceptionProperties exceptionProperties = new ServerExceptionProperties(new UnexpectedConditionException());
        ServerException serverException = new ServerException(exceptionProperties);

        log.debug(StackTracer.toString(serverException));

        Assertions.assertTrue(StackTracer.toString(serverException).startsWith("hu.perit.spvitamin.core.exception.UnexpectedConditionException: Ooops! Something went wrong!\n" +
                "    => 1: hu.perit.spvitamin.core.exception.ServerExceptionPropertiesTest.test3(ServerExceptionPropertiesTest.java"));
    }


    @Test
    void test4() {
        ServerExceptionProperties.setStackTraceEnabled(true);

        ServerExceptionProperties exceptionProperties = new ServerExceptionProperties(new UnexpectedConditionException());
        ServerException serverException = new ServerException(exceptionProperties);

        log.debug(StackTracer.toString(serverException));

        Assertions.assertTrue(StackTracer.toString(serverException).startsWith("hu.perit.spvitamin.core.exception.UnexpectedConditionException: Ooops! Something went wrong!\n" +
                "    => 1: hu.perit.spvitamin.core.exception.ServerExceptionPropertiesTest.test4(ServerExceptionPropertiesTest.java:"));
    }
}
