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

package hu.perit.spvitamin.core.reflection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;

@Slf4j
class ReflectionUtilsTest
{
    private static class TestBaseClass
    {
        public String publicBaseProperty = "publicBaseProperty";
        private String privateBaseWithoutGetter = "privateBaseWithoutGetter";
        @Getter
        private String privateBaseWithGetter = "privateBaseWithGetter";

        @Getter(AccessLevel.PRIVATE)
        private String privateBaseGetter = "privateBaseGetter";

        private static String privateStaticBaseWithoutGetter = "privateStaticBaseWithoutGetter";
        @Getter
        private static String privateStaticBaseWithGetter = "privateStaticBaseWithGetter";
    }


    private static class TestClass extends TestBaseClass
    {
        public String publicProperty = "publicProperty";
        private String privateWithoutGetter = "privateWithoutGetter";

        @Getter
        private String privateWithGetter = "privateWithGetter";

        @Getter(AccessLevel.PRIVATE)
        private String privateGetter = "privateGetter";

        private static String privateStaticWithoutGetter = "privateStaticWithoutGetter";

        @Getter
        private static String privateStaticWithGetter = "privateStaticWithGetter";

        public String getSomething()
        {
            return "something";
        }
    }


    @Test
    void testAllPropertiesOfWithoutPrivate() throws InvocationTargetException, IllegalAccessException
    {
        List<Property> properties = ReflectionUtils.allPropertiesOf(TestClass.class, false);
        log.debug(properties.toString());
        Assertions.assertThat(properties).hasSize(5);

        TestClass testClass = new TestClass();
        dumpProperties(testClass, properties);
    }


    private void dumpProperties(Object object, List<Property> properties) throws InvocationTargetException, IllegalAccessException
    {
        for (Property property : properties)
        {
            log.debug(MessageFormat.format("{0}: {1}", property.getName(), property.get(object)));
        }
    }


    @Test
    void testAllPropertiesOfWithPrivate() throws InvocationTargetException, IllegalAccessException
    {
        List<Property> properties = ReflectionUtils.allPropertiesOf(TestClass.class, true);
        log.debug(properties.toString());
        Assertions.assertThat(properties).hasSize(9);

        TestClass testClass = new TestClass();
        dumpProperties(testClass, properties);
    }


    @Test
    void testPropertiesOfWithoutPrivate() throws InvocationTargetException, IllegalAccessException
    {
        List<Property> properties = ReflectionUtils.propertiesOf(TestClass.class, false);
        log.debug(properties.toString());
        Assertions.assertThat(properties).hasSize(3);

        TestClass testClass = new TestClass();
        dumpProperties(testClass, properties);
    }


    @Test
    void testPropertiesOfWithPrivate() throws InvocationTargetException, IllegalAccessException
    {
        List<Property> properties = ReflectionUtils.propertiesOf(TestClass.class, true);
        log.debug(properties.toString());
        Assertions.assertThat(properties).hasSize(5);

        TestClass testClass = new TestClass();
        dumpProperties(testClass, properties);
    }

}
