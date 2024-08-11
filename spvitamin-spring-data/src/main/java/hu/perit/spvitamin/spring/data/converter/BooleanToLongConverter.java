/*
 * Copyright (c) 2024. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.spring.data.converter;

import jakarta.persistence.AttributeConverter;

public class BooleanToLongConverter implements AttributeConverter<Boolean, Long>
{
    @Override
    public Long convertToDatabaseColumn(Boolean aBoolean)
    {
        if (aBoolean == null)
        {
            return null;
        }
        return aBoolean ? 1L : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Long aLong)
    {
        if (aLong == null)
        {
            return null; // NOSONAR
        }
        return aLong > 0L;
    }
}
