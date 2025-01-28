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

import java.lang.annotation.Annotation;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@Slf4j
class ExceptionWrapperTest {

    @Test
    void getAllCauses() {
        // With original exception
        RuntimeException ex = new RuntimeException("some problem", new RuntimeException("another problem", new NullPointerException()));
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);
        String causes = exceptionWrapper.toStringWithCauses();
        log.debug(causes);
        Assertions.assertEquals(ExceptionWrapper.removeLineSeparators("java.lang.RuntimeException: some problem\r\n" +
                "  caused by java.lang.RuntimeException: another problem\r\n" +
                "    caused by java.lang.NullPointerException"), ExceptionWrapper.removeLineSeparators(causes));

        // With ServerException
        ServerException serverException = new ServerException(ex);
        ExceptionWrapper exceptionWrapper2 = ExceptionWrapper.of(serverException);
        String causes2 = exceptionWrapper2.toStringWithCauses();
        log.debug(causes2);
        Assertions.assertEquals(causes, causes2);
    }

    @Test
    void getAllCauses2() {
        // With original exception
        RuntimeException ex = new RuntimeException("some problem");
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);
        String causes = exceptionWrapper.toStringWithCauses();
        log.debug(causes);
        Assertions.assertEquals("java.lang.RuntimeException: some problem", causes);

        // With ServerException
        ServerException serverException = new ServerException(ex);
        ExceptionWrapper exceptionWrapper2 = ExceptionWrapper.of(serverException);
        String causes2 = exceptionWrapper2.toStringWithCauses();
        log.debug(causes2);
        Assertions.assertEquals(causes, causes2);
    }

    @Test
    void getAllCauses3() {
        RuntimeException ex = new RuntimeException("some problem\nmultiline error text\nthird row");
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);
        String causes = exceptionWrapper.toStringWithCauses();
        log.debug(causes);
        Assertions.assertEquals("java.lang.RuntimeException: some problem|multiline error text|third row", causes);
    }

    @Test
    void causedBy() {
        // With original exception
        RuntimeException ex = new RuntimeException("some problem", new RuntimeException(new NullPointerException()));
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);

        Assertions.assertTrue(exceptionWrapper.causedBy(RuntimeException.class));
        Assertions.assertTrue(exceptionWrapper.causedBy(NullPointerException.class));
        Assertions.assertFalse(exceptionWrapper.causedBy(SQLException.class));

        // With ServerException
        ServerException serverException = new ServerException(ex);
        ExceptionWrapper exceptionWrapper2 = ExceptionWrapper.of(serverException);

        Assertions.assertTrue(exceptionWrapper2.causedBy(RuntimeException.class));
        Assertions.assertTrue(exceptionWrapper2.causedBy(NullPointerException.class));
        Assertions.assertFalse(exceptionWrapper2.causedBy(SQLException.class));
    }

    @Test
    void causedBy_withMessageText() {
        // With original exception
        RuntimeException ex = new RuntimeException("some problem", new RuntimeException(new NullPointerException()));
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);

        Assertions.assertTrue(exceptionWrapper.causedBy(RuntimeException.class, "some"));
        Assertions.assertFalse(exceptionWrapper.causedBy(NullPointerException.class, "some"));
        Assertions.assertFalse(exceptionWrapper.causedBy(RuntimeException.class, "none"));
        Assertions.assertTrue(exceptionWrapper.causedBy(RuntimeException.class, ""));

        // With ServerException
        ServerException serverException = new ServerException(ex);
        ExceptionWrapper exceptionWrapper2 = ExceptionWrapper.of(serverException);

        Assertions.assertTrue(exceptionWrapper2.causedBy(RuntimeException.class, "some"));
        Assertions.assertFalse(exceptionWrapper2.causedBy(NullPointerException.class, "some"));
        Assertions.assertFalse(exceptionWrapper2.causedBy(RuntimeException.class, "none"));
        Assertions.assertTrue(exceptionWrapper2.causedBy(RuntimeException.class, ""));
    }

    @Test
    void getFromCauseChain() {
        // With original exception
        RuntimeException ex = new RuntimeException("some problem", new RuntimeException(new NullPointerException()));
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);

        Assertions.assertTrue(exceptionWrapper.getFromCauseChain(RuntimeException.class).isPresent());
        Assertions.assertTrue(exceptionWrapper.getFromCauseChain(NullPointerException.class).isPresent());
        Assertions.assertFalse(exceptionWrapper.getFromCauseChain(SQLException.class).isPresent());

        // With ServerException
        ServerException serverException = new ServerException(ex);
        ExceptionWrapper exceptionWrapper2 = ExceptionWrapper.of(serverException);

        Assertions.assertTrue(exceptionWrapper2.getFromCauseChain(RuntimeException.class).isPresent());
        Assertions.assertTrue(exceptionWrapper2.getFromCauseChain(NullPointerException.class).isPresent());
        Assertions.assertFalse(exceptionWrapper2.getFromCauseChain(SQLException.class).isPresent());
    }

    @Test
    void causedBy_whenExceptionIsSubclass() {
        // With original exception
        InvalidParameterException ex = new InvalidParameterException("hiba");
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);

        Assertions.assertTrue(exceptionWrapper.causedBy(IllegalArgumentException.class));

        // With ServerException
        ServerException serverException = new ServerException(ex);
        ExceptionWrapper exceptionWrapper2 = ExceptionWrapper.of(serverException);

        Assertions.assertTrue(exceptionWrapper2.causedBy(IllegalArgumentException.class));
    }

    @Test
    void causedBy_whenExceptionCauseIsSubclass() {
        // With original exception
        UnexpectedConditionException ex = new UnexpectedConditionException("unexpected", new InvalidParameterException("hiba"));
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);

        Assertions.assertTrue(exceptionWrapper.causedBy(IllegalArgumentException.class));

        // With ServerException
        ServerException serverException = new ServerException(ex);
        ExceptionWrapper exceptionWrapper2 = ExceptionWrapper.of(serverException);

        Assertions.assertTrue(exceptionWrapper2.causedBy(IllegalArgumentException.class));
    }


    @Test
    void instanceOf() {
        // With original exception
        UnexpectedConditionException ex = new UnexpectedConditionException("unexpected");
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);

        Assertions.assertTrue(exceptionWrapper.instanceOf(UnexpectedConditionException.class));
        Assertions.assertTrue(exceptionWrapper.instanceOf(RuntimeException.class));
        Assertions.assertFalse(exceptionWrapper.instanceOf(IllegalArgumentException.class));

        // With ServerException
        ServerException serverException = new ServerException(ex);
        ExceptionWrapper exceptionWrapper2 = ExceptionWrapper.of(serverException);

        Assertions.assertTrue(exceptionWrapper2.instanceOf(UnexpectedConditionException.class));
        Assertions.assertTrue(exceptionWrapper2.instanceOf(RuntimeException.class));
        Assertions.assertFalse(exceptionWrapper2.instanceOf(IllegalArgumentException.class));
    }


    @Disabled(value = "apple")
    private static class MySpecialException extends RuntimeException {

        public MySpecialException(String message) {
            super(message);
        }
    }

    @Test
    void getAnnotations() {
        // With original exception
        MySpecialException ex = new MySpecialException("not found!");
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);
        Annotation[] annotations = exceptionWrapper.getAnnotations();
        for (Annotation annotation : annotations) {
            log.debug(annotation.toString());
        }
        Assertions.assertTrue(annotations.length > 0);

        // With ServerException
        ServerException serverException = new ServerException(ex);
        ExceptionWrapper exceptionWrapper2 = ExceptionWrapper.of(serverException);

        Annotation[] annotations2 = exceptionWrapper2.getAnnotations();
        for (Annotation annotation : annotations2) {
            log.debug(annotation.toString());
        }

        Assertions.assertTrue(annotations2.length > 0);
    }

    @Test
    void testExecutionException()
    {
        ExecutionException ex = new ExecutionException(new ServerException(new NullPointerException()));
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);
        Assertions.assertTrue(exceptionWrapper.causedBy("java.lang.NullPointerException"));
        Assertions.assertFalse(exceptionWrapper.causedBy("something"));
    }
}
