package hu.perit.spvitamin.spring.json;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class JsonSubtypeAutoconfigurationRegistrar implements ImportBeanDefinitionRegistrar
{
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry)
    {
        // Kivesszük az annotáció értékeit
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableJsonSubtypeAutoconfiguration.class.getName());
        if (attributes != null)
        {
            String[] basePackages = (String[]) attributes.get("scan");

            // Inicializáljuk a TypeRegistry-t a megadott csomagok alapján
            TypeRegistry.autoRegisterTypes(basePackages);
        }
    }
}
