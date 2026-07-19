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

package hu.perit.spvitamin.spring.data.autoconfiguration;

import hu.perit.spvitamin.spring.data.pessimistic.PessimisticJpaRepository;
import hu.perit.spvitamin.spring.data.pessimistic.PessimisticJpaRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

import java.util.List;
import java.util.Map;

/**
 * Automatically registers {@link PessimisticJpaRepositoryImpl} as the base class for every
 * JPA repository that extends {@link PessimisticJpaRepository}, without requiring
 * {@code repositoryBaseClass = PessimisticJpaRepositoryImpl.class} in each
 * {@code @EnableJpaRepositories} annotation.
 */
@AutoConfiguration
@Slf4j
public class SpvitaminSpringDataAutoconfiguration implements BeanDefinitionRegistryPostProcessor
{
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException
    {
        for (String beanName : registry.getBeanDefinitionNames())
        {
            BeanDefinition bd = registry.getBeanDefinition(beanName);
            if (!JpaRepositoryFactoryBean.class.getName().equals(bd.getBeanClassName()))
            {
                continue;
            }

            Map<Integer, ConstructorArgumentValues.ValueHolder> args = bd.getConstructorArgumentValues().getIndexedArgumentValues();
            if (args.isEmpty())
            {
                continue;
            }

            Class<?> repositoryInterface = resolveClass(args.get(0).getValue());
            if (repositoryInterface == null || !PessimisticJpaRepository.class.isAssignableFrom(repositoryInterface))
            {
                continue;
            }

            if (bd.getPropertyValues().getPropertyValue("repositoryBaseClass") == null)
            {
                log.info("Registering PessimisticJpaRepositoryImpl as base class for repository {}", beanName);
                bd.getPropertyValues().add("repositoryBaseClass", PessimisticJpaRepositoryImpl.class);
            }
        }
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
    }


    private static Class<?> resolveClass(Object value)
    {
        if (value instanceof Class<?> cls)
        {
            return cls;
        }
        if (value instanceof String name)
        {
            try
            {
                return Class.forName(name);
            }
            catch (ClassNotFoundException ignored)
            {
                return null;
            }
        }
        return null;
    }
}
