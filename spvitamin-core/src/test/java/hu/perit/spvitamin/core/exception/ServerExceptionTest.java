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

package hu.perit.spvitamin.core.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@Slf4j
class ServerExceptionTest {

    @Test
    void test() {
        String expected;
        String actual = null;
        try {
            throw new NullPointerException();
        }
        catch (Exception ex) {
            expected = StackTracer.toString(ex);
            log.debug("original ex: " + expected);

            try {
                ServerException.throwFrom(ex);
            }
            catch (Exception ex2) {
                actual = StackTracer.toString(ex2);
                log.debug("rethrown ex: " + actual);
            }

        }

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void test2() {
        String expected;
        String actual = null;
        try {
            throw new UnexpectedConditionException("valami rossz", new NullPointerException());
        }
        catch (Exception ex) {
            expected = StackTracer.toString(ex);
            log.debug("original ex: " + expected);

            try {
                ServerException.throwFrom(ex);
            }
            catch (Exception ex2) {
                actual = StackTracer.toString(ex2);
                log.debug("rethrown ex: " + actual);
            }

        }

        Assertions.assertEquals(expected, actual);
    }

}