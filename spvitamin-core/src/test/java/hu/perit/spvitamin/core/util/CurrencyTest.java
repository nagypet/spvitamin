package hu.perit.spvitamin.core.util;

import hu.perit.spvitamin.core.thing.Thing;
import hu.perit.spvitamin.json.JSonSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CurrencyTest
{
    @ParameterizedTest
    @ValueSource(strings = {"EUR", "USD", "GBP", "hUF"})
    void testValidCurrency(String currencyCode)
    {
        Currency currency = Currency.fromString(currencyCode);
        log.debug("{} => {}", currencyCode, currency);
        assertThat(currency.isValid()).isTrue();
    }


    @Test
    void testInvalidCurrency()
    {
        Currency currency = Currency.fromString("alma");
        assertThat(currency.isValid()).isFalse();
    }


    @Test
    void testJsonRoundtrip()
    {
        Currency currency = Currency.fromString("EUR");
        String json = JSonSerializer.toJson(currency);
        log.debug(json);
        Currency currency2 = JSonSerializer.fromJson(json, Currency.class);
        assertThat(currency2).isEqualTo(currency);
    }


    @Data
    private static class TestClassWithString
    {
        private String currency;
        private String text;
    }


    @Data
    private static class TestClassWithCurrency
    {
        private Currency currency;
        private String text;
    }


    @Test
    void testBehavesLikeString()
    {
        TestClassWithCurrency testClassWithCurrency = new TestClassWithCurrency();
        testClassWithCurrency.setCurrency(Currency.fromString("EUR"));
        testClassWithCurrency.setText("hello");

        TestClassWithString testClassWithString = new TestClassWithString();
        testClassWithString.setCurrency("EUR");
        testClassWithString.setText("hello");

        String json1 = JSonSerializer.toJson(testClassWithCurrency);
        log.debug(json1);
        String json2 = JSonSerializer.toJson(testClassWithString);
        assertThat(json1).isEqualTo(json2);
    }


    @Test
    void testCurrencyInThing()
    {
        TestClassWithCurrency testClassWithCurrency = new TestClassWithCurrency();
        testClassWithCurrency.setCurrency(Currency.fromString("EUR"));
        testClassWithCurrency.setText("hello");

        TestClassWithString testClassWithString = new TestClassWithString();
        testClassWithString.setCurrency("EUR");
        testClassWithString.setText("hello");

        Thing thing1 = Thing.from(testClassWithCurrency);
        Thing thing2 = Thing.from(testClassWithString);
        log.debug(thing1.toString());
        log.debug(thing2.toString());
        assertThat(thing1).isEqualTo(thing2);
    }
}
