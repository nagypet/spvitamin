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

package hu.perit.spvitamin.spring;

import hu.perit.spvitamin.spring.environment.EnvironmentPostProcessor;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.resource.Resources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.AbstractEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SpvitaminApplication extends SpringApplication
{

    public static final String SPVITAMIN_DEFAULTS = "spvitamin-defaults";


    public static ConfigurableApplicationContext run(Class<?> primarySources, String[] args)
    {
        SpringApplication springApplication = new SpvitaminApplication(primarySources);
        return springApplication.run(args);
    }


    public static ConfigurableApplicationContext run(Class<?> primarySources, String[] args, ApplicationContextInitializer<?>... initializers)
    {
        SpringApplication springApplication = new SpvitaminApplication(primarySources);
        springApplication.addInitializers(initializers);
        return springApplication.run(args);
    }


    public SpvitaminApplication(Class<?>... primarySources)
    {
        super(primarySources);

        log.info("Environment variables:");
        Map<String, String> env = System.getenv();
        env.keySet().stream().sorted().forEach(i -> log.info("{}={}", i, env.get(i)));

        setAdditionalProfiles();

        setAdditionalSystemProperties();

        this.addListeners(new EnvironmentPostProcessor());
    }


    private static void setAdditionalProfiles()
    {
        // Setting additional profiles
        String activeProfiles = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
        if (StringUtils.isBlank(activeProfiles))
        {
            // if there is no set profile
            activeProfiles = Objects.requireNonNullElse(getHostBasedProfiles(), "default");
        }
        StringJoiner sj = new StringJoiner(",");
        sj.add(activeProfiles).add(SPVITAMIN_DEFAULTS);
        String hostname = getHostName();
        if (hostname != null)
        {
            sj.add(hostname);
        }

        log.info("Setting property: {}: {}", AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, sj.toString());
        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, sj.toString());
    }


    private static String getHostBasedProfiles()
    {
        List<String> profiles = new ArrayList<>();
        String hostname = getHostName();
        if (hostname != null)
        {
            profiles.addAll(tryGetProfilesFromFile(hostname + ".profiles"));
            profiles.addAll(tryGetProfilesFromFile("config/" + hostname + ".profiles"));
        }

        if (profiles.isEmpty())
        {
            // If there is no host based profile => load the defaults
            profiles.addAll(tryGetProfilesFromFile("default.profiles"));
            profiles.addAll(tryGetProfilesFromFile("config/default.profiles"));
        }

        if (profiles.isEmpty())
        {
            profiles.add("default");
        }

        return profiles.stream().distinct().collect(Collectors.joining(","));
    }


    private static List<String> tryGetProfilesFromFile(String filepath)
    {
        log.info("Trying to load profiles from {}", filepath);
        try (InputStream inputStream = Resources.getResourceAsInputStream(filepath))
        {
            String activeProfiles = StringUtils.strip(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
            log.info("Additional profiles loaded from {}: '{}'", filepath, activeProfiles);
            if (!StringUtils.isBlank(activeProfiles))
            {
                return Stream.of(activeProfiles.split(","))
                        .map(i -> StringUtils.strip(i))
                        .filter(i -> !i.startsWith("#"))
                        .toList();
            }
        }
        catch (IOException | ResourceNotFoundException | RuntimeException e)
        {
            log.warn("Error loading resource from {}: {}", filepath, e.getMessage());
        }
        return Collections.emptyList();
    }


    private static String getHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e)
        {
            return "unknown-host";
        }
    }


    private static void setAdditionalSystemProperties()
    {
        String activeProfiles = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
        if (StringUtils.isNotBlank(activeProfiles))
        {
            Properties sysProperties = new Properties();
            // Try loading standard sysproperties
            sysProperties.putAll(tryGetSysPropertiesFromFile("application.sysproperties"));
            sysProperties.putAll(tryGetSysPropertiesFromFile("config/application.sysproperties"));

            // Try loading profile-specific sysproperties
            List<String> profiles = Arrays.stream(activeProfiles.split(",")).distinct().map(i -> StringUtils.strip(i)).toList();
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
