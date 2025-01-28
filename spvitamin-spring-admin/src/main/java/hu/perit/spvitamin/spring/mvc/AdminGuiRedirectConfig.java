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

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import hu.perit.spvitamin.spring.config.AdminProperties;
import hu.perit.spvitamin.spring.config.SysConfig;

@Configuration
public class AdminGuiRedirectConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // adminProperties.getAdminGuiUrl() must be e.g.: /admin-gui
        AdminProperties adminProperties = SysConfig.getAdminProperties();

        if (!adminProperties.getDefaultSiteUrl().isBlank()) {
            String target = String.format("redirect:%s/%s", adminProperties.getDefaultSiteUrl(), adminProperties.getDefaultSiteRootFileName());

            registry.addViewController("/").setViewName(target);

            registry.addViewController(adminProperties.getDefaultSiteUrl()).setViewName(target);
            registry.addViewController(adminProperties.getDefaultSiteUrl() + "/").setViewName(target);
        }

        if (!adminProperties.getAdminGuiUrl().isBlank()) {
            String target = String.format("redirect:%s/%s", adminProperties.getAdminGuiUrl(), adminProperties.getAdminGuiRootFileName());

            registry.addViewController(adminProperties.getAdminGuiUrl()).setViewName(target);
            registry.addViewController(adminProperties.getAdminGuiUrl() + "/").setViewName(target);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        AdminProperties adminProperties = SysConfig.getAdminProperties();
        if (adminProperties.getDefaultSiteStaticContentsPath() != null) {
            registry
                    .addResourceHandler(adminProperties.getDefaultSiteUrl() + "/**")
                    .addResourceLocations(
                            !adminProperties.getDefaultSiteStaticContentsPath().endsWith("/") ?
                            adminProperties.getDefaultSiteStaticContentsPath() + "/" :
                            adminProperties.getDefaultSiteStaticContentsPath()
                    );
        }
    }

}
