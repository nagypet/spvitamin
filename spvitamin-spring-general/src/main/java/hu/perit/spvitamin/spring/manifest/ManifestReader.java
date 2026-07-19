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

package hu.perit.spvitamin.spring.manifest;

import hu.perit.spvitamin.core.StackTracer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

@UtilityClass
@Slf4j
public class ManifestReader
{

    public static Properties getManifestAttributes(String applicationName)
    {
        Objects.requireNonNull(applicationName, "applicationName");
        Properties prop = new Properties();
        Enumeration<URL> resources;
        try
        {
            resources = ManifestReader.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements())
            {
                URL url = resources.nextElement();
                try (InputStream manifestStream = url.openStream())
                {
                    prop.load(manifestStream);
                    if (matchesApplication(prop, applicationName))
                    {
                        ResourceUrlDecoder resourceUrl = new ResourceUrlDecoder(url);
                        log.info(String.format("Manifest loaded from '%s'", resourceUrl.getLocation().getFirst()));
                        return prop;
                    }
                }
            }
        }
        catch (IOException ex)
        {
            log.error(StackTracer.toString(ex));
        }

        return new Properties();
    }


    private static boolean matchesApplication(Properties prop, String applicationName)
    {
        return Strings.CI.equals(prop.getProperty("Implementation-Title"), applicationName);
    }
}
