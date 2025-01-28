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

package hu.perit.spvitamin.core.reflection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Simple Reflection Utility
 *
 * @author Peter Nagy
 */

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils
{
    /**
     * Returns the getters of the class.
     *
     * @param clazz
     * @param includeInherited
     * @return List<Method>
     */
    public static List<Method> gettersOf(Class<?> clazz, Boolean includeInherited)
    {
        Method[] methods = BooleanUtils.isTrue(includeInherited) ? clazz.getMethods() : clazz.getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> isGetter(method) && isNonStatic(method))
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }


    /**
     * Returns the getters of the class.
     *
     * @param clazz
     * @param includeInherited
     * @return List<Method>
     */
    public static List<Method> settersOf(Class<?> clazz, Boolean includeInherited)
    {
        Method[] methods = BooleanUtils.isTrue(includeInherited) ? clazz.getMethods() : clazz.getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> isSetter(method) && isNonStatic(method))
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }


    /**
     * Returns all properties of the given class
     *
     * @param clazz
     * @param includePrivate
     * @return List<Property>
     */
    public static List<Property> propertiesOf(Class<?> clazz, Boolean includePrivate)
    {
        List<Property> properties = new ArrayList<>();

        List<Method> methods = gettersOf(clazz, false);
        List<Property> getterProperties = methods.stream()
                .filter(method -> BooleanUtils.isTrue(includePrivate) || Modifier.isPublic(method.getModifiers()))
                .map(method -> Property.fromGetter(method))
                .toList();

        Field[] fields = BooleanUtils.isTrue(includePrivate) ? clazz.getDeclaredFields() : clazz.getFields();
        List<Property> fieldProperties = Arrays.stream(fields)
                .filter(field -> isNonStatic(field) && field.getDeclaringClass().equals(clazz))
                .filter(field -> thereIsNoGetterWithName(getterProperties, field.getName()))
                .map(field -> Property.fromField(field))
                .toList();

        properties.addAll(fieldProperties);
        properties.addAll(getterProperties);
        return properties;
    }


    private static boolean thereIsNoGetterWithName(List<Property> getters, String name)
    {
        return getters.stream().noneMatch(method -> method.getName().equals(name));
    }


    /**
     * Returns all properties of the given class and of each base classes
     *
     * @param clazz
     * @param includePrivate
     * @return List<Property>
     */
    public static List<Property> allPropertiesOf(Class<?> clazz, Boolean includePrivate)
    {
        List<Property> properties = new ArrayList<>();

        if (clazz == null || clazz.equals(Object.class))
        {
            return properties;
        }

        // Adding properties of the superclass
        properties.addAll(allPropertiesOf(clazz.getSuperclass(), includePrivate));

        // Adding properties of self
        properties.addAll(propertiesOf(clazz, includePrivate));

        return properties;
    }


    public static boolean isStatic(Field field)
    {
        return Modifier.isStatic(field.getModifiers());
    }


    public static boolean isNonStatic(Field field)
    {
        return !isStatic(field);
    }


    /**
     * Returns the property name from a getter/setter method
     * <p>
     * - getTextValue() => 'textValue'
     * - isAvalilable() => 'available'
     * - setAge(int age) => 'age'
     * <p>
     * The provided method must be a getter or a setter
     *
     * @param method
     * @return Optional<String> or Optional.empty()
     */
    public static Optional<String> getFieldNameFromMethod(Method method)
    {
        if (isGetter(method) || isSetter(method))
        {
            if (method.getName().startsWith("get") || method.getName().startsWith("set"))
            {
                return Optional.of(lowerCamelCase(method.getName().substring(3)));
            }
            else if (method.getName().startsWith("is"))
            {
                return Optional.of(lowerCamelCase(method.getName().substring(2)));
            }
        }

        return Optional.empty();
    }


    /**
     * Checks if the method is a getter
     *
     * @param method
     * @return true if the method is a getter
     */
    public static boolean isGetter(Method method)
    {
        return method.getParameters().length == 0 && (method.getName().startsWith("get") || method.getName().startsWith("is"));
    }


    /**
     * Checks if the method is a setter
     *
     * @param method
     * @return true if the method is a setter
     */
    public static boolean isSetter(Method method)
    {
        return method.getParameters().length == 1 && (method.getName().startsWith("set"));
    }


    /**
     * Checks if the method is static
     *
     * @param method
     * @return true if the method is static
     */
    public static boolean isStatic(Method method)
    {
        return Modifier.isStatic(method.getModifiers());
    }


    /**
     * Checks if the method is non-static
     *
     * @param method
     * @return true if the method is non-static
     */
    public static boolean isNonStatic(Method method)
    {
        return !isStatic(method);
    }


    public static Optional<Method> getGetter(Class<?> clazz, String fieldName)
    {
        List<Method> getters = gettersOf(clazz, true);
        return getters.stream()
                .filter(getter -> getFieldNameFromMethod(getter).map(s -> s.equalsIgnoreCase(fieldName)).orElse(false)).findFirst();
    }


    public static Optional<Method> getSetter(Class<?> clazz, String fieldName)
    {
        List<Method> setters = settersOf(clazz, true);
        return setters.stream().filter(setter -> getFieldNameFromMethod(setter).map(s -> s.equalsIgnoreCase(fieldName)).orElse(false)).findFirst();
    }


    public static Optional<Field> getField(Class<?> clazz, String fieldName)
    {
        Field[] fields = clazz.getDeclaredFields();
        return Arrays.stream(fields).filter(field -> field.getName().equals(fieldName)).findFirst();
    }


    private static String lowerCamelCase(String name)
    {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }


    public static Object getSubject(Method method, Object object)
    {
        return isStatic(method) ? null : object;
    }


    public static Object getSubject(Field field, Object object)
    {
        return isStatic(field) ? null : object;
    }


    // Returns true, if this type of object cannot be converted
    public static boolean isTerminalType(Object object)
    {
        if (object == null)
        {
            return true;
        }

        return isTerminalType(object.getClass());
    }


    public static boolean isTerminalType(Class<?> clazz)
    {
        if (clazz.isPrimitive() || clazz.isEnum())
        {
            return true;
        }

        // Special Java types
        if (clazz.isAssignableFrom(java.util.Date.class)
                || clazz.isAssignableFrom(XMLGregorianCalendar.class)
                || clazz.isAssignableFrom(TimeUnit.class)
                || clazz.isAssignableFrom(TimeZone.class)
        )
        {
            return true;
        }

        if (clazz.getSimpleName().equals("JAXBElement"))
        {
            return true;
        }

        String packageName = clazz.getPackageName();
        return packageName.startsWith("java.lang")
                || packageName.startsWith("java.time")
                || packageName.startsWith("java.io")
                || packageName.startsWith("java.math")
                ;
    }
}
