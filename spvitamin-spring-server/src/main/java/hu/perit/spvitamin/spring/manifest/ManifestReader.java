/*
 * Copyright 2020-2020 the original author or authors.
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
import hu.perit.spvitamin.spring.rest.api.AdminController;
import hu.perit.spvitamin.spring.rest.model.ResourceUrlDecoder;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

@Log4j
public class ManifestReader {

    public static Properties getManifestAttributes()
    {
        Properties prop = new Properties();
        Enumeration<URL> resources;
        try
        {
            resources = AdminController.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements())
            {
                URL url = resources.nextElement();
                try (InputStream manifestStream = url.openStream())
                {
                    prop.load(manifestStream);
                    String name = prop.getProperty("Implementation-Vendor");
                    String type = prop.getProperty("Implementation-Type");
                    if (name != null && name.equalsIgnoreCase("perit.hu") && type != null && type.equalsIgnoreCase("Application")) {
                        ResourceUrlDecoder resourceUrl = new ResourceUrlDecoder(url);
                        log.debug(String.format("Manifest loaded from '%s'", resourceUrl.getLocation().get(0)));
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
}
