package hu.perit.spvitamin.spring.logging;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class AbstractInterfaceLoggerTest
{

    private static String PASSWORD = "password";
    private static String AUTHORIZATION = "authorization";

    @Test
    void testPassword()
    {
        String maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(PASSWORD, "1");
        log.debug(PASSWORD + String.format(" 1 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(PASSWORD, "12");
        log.debug(PASSWORD + String.format(" 12 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(PASSWORD, "123456789012345678901234567890123456789012345678901234567890");
        log.debug(PASSWORD + String.format("  60 char long input -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);
    }


    @Test
    void testAuthorization()
    {
        String maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "1");
        log.debug(AUTHORIZATION + String.format(" 1 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "12");
        log.debug(AUTHORIZATION + String.format(" 12 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "123");
        log.debug(AUTHORIZATION + String.format(" 123 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "1234");
        log.debug(AUTHORIZATION + String.format(" 1234 -> '%s'", maskedHeaderValue));
        assertEquals("***", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "12345");
        log.debug(AUTHORIZATION + String.format(" 12345 -> '%s'", maskedHeaderValue));
        assertEquals("1...", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "123456");
        log.debug(AUTHORIZATION + String.format(" 123456 -> '%s'", maskedHeaderValue));
        assertEquals("12...", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "1234567");
        log.debug(AUTHORIZATION + String.format(" 1234567 -> '%s'", maskedHeaderValue));
        assertEquals("123...", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "12345678");
        log.debug(AUTHORIZATION + String.format(" 12345678 -> '%s'", maskedHeaderValue));
        assertEquals("1234...", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "123456789");
        log.debug(AUTHORIZATION + String.format(" 123456789 -> '%s'", maskedHeaderValue));
        assertEquals("1234...", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "1234567890");
        log.debug(AUTHORIZATION + String.format(" 1234567890 -> '%s'", maskedHeaderValue));
        assertEquals("12345...", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "12345678901234567890123456789012345678901234567890");
        log.debug(AUTHORIZATION + String.format("  50 char long input -> '%s'", maskedHeaderValue));
        assertEquals("1234567890123456789012345...", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "123456789012345678901234567890123456789012345678901234567890");
        log.debug(AUTHORIZATION + String.format("  60 char long input -> '%s'", maskedHeaderValue));
        assertEquals("123456789012345678901234567890...", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        log.debug(AUTHORIZATION + String.format(" 120 char long input -> '%s'", maskedHeaderValue));
        assertEquals("1234567890123456789012345678901234567...", maskedHeaderValue);

        maskedHeaderValue = AbstractInterfaceLogger.getMaskedHeaderValue(AUTHORIZATION, "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        log.debug(AUTHORIZATION + String.format(" 180 char long input -> '%s'", maskedHeaderValue));
        assertEquals("1234567890123456789012345678901234567...", maskedHeaderValue);
    }

}
