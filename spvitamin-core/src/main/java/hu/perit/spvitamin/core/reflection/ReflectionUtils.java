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

package hu.perit.spvitamin.core.reflection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
                .filter(method -> isGetter(method) && isNonStatic(method) && isAccesible(method))
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
                .filter(method -> isSetter(method) && isNonStatic(method) && isAccesible(method))
                .toList();
    }


    /**
     * Returns all non-static fields
     *
     * @param clazz
     * @param includePrivate
     * @return
     */
    public static List<Field> propertiesOf(Class<?> clazz, Boolean includePrivate)
    {
        Field[] fields = BooleanUtils.isTrue(includePrivate) ? clazz.getDeclaredFields() : clazz.getFields();
        return Arrays.stream(fields)
                .filter(field -> isNonStatic(field) && isAccesible(field))
                .toList();
    }


    private static boolean isAccesible(Field field)
    {
        if (field.isAccessible())
        {
            log.debug(String.format("%s() is not accessible", field.getName()));
            return false;
        }

        return true;
    }


    private static boolean isStatic(Field field)
    {
        int modifiers = field.getModifiers();
        return (modifiers & Modifier.STATIC) != 0;
    }


    private static boolean isNonStatic(Field field)
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
        int modifiers = method.getModifiers();
        return (modifiers & Modifier.STATIC) != 0;
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


    /**
     * Checks if the method is publicly available
     *
     * @param method
     * @return true if the method is public
     */
    public static boolean isAccesible(Method method)
    {
        if (method.isAccessible())
        {
            log.debug(String.format("%s() is not public", method.getName()));
            return false;
        }

        return true;
    }


    public static Optional<Method> getSetter(Class<?> clazz, String fieldName)
    {
        List<Method> setters = settersOf(clazz, true);
        return setters.stream().filter(setter -> getFieldNameFromMethod(setter).map(s -> s.equalsIgnoreCase(fieldName)).orElse(false)).findFirst();
    }


    private static String lowerCamelCase(String name)
    {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }
}
