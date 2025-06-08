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
 * An abstract base class implementing the Composite pattern for representing hierarchical data structures.
 * 
 * <p>This class provides a flexible way to convert arbitrary Java objects into a tree-like
 * structure that can be traversed and manipulated using the Visitor pattern. It handles
 * various types of objects including primitive values, collections, maps, arrays, and
 * complex objects with properties.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Automatic conversion of Java objects to Thing hierarchies</li>
 *   <li>Support for collections, maps, arrays, and nested objects</li>
 *   <li>Visitor pattern implementation for traversing and processing the hierarchy</li>
 *   <li>Option to include or exclude private fields</li>
 *   <li>Special handling for terminal types and byte arrays</li>
 * </ul>
 * 
 * <p>The class hierarchy includes specialized implementations for different types of values:</p>
 * <ul>
 *   <li>Value - for primitive and terminal types</li>
 *   <li>ValueList - for collections and arrays</li>
 *   <li>ValueMap - for maps and objects with properties</li>
 * </ul>
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

        // Special handling for byte arrays - treat them as terminal types
        if (object instanceof byte[])
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
        else if (object.getClass().isArray())
        {
            return convertArray(name, object, includePrivate);
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
                if (!property.isIgnored())
                {
                    valueMap.getProperties().put(propertyName, valueToThing(propertyName, propertyValue, includePrivate));
                }
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


    private static ValueList convertArray(String name, Object array, Boolean includePrivate)
    {
        ValueList valueList = new ValueList(name);
        int length = java.lang.reflect.Array.getLength(array);
        for (int i = 0; i < length; i++)
        {
            Object item = java.lang.reflect.Array.get(array, i);
            valueList.getElements().add(valueToThing(name, item, includePrivate));
        }
        return valueList;
    }
}
