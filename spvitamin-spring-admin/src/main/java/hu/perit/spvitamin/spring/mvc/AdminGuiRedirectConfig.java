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
                    .addResourceLocations(adminProperties.getDefaultSiteStaticContentsPath());
        }
    }

}
