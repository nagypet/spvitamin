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

package hu.perit.spvitamin.spring.info;

import hu.perit.spvitamin.spring.manifest.ManifestReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class CustomInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Properties manifest = ManifestReader.getManifestAttributes();
            String name = manifest.getProperty("Implementation-Title", "Application Title");
        String version = manifest.getProperty("Implementation-Version", "");
        //String svnVersion = manifest.getProperty("SVN-REVISION", "");
        String buildTime = manifest.getProperty("Build-Time", "");
        String build;
        if (StringUtils.isNoneBlank(buildTime)) {
            build = String.format("%s", buildTime);
        }
        else {
            build = "Started from IDE, no build info is available!";
        }

        builder.withDetail("name", name);
        builder.withDetail("version", version);
        builder.withDetail("build", build);
    }
}
