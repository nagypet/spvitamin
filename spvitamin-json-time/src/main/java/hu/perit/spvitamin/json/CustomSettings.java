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

package hu.perit.spvitamin.json;

import hu.perit.spvitamin.core.typehelpers.MapUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.AbstractTypeResolver;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.module.SimpleAbstractTypeResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomSettings
{
    private static final Map<Class<?>, Class<?>> ABSTRACT_TYPE_MAPPINGS = new ConcurrentHashMap<>();
    private static final Map<String, JacksonModule> MODULES = new ConcurrentHashMap<>();
    private static final Map<String, NamedType> SUBTYPES = new ConcurrentHashMap<>();


    static AbstractTypeResolver getAbstractTypeResolver()
    {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        if (!ABSTRACT_TYPE_MAPPINGS.isEmpty())
        {
            ABSTRACT_TYPE_MAPPINGS.forEach((api, impl) -> addMapping(resolver, api, impl));
        }

        return resolver;
    }


    @SuppressWarnings("unchecked")
    private static <T> void addMapping(SimpleAbstractTypeResolver resolver, Class<?> api, Class<?> impl)
    {
        resolver.addMapping((Class<T>) api, (Class<? extends T>) impl);
    }


    static <T> void registerAbstractType(Class<T> api, Class<? extends T> impl)
    {
        ABSTRACT_TYPE_MAPPINGS.put(api, impl);
    }


    static void addModule(JacksonModule module)
    {
        MODULES.put(module.getModuleName(), module);
    }


    public static List<JacksonModule> getAdditionalModules()
    {
        return MODULES.isEmpty() ? new ArrayList<>() : new ArrayList<>(MODULES.values());
    }


    public static void registerSubtypes(List<NamedType> namedTypes)
    {
        SUBTYPES.putAll(MapUtils.toMap(namedTypes, NamedType::getName));
    }


    public static NamedType[] getSubtypes()
    {
        return SUBTYPES.values().toArray(new NamedType[0]);
    }
}
