package hu.perit.spvitamin.core.reflection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * Property unifies the characteristics of Fields and getter Methods. Through this type one can handle the
 * public interface of a class in a unified way.
 */

@Getter
@EqualsAndHashCode
public class Property
{
    public enum Type
    {
        FIELD,
        GETTER
    }

    protected Type type;
    protected String name;
    protected Field field;
    protected Method getter;

    protected static Property fromField(Field field)
    {
        Property property = new Property();
        property.type = Type.FIELD;
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
        property.type = Type.GETTER;
        property.name = getFieldNameFromGetter(getter);
        property.getter = getter;
        return property;
    }

    public Class<?> getDeclaringClass()
    {
        return switch (type)
        {
            case FIELD -> field.getDeclaringClass();
            case GETTER -> getter.getDeclaringClass();
        };
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


    public static String lowerCamelCase(String name)
    {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }


    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return switch (type)
        {
            case FIELD -> field.getAnnotation(annotationClass);
            case GETTER -> getter.getAnnotation(annotationClass);
        };
    }


    public boolean canAccess(Object object)
    {
        return switch (type)
        {
            case FIELD -> field.canAccess(object);
            case GETTER -> getter.canAccess(object);
        };
    }


    public void setAccessible(boolean accessible)
    {
        switch (type)
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
    public Object get(Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        if (object == null)
        {
            return null;
        }
        if (type == Type.FIELD)
        {
            if (!field.canAccess(object) && !field.trySetAccessible())
            {
                return MessageFormat.format("{0} is not accessible!", field.getName());
            }

            return field.get(object);
        }
        else if (type == Type.GETTER)
        {
            if (!getter.canAccess(object) && !getter.trySetAccessible())
            {
                return MessageFormat.format("{0} is not accessible!", getter.getName());
            }
            return getter.invoke(object);
        }
        throw new IllegalArgumentException();
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("type", type)
                .append("name", name)
                .toString();
    }
}
