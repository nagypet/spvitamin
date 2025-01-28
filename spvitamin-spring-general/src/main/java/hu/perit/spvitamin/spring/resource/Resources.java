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

package hu.perit.spvitamin.spring.resource;

import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Resources
{
    public static Path getResourcePath(String resourcePath) throws ResourceNotFoundException
    {
        String baseDir = System.getProperty("user.dir");
        Path configPath = Paths.get(baseDir, resourcePath).toAbsolutePath();
        if (configPath.toFile().exists())
        {
            return configPath;
        }

        // Trying to load from the classpath
        try
        {
            File resource = new ClassPathResource(resourcePath).getFile();
            return Paths.get(resource.getAbsolutePath()).toAbsolutePath();
        }
        catch (IOException e)
        {
            throw new ResourceNotFoundException(MessageFormat.format("Resource not found: {0}", resourcePath), e);
        }
    }
}
