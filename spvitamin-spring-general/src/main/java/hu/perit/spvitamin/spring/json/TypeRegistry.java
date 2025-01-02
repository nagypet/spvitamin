package hu.perit.spvitamin.spring.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypeRegistry
{
    private static final Map<Class<?>, String> TYPES = new HashMap<>();

    public static void autoRegisterTypes(String... basePackages)
    {
        Reflections reflections = new Reflections((Object[]) basePackages);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(AutoregisterJsonType.class);

        for (Class<?> clazz : annotatedClasses)
        {
            AutoregisterJsonType annotation = clazz.getAnnotation(AutoregisterJsonType.class);
            TYPES.put(clazz, annotation.name());
        }
    }


    public static void registerTypesInMapper(ObjectMapper mapper)
    {
        TYPES.forEach((key, value) -> mapper.registerSubtypes(new NamedType(key, value)));
    }
}
