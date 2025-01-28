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

package hu.perit.spvitamin.spring.config;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Peter Nagy
 */


@Data
@Component
@ConfigurationProperties(prefix = "admin")
@Slf4j
public class AdminProperties
{
    @Autowired
    @Getter(AccessLevel.NONE)
    private ServerProperties serverProperties;

    private String defaultSiteUrl = "/admin-gui";
    private String defaultSiteRootFileName = "";
    private String defaultSiteStaticContentsPath;

    // e.g. admin.admin-gui-url=/alma
    private String adminGuiUrl = "/admin-gui";
    private String adminGuiRootFileName = "index.html";

    // This string will be displayen in the footer of the AdminGUI
    private String copyright = "Peter Nagy - nagy.peter.home@gmail.com; peter.nagy@perit.hu";

    // If set to false, the Keystore and Truststore menus are disabled in the AdminGUI. This is useful in case of
    // a Kubernetes or Openshift deployment, where certificates are not managed by the app.
    private Boolean keystoreAdminEnabled = true;


    @PostConstruct
    private void postConstruct()
    {
        log.info(String.format("Default site: %s%s/%s", serverProperties.getServiceUrl(), this.defaultSiteUrl,
            this.defaultSiteRootFileName));
        log.info(String.format("AdminGUI: %s%s/%s", serverProperties.getServiceUrl(), this.adminGuiUrl, this.adminGuiRootFileName));
    }


    public String getKeystoreAdminEnabled()
    {
        return BooleanUtils.isTrue(this.keystoreAdminEnabled) ? "true" : "false";
    }
}
