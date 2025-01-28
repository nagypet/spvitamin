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
