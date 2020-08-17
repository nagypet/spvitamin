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

import hu.perit.spvitamin.core.exception.UnexpectedConditionException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * #know-how:access-spring-managed-beans-from-outside
 *
 * @author Peter Nagy
 */


@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        // store ApplicationContext reference to access required beans later on
        SpringContext.context = context;
    }


    /**
     * Returns the Spring managed bean instance of the given class type if it exists.
     * Returns null otherwise.
     *
     * @param beanClass
     * @return
     */
    public static <T> T getBean(Class<T> beanClass) {
        validateSelf();
        return context.getBean(beanClass);
    }

    public static <T> T getBean(String name, Class<T> beanClass) {
        validateSelf();
        return context.getBean(name, beanClass);
    }

    public static String[] getBeanNamesForType(Class<?> beanClass) {
        validateSelf();
        return context.getBeanNamesForType(beanClass);
    }

    public static boolean isBeanAvailable(Class<?> beanClass) {
        validateSelf();
        String[] names = context.getBeanNamesForType(beanClass);
        return names.length > 0;
    }

    private static void validateSelf() {
        if (SpringContext.context == null) {
            throw new UnexpectedConditionException("There is no injected ApplicationContext!");
        }
    }
}