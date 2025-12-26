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

package hu.perit.spvitamin.spring.resilientjobrunner;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Generated;

@Converter
@Generated // To disable counting in unit test coverage
public class ResilientJobStatusConverter implements AttributeConverter<ResilientJobStatus, Long>
{
    @Override
    public Long convertToDatabaseColumn(ResilientJobStatus attribute)
    {
        if (attribute == null)
        {
            return null;
        }

        return attribute.getValue();
    }


    @Override
    public ResilientJobStatus convertToEntityAttribute(Long dbData)
    {
        if (dbData == null)
        {
            return null;
        }

        return ResilientJobStatus.fromValue(dbData).orElse(null);
    }
}
