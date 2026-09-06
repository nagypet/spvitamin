package hu.perit.spvitamin.spring.data.converter;

import hu.perit.spvitamin.core.util.Currency;
import jakarta.persistence.AttributeConverter;

public class CurrencyDbConverter implements AttributeConverter<Currency, String>
{
    @Override
    public String convertToDatabaseColumn(Currency currency)
    {
        if (currency == null)
        {
            return null;
        }

        return currency.toString();
    }


    @Override
    public Currency convertToEntityAttribute(String dbData)
    {
        return Currency.fromString(dbData);
    }
}
