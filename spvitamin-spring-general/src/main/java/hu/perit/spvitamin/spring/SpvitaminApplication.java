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
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.AbstractEnvironment;

import java.util.StringJoiner;

public class SpvitaminApplication extends SpringApplication
{
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
        return run(new Class<?>[] {primarySource}, args);
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
        // setAdditionalProfiles is not working with SpringBoot 2.4.5
        //this.setAdditionalProfiles("spvitamin-defaults");
        String activeProfiles = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
        if (StringUtils.isBlank(activeProfiles))
        {
            activeProfiles = "default";
        }
        StringJoiner sj = new StringJoiner(",");
        sj.add(activeProfiles).add("spvitamin-defaults");
        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, sj.toString());
        this.addListeners(new EnvironmentPostProcessor());
    }
}
