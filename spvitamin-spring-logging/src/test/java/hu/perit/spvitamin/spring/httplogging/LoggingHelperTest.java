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

package hu.perit.spvitamin.spring.httplogging;

import static org.junit.jupiter.api.Assertions.*;

import hu.perit.spvitamin.spring.httplogging.LoggingHelper;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class LoggingHelperTest
{

    private static String PASSWORD = "password";
    private static String AUTHORIZATION = "authorization";

    @Test
    void testPassword()
    {
        String maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(PASSWORD, "1");
        log.debug(PASSWORD + String.format(" 1 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(PASSWORD, "12");
        log.debug(PASSWORD + String.format(" 12 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(PASSWORD, "123456789012345678901234567890123456789012345678901234567890");
        log.debug(PASSWORD + String.format("  60 char long input -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);
    }


    @Test
    void testAuthorization()
    {
        String maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "1");
        log.debug(AUTHORIZATION + String.format(" 1 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "12");
        log.debug(AUTHORIZATION + String.format(" 12 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "123");
        log.debug(AUTHORIZATION + String.format(" 123 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "1234");
        log.debug(AUTHORIZATION + String.format(" 1234 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "12345");
        log.debug(AUTHORIZATION + String.format(" 12345 -> '%s'", maskedHeaderValue));
        assertEquals("1...", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "123456");
        log.debug(AUTHORIZATION + String.format(" 123456 -> '%s'", maskedHeaderValue));
        assertEquals("12...", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "1234567");
        log.debug(AUTHORIZATION + String.format(" 1234567 -> '%s'", maskedHeaderValue));
        assertEquals("123...", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "12345678");
        log.debug(AUTHORIZATION + String.format(" 12345678 -> '%s'", maskedHeaderValue));
        assertEquals("1234...", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "123456789");
        log.debug(AUTHORIZATION + String.format(" 123456789 -> '%s'", maskedHeaderValue));
        assertEquals("1234...", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "1234567890");
        log.debug(AUTHORIZATION + String.format(" 1234567890 -> '%s'", maskedHeaderValue));
        assertEquals("12345...", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "12345678901234567890123456789012345678901234567890");
        log.debug(AUTHORIZATION + String.format("  50 char long input -> '%s'", maskedHeaderValue));
        assertEquals("1234567890123456789012345...", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "123456789012345678901234567890123456789012345678901234567890");
        log.debug(AUTHORIZATION + String.format("  60 char long input -> '%s'", maskedHeaderValue));
        assertEquals("123456789012345678901234567890...", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        log.debug(AUTHORIZATION + String.format(" 120 char long input -> '%s'", maskedHeaderValue));
        assertEquals("1234567890123456789012345678901234567...", maskedHeaderValue);

        maskedHeaderValue = LoggingHelper.getMaskedHeaderValue(AUTHORIZATION, "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        log.debug(AUTHORIZATION + String.format(" 180 char long input -> '%s'", maskedHeaderValue));
        assertEquals("1234567890123456789012345678901234567...", maskedHeaderValue);
    }

}
