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

package hu.perit.spvitamin.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */


@Data
@Component
@ConfigurationProperties(prefix = "admin")
@Slf4j
public class AdminProperties
{
    private String defaultSiteUrl = "";
    private String defaultSiteRootFileName = "index.html";

    // e.g. admin.admin-gui-url=/alma
    private String adminGuiUrl = "";
    private String adminGuiRootFileName = "index.html";

    @EventListener
    private void onApplicationEvent(ContextStartedEvent event)
    {
        ServerProperties serverProperties = SysConfig.getServerProperties();
        log.info(
            String.format("Default site: %s%s/%s", serverProperties.getServiceUrl(), this.defaultSiteUrl, this.defaultSiteRootFileName));
        log.info(String.format("AdminGUI: %s%s/%s", serverProperties.getServiceUrl(), this.adminGuiUrl, this.adminGuiRootFileName));
    }
}
