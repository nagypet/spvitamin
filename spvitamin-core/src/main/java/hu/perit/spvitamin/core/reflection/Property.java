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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * Property unifies the characteristics of Fields and getter Methods. Through this type one can handle the
 * public interface of a class in a unified way.
 */

@Getter
@EqualsAndHashCode
public class Property implements Member
{
    public enum Kind
    {
        FIELD,
        GETTER
    }

    protected Kind kind;
    protected String name;
    protected Field field;
    protected Method getter;

    protected static Property fromField(Field field)
    {
        Property property = new Property();
        property.kind = Kind.FIELD;
        property.name = field.getName();
        property.field = field;
        return property;
    }

    protected static Property fromGetter(Method getter)
    {
        if (!ReflectionUtils.isGetter(getter))
        {
            throw new ReflectionException(String.format("'%s' is not a getter method!", getter.getName()));
        }

        Property property = new Property();
        property.kind = Kind.GETTER;
        property.name = getFieldNameFromGetter(getter);
        property.getter = getter;
        return property;
    }


    @Override
    public Class<?> getDeclaringClass()
    {
        return switch (kind)
        {
            case FIELD -> field.getDeclaringClass();
            case GETTER -> getter.getDeclaringClass();
        };
    }


    @Override
    public int getModifiers()
    {
        return switch (kind)
        {
            case FIELD -> field.getModifiers();
            case GETTER -> getter.getModifiers();
        };
    }


    @Override
    public boolean isSynthetic()
    {
        return false;
    }


    private static String getFieldNameFromGetter(Method method)
    {
        if (method.getName().startsWith("get"))
        {
            return lowerCamelCase(method.getName().substring(3));
        }
        else if (method.getName().startsWith("is"))
        {
            return lowerCamelCase(method.getName().substring(2));
        }

        throw new ReflectionException(String.format("'%s' is not a getter method!", method.getName()));
    }


    private static String lowerCamelCase(String name)
    {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }


    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        if (this.kind == Kind.FIELD)
        {
            T annotation = this.field.getAnnotation(annotationClass);
            if (annotation != null)
            {
                return annotation;
            }
            // Find the getter if exists
            Method propertyGetter = ReflectionUtils.getGetter(this.field.getDeclaringClass(), this.name).orElse(null);
            if (propertyGetter != null)
            {
                return propertyGetter.getAnnotation(annotationClass);
            }
        }
        else if (this.kind == Kind.GETTER)
        {
            T annotation = this.getter.getAnnotation(annotationClass);
            if (annotation != null)
            {
                return annotation;
            }
            // Find the field if exist
            Field propertyField = ReflectionUtils.getField(this.getter.getDeclaringClass(), this.name).orElse(null);
            if (propertyField != null)
            {
                return propertyField.getAnnotation(annotationClass);
            }
        }
        return null;
    }


    public boolean canAccess(Object object)
    {
        return switch (kind)
        {
            case FIELD -> field.canAccess(object);
            case GETTER -> getter.canAccess(object);
        };
    }


    public void setAccessible(boolean accessible)
    {
        switch (kind)
        {
            case FIELD -> field.setAccessible(accessible);
            case GETTER -> getter.setAccessible(accessible);
        }
    }


    /**
     * Returns the value of the property
     *
     * @return Object
     */
    public Object get(Object object) throws IllegalAccessException, InvocationTargetException
    {
        if (object == null)
        {
            return null;
        }
        if (kind == Kind.FIELD)
        {
            if (!field.canAccess(object) && !field.trySetAccessible())
            {
                throw new IllegalAccessException(MessageFormat.format("{0} is not accessible!", field.getName()));
            }

            return field.get(object);
        }
        else if (kind == Kind.GETTER)
        {
            if (!getter.canAccess(object) && !getter.trySetAccessible())
            {
                throw new IllegalAccessException(MessageFormat.format("{0} is not accessible!", field.getName()));
            }
            return getter.invoke(object);
        }
        throw new IllegalArgumentException();
    }


    public Class<?> getType()
    {
        return switch (kind)
        {
            case FIELD -> field.getType();
            case GETTER -> getter.getReturnType();
        };
    }


    public Object getGenericType()
    {
        return switch (kind)
        {
            case FIELD -> field.getGenericType();
            case GETTER -> getter.getGenericReturnType();
        };
    }



    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("type", kind)
                .append("name", name)
                .toString();
    }
}
