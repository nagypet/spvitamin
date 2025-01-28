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

package hu.perit.spvitamin.spring.environment;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.keystore.KeystoreUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

/**
 * A utility class for post processing some properties. This must be done just
 * after the property sources have been loaded but before any further
 * initialization of our SpringApplication. The loaded environment will be
 * stored in a singleton, so it can be used for initializing beans, where
 * autowired values would not be ready for use.
 */

@Slf4j
public class EnvironmentPostProcessor implements ApplicationListener<ApplicationEvent>
{

    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ApplicationPreparedEvent applicationPreparedEvent)
        {
            this.onApplicationPreparedEvent(applicationPreparedEvent);
        }
        else if (event instanceof ContextRefreshedEvent contextRefreshedEvent)
        {
            this.onContextRefreshedEvent(contextRefreshedEvent);
        }
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event)
    {
        log.info("ContextRefreshedEvent occured");
    }


    private void onApplicationPreparedEvent(ApplicationPreparedEvent event)
    {
        if (!event.getApplicationContext().isActive())
        {
            try
            {
                // Initializing a singleton object with the current Environment
                ConfigurableEnvironment env = event.getApplicationContext().getEnvironment();
                SpringEnvironment.setEnvironment(env);

                KeystoreUtils.locateJksStores();

                env.getPropertySources().forEach(this::dumpPropertySource);
            }
            catch (RuntimeException ex)
            {
                log.error(StackTracer.toString(ex));
                throw ex;
            }
        }
    }

    private void dumpPropertySource(PropertySource<?> ps)
    {
        if (ps instanceof OriginTrackedMapPropertySource propertySource)
        {
            log.info(propertySource.getName());
        }
    }
}
