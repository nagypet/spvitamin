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

import hu.perit.spvitamin.core.singleton.SingletonFactory;
import hu.perit.spvitamin.json.time.SpvitaminJsonTimeModul;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpvitaminObjectMapper
{
    public enum MapperType
    {
        JSON, YAML
    }

    private static final SingletonFactory<ObjectMapper> jsonMapperFactory = SingletonFactory.of(() -> internalCreateMapper(MapperType.JSON));
    private static final SingletonFactory<ObjectMapper> yamlMapperFactory = SingletonFactory.of(() -> internalCreateMapper(MapperType.YAML));


    public static ObjectMapper createMapper(MapperType type)
    {
        return switch (type)
        {
            case JSON -> jsonMapperFactory.getInstance();
            case YAML -> yamlMapperFactory.getInstance();
        };
    }


    public static void addModule(JacksonModule module)
    {
        CustomSettings.addModule(module);
        jsonMapperFactory.renew();
        yamlMapperFactory.renew();
    }


    public static void registerAbstractType(Class<?> api, Class<?> impl)
    {
        CustomSettings.registerAbstractType(api, impl);
        jsonMapperFactory.renew();
        yamlMapperFactory.renew();
    }


    public static void registerSubtypes(List<NamedType> namedTypes)
    {
        CustomSettings.registerSubtypes(namedTypes);
        jsonMapperFactory.renew();
        yamlMapperFactory.renew();
    }


    static ObjectMapper internalCreateMapper(MapperType type)
    {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("hu.perit.")
                .allowIfSubType("java.")
                .build();

        if (type == MapperType.JSON)
        {
            return JsonMapper.builderWithJackson2Defaults()
                    .polymorphicTypeValidator(ptv)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                    .addAbstractTypeResolver(CustomSettings.getAbstractTypeResolver())
                    .addModule(new SpvitaminJsonTimeModul())
                    .addModules(CustomSettings.getAdditionalModules())
                    .registerSubtypes(CustomSettings.getSubtypes())
                    .build();
        }

        return YAMLMapper.builder(new YAMLFactory())
                .polymorphicTypeValidator(ptv)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .addAbstractTypeResolver(CustomSettings.getAbstractTypeResolver())
                .addModule(new SpvitaminJsonTimeModul())
                .addModules(CustomSettings.getAdditionalModules())
                .registerSubtypes(CustomSettings.getSubtypes())
                .build();
    }
}
