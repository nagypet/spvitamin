package hu.perit.spvitamin.spring.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminGuiRedirectConfig implements WebMvcConfigurer
{
    private static final String REDIRECT_TARGET = "redirect:/admin-gui/";

    @Override
    public void addViewControllers(ViewControllerRegistry registry)
    {
        registry.addViewController("/admin-gui").setViewName(REDIRECT_TARGET);
        registry.addViewController("/admin-gui/").setViewName("forward:/admin-gui/index.html");
    }
}
