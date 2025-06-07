package hu.perit.spvitamin.core.softenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class can be used instead of an enum. It has the great advantage, that not only predefined values are allowed.
 * It is useful if we are consuming a REST API, but the interface is maintained by someone else and we cannot predict
 * when a new enum value appears. This would cause a runtime error, which can be avoided by using the SoftEnum like this:
 *
 * public final class Status extends SoftEnum<Status>
 * {
 *     public static final Status ACTIVE = new Status("Active");
 *     public static final Status BLOCKED = new Status("Blocked");
 *     public static final Status CLOSED = new Status("Closed");
 *
 *     private Status(String value)
 *     {
 *         super(value, Status.class);
 *     }
 *
 *     @JsonCreator
 *     public static Status fromValue(String value)
 *     {
 *         return SoftEnum.fromValue(value, Status.class, Status::new);
 *     }
 *
 *     public static Collection<Status> values()
 *     {
 *         return SoftEnum.knownValues(Status.class);
 *     }
 * }
 *
 * @param <T>
 */

public abstract class SoftEnum<T extends SoftEnum<T>>
{
    private static final Map<Class<?>, Map<String, SoftEnum<?>>> REGISTRY = new ConcurrentHashMap<>();

    private final String value;

    protected SoftEnum(String value, Class<T> type)
    {
        this.value = value;
        register(value, this, type);
    }

    private static <T extends SoftEnum<T>> void register(String value, SoftEnum<T> instance, Class<T> type)
    {
        REGISTRY.computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                .putIfAbsent(value, instance);
    }

    @JsonValue
    public String getValue()
    {
        return value;
    }

    @JsonCreator
    @SuppressWarnings("unchecked")
    public static <T extends SoftEnum<T>> T fromValue(String value, Class<T> type, SoftEnumCreator<T> creator)
    {
        Map<String, SoftEnum<?>> softEnumMap = REGISTRY.get(type);
        if (softEnumMap != null && softEnumMap.containsKey(value))
        {
            return (T) softEnumMap.get(value);
        }
        T t = creator.create(value);
        register(value, t, type);
        return t;
    }

    public static <T extends SoftEnum<T>> Collection<T> knownValues(Class<T> type)
    {
        Map<String, SoftEnum<?>> map = REGISTRY.get(type);
        if (map == null)
        {
            return List.of();
        }
        @SuppressWarnings("unchecked")
        Collection<T> values = (Collection<T>) map.values();
        return Collections.unmodifiableCollection(values);
    }

    public boolean isCustom()
    {
        return !REGISTRY.getOrDefault(this.getClass(), Map.of()).containsKey(value);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof SoftEnum<?> other && Objects.equals(this.value, other.value) && this.getClass() == other.getClass();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(value, getClass());
    }

    @Override
    public String toString()
    {
        return value;
    }

    @FunctionalInterface
    public interface SoftEnumCreator<T extends SoftEnum<T>>
    {
        T create(String value);
    }
}
