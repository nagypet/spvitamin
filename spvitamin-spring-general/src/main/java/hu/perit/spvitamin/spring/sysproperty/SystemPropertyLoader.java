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

package hu.perit.spvitamin.spring.sysproperty;

import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.resource.Resources;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.AbstractEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SystemPropertyLoader
{
    public static void setAdditionalSystemProperties()
    {
        String activeProfiles = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);

        Properties sysProperties = new Properties();
        // Try loading standard sysproperties
        sysProperties.putAll(tryGetSysPropertiesFromFile("application.sysproperties"));
        sysProperties.putAll(tryGetSysPropertiesFromFile("config/application.sysproperties"));

        // Try loading profile-specific sysproperties
        List<String> profiles = new ArrayList<>();
        if (StringUtils.isNotBlank(activeProfiles))
        {
            profiles = Arrays.stream(activeProfiles.split(",")).distinct().map(i -> StringUtils.strip(i)).toList();
        }
        if (!profiles.isEmpty())
        {
            for (String profile : profiles)
            {
                sysProperties.putAll(tryGetSysPropertiesFromFile(profile + ".sysproperties"));
                sysProperties.putAll(tryGetSysPropertiesFromFile("config/" + profile + ".sysproperties"));
            }
        }
        else
        {
            // Try loading default sysproperties
            sysProperties.putAll(tryGetSysPropertiesFromFile("default.sysproperties"));
            sysProperties.putAll(tryGetSysPropertiesFromFile("config/default.sysproperties"));
        }

        // Adding loaded sysproperties
        for (Map.Entry<Object, Object> entry : sysProperties.entrySet())
        {
            log.info("Setting property: {}: {}", entry.getKey(), entry.getValue());
            System.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
    }


    private static Properties tryGetSysPropertiesFromFile(String filepath)
    {
        log.info("Trying to load system properties from {}", filepath);
        try (InputStream inputStream = Resources.getResourceAsInputStream(filepath))
        {
            Properties properties = new Properties();
            properties.load(inputStream);
            log.info("Additional system properties loaded from {}: {}", filepath, properties);
            return properties;
        }
        catch (IOException | ResourceNotFoundException | RuntimeException e)
        {
            return new Properties();
        }
    }
}
