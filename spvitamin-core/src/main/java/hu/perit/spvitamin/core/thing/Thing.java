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

package hu.perit.spvitamin.core.thing;

import hu.perit.spvitamin.core.reflection.Property;
import hu.perit.spvitamin.core.reflection.ReflectionUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Using composite pattern.
 */

@Getter
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
public abstract class Thing
{
    private final String name;

    public static Thing from(Object object)
    {
        return valueToThing(null, object, false);
    }

    public static Thing from(Object object, Boolean includePrivate)
    {
        return valueToThing(null, object, includePrivate);
    }

    public abstract void accept(ThingVisitor visitor);

    abstract boolean isEmpty();


    private static Value objectToValue(String name, Object object)
    {
        return new Value(name, object);
    }


    private static Thing valueToThing(String name, Object object, Boolean includePrivate)
    {
        if (object == null || ReflectionUtils.isTerminalType(object))
        {
            return objectToValue(name, object);
        }

        if (object instanceof Collection<?> list)
        {
            return convertCollection(name, list, includePrivate);
        }
        else if (object instanceof Map<?, ?> map)
        {
            return convertMap(name, map, includePrivate);
        }

        List<Property> properties = ReflectionUtils.allPropertiesOf(object.getClass(), includePrivate);
        if (properties.isEmpty())
        {
            // Enums come here
            return objectToValue(name, object);
        }
        ValueMap valueMap = new ValueMap(name);
        for (Property property : properties)
        {
            String propertyName = property.getName();
            try
            {
                Object propertyValue = property.get(object);
                valueMap.getProperties().put(propertyName, valueToThing(propertyName, propertyValue, includePrivate));
            }
            catch (IllegalAccessException | InvocationTargetException | RuntimeException e)
            {
                log.warn("Cannot process property {}.{}: {}", object.getClass().getName(), propertyName, e.getMessage());
            }
        }
        return valueMap;
    }


    private static ValueList convertCollection(String name, Collection<?> collection, Boolean includePrivate)
    {
        ValueList valueList = new ValueList(name);
        for (Object item : collection)
        {
            valueList.getElements().add(valueToThing(name, item, includePrivate));
        }
        return valueList;
    }


    private static ValueMap convertMap(String name, Map<?, ?> map, Boolean includePrivate)
    {
        ValueMap valueMap = new ValueMap(name);
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            String propertyName = entry.getKey().toString();
            valueMap.getProperties().put(propertyName, valueToThing(propertyName, entry.getValue(), includePrivate));
        }
        return valueMap;
    }
}
