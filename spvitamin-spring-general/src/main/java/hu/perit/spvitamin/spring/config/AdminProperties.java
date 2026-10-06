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
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Nagy
 */


@Data
@Component
@ConfigurationProperties(prefix = "admin")
@Slf4j
@Validated
public class AdminProperties
{
    @Autowired
    @Getter(AccessLevel.NONE)
    private ServerProperties serverProperties;

    // Legacy site configuration
    @Getter(AccessLevel.NONE)
    private String defaultSiteUrl;
    @Getter(AccessLevel.NONE)
    private String defaultSiteRootFileName;
    @Getter(AccessLevel.NONE)
    private String defaultSiteStaticContentsPath;

    // e.g. admin.admin-gui-url=/alma
    @Getter(AccessLevel.NONE)
    private String adminGuiUrl;
    @Getter(AccessLevel.NONE)
    private String adminGuiRootFileName;

    // New site configuration
    @Setter(AccessLevel.NONE)
    private List<SiteConfig> sites = new ArrayList<>();


    public SiteConfig getDefaultSiteConfig()
    {
        return this.sites.getFirst();
    }


    // This string will be displayed in the footer of the AdminGUI
    private String copyright = "Peter Nagy - nagy.peter.home@gmail.com; peter.nagy@perit.hu";

    // If set to false, the Keystore and Truststore menus are disabled in the AdminGUI. This is useful in the case of
    // a Kubernetes or Openshift deployment, where the app does not manage certificates.
    private Boolean keystoreAdminEnabled = true;


    @PostConstruct
    private void postConstruct()
    {
        List<SiteConfig> newSites = new ArrayList<>();
        if (StringUtils.isNotBlank(this.defaultSiteUrl))
        {
            newSites.add(SiteConfig.of(this.defaultSiteUrl, this.defaultSiteRootFileName, this.defaultSiteStaticContentsPath));
        }
        if (StringUtils.isNotBlank(this.adminGuiUrl))
        {
            newSites.add(SiteConfig.of(this.adminGuiUrl, this.adminGuiRootFileName));
        }
        newSites.addAll(this.sites);
        if (newSites.stream().noneMatch(site -> site.getUrl().equals("/admin-gui")))
        {
            newSites.add(SiteConfig.of("/admin-gui", "index.html"));
        }
        this.sites = newSites;
        this.sites.forEach(site -> log.info(String.format("Site: %s%s/%s", serverProperties.getServiceUrl(), site.getUrl(), site.getFilename())));
    }


    public String getKeystoreAdminEnabled()
    {
        return Boolean.toString(BooleanUtils.isTrue(this.keystoreAdminEnabled));
    }


    @Data
    public static class SiteConfig
    {
        @NotBlank
        private final String url;
        @NotBlank
        private final String filename;
        private final String staticContentsPath;


        public static SiteConfig of(String url, String filename)
        {
            return new SiteConfig(url, filename, null);
        }


        public static SiteConfig of(String url, String filename, String staticContentsPath)
        {
            return new SiteConfig(url, filename, staticContentsPath);
        }
    }
}
