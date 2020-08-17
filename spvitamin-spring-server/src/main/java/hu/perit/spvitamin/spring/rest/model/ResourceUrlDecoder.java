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

package hu.perit.spvitamin.spring.rest.model;

import lombok.Getter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * file:/C:/dev_new/OTP/OnBase/builds/dev/EventLogService/build/distributions/java/EventLogService/EventLogService-1.2.0-SNAPSHOT.jar!/META-INF/MANIFEST.MF
 * file:/C:/dev_new/OTP/OnBase/builds/dev/EventLogService/build/distributions/java/EventLogService/EventLogService-1.2.0-SNAPSHOT.jar!/BOOT-INF/classes!/META-INF/MANIFEST.MF
 * file:/C:/dev_new/OTP/OnBase/builds/dev/EventLogService/build/distributions/java/EventLogService/EventLogService-1.2.0-SNAPSHOT.jar!/BOOT-INF/lib/micrometer-registry-prometheus-1.3.6.jar!/META-INF/MANIFEST.MF
 *
 * @author Peter Nagy
 */

@Getter
public class ResourceUrlDecoder {

    private static final String FILE_PREFIX = "file:";
    private List<String> location = new ArrayList<>();
    private boolean valid = false;

    public ResourceUrlDecoder(URL url) {
        if (url.getProtocol().equalsIgnoreCase("jar")) {
            String path = url.getPath();
            if (path.startsWith(FILE_PREFIX)) {
                path = path.substring(FILE_PREFIX.length());
            }

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            this.location = Arrays.stream(path.split("!")).collect(Collectors.toList());

            valid = true;
        }
    }
}
