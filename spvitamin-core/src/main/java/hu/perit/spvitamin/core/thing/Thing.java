/*
 * Copyright 2020-2024 the original author or authors.
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

package hu.perit.spvitamin.core.thing;

import hu.perit.spvitamin.core.reflection.Property;
import hu.perit.spvitamin.core.reflection.ReflectionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Using composite pattern.
 */

@Getter
@Setter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Thing
{
    private String name;

    public static Thing from(Object object)
    {
        return valueToThing(object, false);
    }

    public static Thing from(Object object, Boolean includePrivate)
    {
        return valueToThing(object, includePrivate);
    }

    public abstract void accept(String name, ThingVisitor visitor);

    abstract boolean isEmpty();


    private static Value objectToValue(Object object)
    {
        return new Value(object);
    }


    private static Thing valueToThing(Object object, Boolean includePrivate)
    {
        if (object == null || ReflectionUtils.isTerminalType(object))
        {
            return objectToValue(object);
        }

        if (object instanceof Collection<?> list)
        {
            return convertCollection(list, includePrivate);
        }
        else if (object instanceof Map<?, ?> map)
        {
            return convertMap(map, includePrivate);
        }

        List<Property> properties = ReflectionUtils.propertiesOf(object.getClass(), includePrivate);
        if (properties.isEmpty())
        {
            // Enums come here
            return objectToValue(object);
        }
        ValueMap valueMap = new ValueMap();
        for (Property property : properties)
        {
            try
            {
                Object propertyValue = property.get(object);
                valueMap.getProperties().put(property.getName(), valueToThing(propertyValue, includePrivate));
            }
            catch (IllegalAccessException | InvocationTargetException | RuntimeException e)
            {
                log.warn("Cannot process property {}.{}: {}", object.getClass().getName(), property.getName(), e.getMessage());
            }
        }
        return valueMap;
    }


    private static ValueList convertCollection(Collection<?> collection, Boolean includePrivate)
    {
        ValueList valueList = new ValueList();
        for (Object item : collection)
        {
            valueList.getElements().add(valueToThing(item, includePrivate));
        }
        return valueList;
    }


    private static ValueMap convertMap(Map<?, ?> map, Boolean includePrivate)
    {
        ValueMap valueMap = new ValueMap();
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            valueMap.getProperties().put(entry.getKey().toString(), valueToThing(entry.getValue(), includePrivate));
        }
        return valueMap;
    }
}
