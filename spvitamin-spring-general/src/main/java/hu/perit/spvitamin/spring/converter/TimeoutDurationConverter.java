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

package hu.perit.spvitamin.spring.converter;

import hu.perit.spvitamin.core.typehelpers.TimeoutDuration;
import hu.perit.spvitamin.core.typehelpers.TimeoutDurationStyle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class TimeoutDurationConverter implements Converter<String, TimeoutDuration>
{
    @Override
    public TimeoutDuration convert(String source)
    {
        if (StringUtils.isBlank(source))
        {
            return null;
        }
        return TimeoutDurationStyle.detectAndParse(source.trim());
    }
}
