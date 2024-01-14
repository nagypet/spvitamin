/*
 * Copyright 2020-2024 the original author or authors.
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.AbstractEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SpvitaminApplication extends SpringApplication
{

    public static final String SPVITAMIN_DEFAULTS = "spvitamin-defaults";

    /**
     * Static helper that can be used to run a {@link SpringApplication} from the
     * specified source using default settings.
     *
     * @param primarySource the primary source to load
     * @param args          the application arguments (usually passed from a Java main method)
     * @return the running {@link ApplicationContext}
     */
    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args)
    {
        return run(new Class<?>[]{primarySource}, args);
    }


    /**
     * Static helper that can be used to run a {@link SpringApplication} from the
     * specified sources using default settings and user supplied arguments.
     *
     * @param primarySources the primary sources to load
     * @param args           the application arguments (usually passed from a Java main method)
     * @return the running {@link ApplicationContext}
     */
    public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args)
    {
        SpringApplication springApplication = new SpvitaminApplication(primarySources);
        return springApplication.run(args);
    }


    public SpvitaminApplication(Class<?>... primarySources)
    {
        super(primarySources);

        // Setting additional profiles
        String activeProfiles = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
        if (StringUtils.isBlank(activeProfiles))
        {
            activeProfiles = Objects.requireNonNullElse(getHostBasedProfiles(), "default");
        }
        StringJoiner sj = new StringJoiner(",");
        sj.add(activeProfiles).add(SPVITAMIN_DEFAULTS);
        String hostname = getHostName();
        if (hostname != null)
        {
            sj.add(hostname);
        }

        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, sj.toString());

        this.addListeners(new EnvironmentPostProcessor());
    }


    private static String getHostBasedProfiles()
    {
        List<String> profiles = new ArrayList<>();
        profiles.addAll(getHostBasedProfiles("default.profiles"));
        profiles.addAll(getHostBasedProfiles("config/default.profiles"));
        String hostname = getHostName();
        if (hostname != null)
        {
            profiles.addAll(getHostBasedProfiles(hostname + ".profiles"));
            profiles.addAll(getHostBasedProfiles("config/" + hostname + ".profiles"));
        }
        return profiles.stream().distinct().collect(Collectors.joining(","));
    }


    private static List<String> getHostBasedProfiles(String filepath)
    {
        try
        {
            log.info("Trying to load profiles from {}", filepath);
            Path activeProfilesPath = Resources.getResourcePath(filepath);
            InputStream inputStream = Files.newInputStream(activeProfilesPath);
            String activeProfiles = StringUtils.strip(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
            log.info("Additional profiles loaded from {}: {}", activeProfilesPath, activeProfiles);
            return List.of(activeProfiles.split(","));
        }
        catch (IOException | ResourceNotFoundException | RuntimeException e)
        {
            return Collections.emptyList();
        }
    }


    private static String getHostName()
    {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null)
        {
            return hostname.toLowerCase();
        }

        return StringUtils.toRootLowerCase(System.getenv("COMPUTERNAME"));
    }
}
