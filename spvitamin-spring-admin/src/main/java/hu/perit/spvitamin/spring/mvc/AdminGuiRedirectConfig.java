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

package hu.perit.spvitamin.spring.mvc;

import hu.perit.spvitamin.spring.config.AdminProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class AdminGuiRedirectConfig implements WebMvcConfigurer
{
    @Override
    public void addViewControllers(ViewControllerRegistry registry)
    {
        // adminProperties.getAdminGuiUrl() must be e.g.: /admin-gui
        AdminProperties adminProperties = SysConfig.getAdminProperties();

        List<AdminProperties.SiteConfig> sites = adminProperties.getSites();
        for (int i = 0; i < sites.size(); i++)
        {
            AdminProperties.SiteConfig site = sites.get(i);
            String target = String.format("redirect:%s/%s", site.getUrl(), site.getFilename());
            if (i == 0)
            {
                // The first site is the default site
                registry.addViewController("/").setViewName(target);
            }
            registry.addViewController(site.getUrl()).setViewName(target);
            registry.addViewController(site.getUrl() + "/").setViewName(target);
        }
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        AdminProperties adminProperties = SysConfig.getAdminProperties();
        AdminProperties.SiteConfig defaultSiteConfig = adminProperties.getSites().getFirst();
        if (StringUtils.isNotBlank(defaultSiteConfig.getStaticContentsPath()))
        {
            registry
                    .addResourceHandler(defaultSiteConfig.getUrl() + "/**")
                    .addResourceLocations(!defaultSiteConfig.getStaticContentsPath().endsWith("/")
                            ? defaultSiteConfig.getStaticContentsPath() + "/"
                            : defaultSiteConfig.getStaticContentsPath()
                    );
        }
    }

}
