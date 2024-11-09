package hu.perit.spvitamin.test.spring;

import hu.perit.spvitamin.spring.environment.SpringEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class SpvitaminTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        SpringEnvironment.setEnvironment(applicationContext.getEnvironment());
    }
}
