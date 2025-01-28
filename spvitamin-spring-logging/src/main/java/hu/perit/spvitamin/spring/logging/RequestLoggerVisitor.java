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

package hu.perit.spvitamin.spring.logging;

import hu.perit.spvitamin.core.thing.PrinterVisitor;
import jakarta.xml.bind.JAXBElement;

public class RequestLoggerVisitor extends PrinterVisitor
{
    public RequestLoggerVisitor(Options options)
    {
        super(options);
    }


    @Override
    protected String convertProperty(String name, Object value)
    {
        if (value instanceof JAXBElement<?> jaxbElement && jaxbElement.getValue() instanceof byte[] bytes)
        {
            return formatByteArray(bytes);
        }

        return super.convertProperty(name, value);
    }
}
