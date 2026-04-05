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
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.ConfigFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpvitaminObjectMapper
{
    public enum MapperType
    {
        JSON, YAML
    }

    private static final SingletonFactory<JsonMapper> jsonMapperFactory = SingletonFactory.of(() -> createNewMapper(JsonMapper.class));
    private static final SingletonFactory<YAMLMapper> yamlMapperFactory = SingletonFactory.of(() -> createNewMapper(YAMLMapper.class));


    // Use getJsonMapper() or getYamlMapper() instead of this method!
    @Deprecated
    public static ObjectMapper createMapper(MapperType type)
    {
        return switch (type)
        {
            case JSON -> jsonMapperFactory.getInstance();
            case YAML -> yamlMapperFactory.getInstance();
        };
    }


    public static JsonMapper getJsonMapper()
    {
        return jsonMapperFactory.getInstance();
    }


    public static YAMLMapper getYamlMapper()
    {
        return yamlMapperFactory.getInstance();
    }


    public static void addModule(JacksonModule module)
    {
        CustomSettings.addModule(module);
        jsonMapperFactory.renew();
        yamlMapperFactory.renew();
    }


    public static <T> void registerAbstractType(Class<T> api, Class<? extends T> impl)
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


    public static void addPackage(String prefix)
    {
        CustomSettings.addAdditionalPolymorphicSubtypePrefix(prefix);
        jsonMapperFactory.renew();
        yamlMapperFactory.renew();
    }


    public static void addConfigFeature(ConfigFeature configFeature, Boolean value)
    {
        CustomSettings.addConfigFeature(configFeature, value);
        jsonMapperFactory.renew();
        yamlMapperFactory.renew();
    }


    @SuppressWarnings("unchecked")
    public static <T> T createNewMapper(Class<T> type)
    {
        if (type.equals(JsonMapper.class))
        {
            return (T) jsonMapperBuilderWithDefaults().build();
        }
        else if (type.equals(YAMLMapper.class))
        {
            return (T) yamlMapperBuilderWithDefaults().build();
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }


    public static JsonMapper.Builder jsonMapperBuilderWithDefaults()
    {
        JsonMapper.Builder builder = JsonMapper.builderWithJackson2Defaults()
                .polymorphicTypeValidator(CustomSettings.getPolymorphicTypeValidator())
                .configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION, true)
                .configure(MapperFeature.USE_GETTERS_AS_SETTERS, true)
                .addAbstractTypeResolver(CustomSettings.getAbstractTypeResolver())
                .addModule(new SpvitaminJsonTimeModul())
                .addModules(CustomSettings.getAdditionalModules())
                .registerSubtypes(CustomSettings.getSubtypes());

        for (Map.Entry<ConfigFeature, Boolean> configFeatureBooleanEntry : CustomSettings.getConfigFeatures().entrySet())
        {
            if (configFeatureBooleanEntry.getKey() instanceof SerializationFeature serializationFeature)
            {
                builder.configure(serializationFeature, configFeatureBooleanEntry.getValue());
            }
            else if (configFeatureBooleanEntry.getKey() instanceof DeserializationFeature deserializationFeature)
            {
                builder.configure(deserializationFeature, configFeatureBooleanEntry.getValue());
            }
        }

        return builder;
    }


    public static YAMLMapper.Builder yamlMapperBuilderWithDefaults()
    {
        YAMLMapper.Builder builder = YAMLMapper.builder(new YAMLFactory())
                .polymorphicTypeValidator(CustomSettings.getPolymorphicTypeValidator())
                .configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION, true)
                .configure(MapperFeature.USE_GETTERS_AS_SETTERS, true)
                .addAbstractTypeResolver(CustomSettings.getAbstractTypeResolver())
                .addModule(new SpvitaminJsonTimeModul())
                .addModules(CustomSettings.getAdditionalModules())
                .registerSubtypes(CustomSettings.getSubtypes());

        for (Map.Entry<ConfigFeature, Boolean> configFeatureBooleanEntry : CustomSettings.getConfigFeatures().entrySet())
        {
            if (configFeatureBooleanEntry.getKey() instanceof SerializationFeature serializationFeature)
            {
                builder.configure(serializationFeature, configFeatureBooleanEntry.getValue());
            }
            else if (configFeatureBooleanEntry.getKey() instanceof DeserializationFeature deserializationFeature)
            {
                builder.configure(deserializationFeature, configFeatureBooleanEntry.getValue());
            }
        }

        return builder;
    }

}
