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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Slf4j
class ReflectionUtilsTest
{
    // Custom annotation for testing
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface TestAnnotation {
        String value() default "";
    }

    private interface TestInterface {
        @TestAnnotation("interface")
        String getInterfaceMethod();
    }

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

        @TestAnnotation("base")
        public String getOverriddenMethod() {
            return "base";
        }
    }


    private static class TestClass extends TestBaseClass implements TestInterface
    {
        public String publicProperty = "publicProperty";
        private String privateWithoutGetter = "privateWithoutGetter";

        @Getter
        private String privateWithGetter = "privateWithGetter";

        @Getter(AccessLevel.PRIVATE)
        private String privateGetter = "privateGetter";

        @Getter @Setter
        private boolean active = true;

        private static String privateStaticWithoutGetter = "privateStaticWithoutGetter";

        @Getter
        private static String privateStaticWithGetter = "privateStaticWithGetter";

        public String getSomething()
        {
            return "something";
        }

        @Override
        public String getOverriddenMethod() {
            return "child";
        }

        @Override
        public String getInterfaceMethod() {
            return "implemented";
        }

        public static String getStaticMethod() {
            return "static";
        }

        public void setProperty(String value) {
            this.publicProperty = value;
        }
    }


    @Test
    void testAllPropertiesOfWithoutPrivate() throws InvocationTargetException, IllegalAccessException
    {
        List<Property> properties = ReflectionUtils.allPropertiesOf(TestClass.class, false);
        log.debug(properties.toString());
        Assertions.assertThat(properties).hasSize(9);

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
        Assertions.assertThat(properties).hasSize(13);

        TestClass testClass = new TestClass();
        dumpProperties(testClass, properties);
    }


    @Test
    void testPropertiesOfWithoutPrivate() throws InvocationTargetException, IllegalAccessException
    {
        List<Property> properties = ReflectionUtils.propertiesOf(TestClass.class, false);
        log.debug(properties.toString());
        Assertions.assertThat(properties).hasSize(6);

        TestClass testClass = new TestClass();
        dumpProperties(testClass, properties);
    }


    @Test
    void testPropertiesOfWithPrivate() throws InvocationTargetException, IllegalAccessException
    {
        List<Property> properties = ReflectionUtils.propertiesOf(TestClass.class, true);
        log.debug(properties.toString());
        Assertions.assertThat(properties).hasSize(8);

        TestClass testClass = new TestClass();
        dumpProperties(testClass, properties);
    }

    @Test
    void testGettersOf() {
        // Test with includeInherited = false
        List<Method> getters = ReflectionUtils.gettersOf(TestClass.class, false);

        // Should include getters from TestClass but not from TestBaseClass
        Assertions.assertThat(getters).extracting(Method::getName)
            .contains("getSomething", "getPrivateWithGetter", "isActive", "getOverriddenMethod", "getInterfaceMethod")
            .doesNotContain("getPrivateBaseWithGetter");

        // Test with includeInherited = true
        getters = ReflectionUtils.gettersOf(TestClass.class, true);

        // Should include getters from both TestClass and TestBaseClass
        Assertions.assertThat(getters).extracting(Method::getName)
            .contains("getSomething", "getPrivateWithGetter", "isActive", "getPrivateBaseWithGetter", 
                      "getOverriddenMethod", "getInterfaceMethod");
    }

    @Test
    void testSettersOf() {
        // Test with includeInherited = false
        List<Method> setters = ReflectionUtils.settersOf(TestClass.class, false);

        // Should include setters from TestClass
        Assertions.assertThat(setters).extracting(Method::getName)
            .contains("setActive", "setProperty");

        // Test with includeInherited = true
        setters = ReflectionUtils.settersOf(TestClass.class, true);

        // Should include setters from both TestClass and TestBaseClass
        Assertions.assertThat(setters).extracting(Method::getName)
            .contains("setActive", "setProperty");
    }

    @Test
    void testIsGetter() throws NoSuchMethodException {
        // Test valid getters
        Method getMethod = TestClass.class.getMethod("getSomething");
        Method isMethod = TestClass.class.getMethod("isActive");

        Assertions.assertThat(ReflectionUtils.isGetter(getMethod)).isTrue();
        Assertions.assertThat(ReflectionUtils.isGetter(isMethod)).isTrue();

        // Test non-getters
        Method setMethod = TestClass.class.getMethod("setActive", boolean.class);
        Method toStringMethod = Object.class.getMethod("toString");

        Assertions.assertThat(ReflectionUtils.isGetter(setMethod)).isFalse();
        Assertions.assertThat(ReflectionUtils.isGetter(toStringMethod)).isFalse();
    }

    @Test
    void testIsSetter() throws NoSuchMethodException {
        // Test valid setter
        Method setMethod = TestClass.class.getMethod("setActive", boolean.class);

        Assertions.assertThat(ReflectionUtils.isSetter(setMethod)).isTrue();

        // Test non-setters
        Method getMethod = TestClass.class.getMethod("getSomething");
        Method toStringMethod = Object.class.getMethod("toString");

        Assertions.assertThat(ReflectionUtils.isSetter(getMethod)).isFalse();
        Assertions.assertThat(ReflectionUtils.isSetter(toStringMethod)).isFalse();
    }

    @Test
    void testIsStaticMethod() throws NoSuchMethodException {
        // Test static method
        Method staticMethod = TestClass.class.getMethod("getStaticMethod");

        Assertions.assertThat(ReflectionUtils.isStatic(staticMethod)).isTrue();
        Assertions.assertThat(ReflectionUtils.isNonStatic(staticMethod)).isFalse();

        // Test non-static method
        Method nonStaticMethod = TestClass.class.getMethod("getSomething");

        Assertions.assertThat(ReflectionUtils.isStatic(nonStaticMethod)).isFalse();
        Assertions.assertThat(ReflectionUtils.isNonStatic(nonStaticMethod)).isTrue();
    }

    @Test
    void testIsStaticField() throws NoSuchFieldException {
        // Test static field
        Field staticField = TestClass.class.getDeclaredField("privateStaticWithoutGetter");

        Assertions.assertThat(ReflectionUtils.isStatic(staticField)).isTrue();
        Assertions.assertThat(ReflectionUtils.isNonStatic(staticField)).isFalse();

        // Test non-static field
        Field nonStaticField = TestClass.class.getDeclaredField("privateWithoutGetter");

        Assertions.assertThat(ReflectionUtils.isStatic(nonStaticField)).isFalse();
        Assertions.assertThat(ReflectionUtils.isNonStatic(nonStaticField)).isTrue();
    }

    @Test
    void testGetFieldNameFromMethod() throws NoSuchMethodException {
        // Test getter methods
        Method getMethod = TestClass.class.getMethod("getSomething");
        Method isMethod = TestClass.class.getMethod("isActive");

        Assertions.assertThat(ReflectionUtils.getFieldNameFromMethod(getMethod)).contains("something");
        Assertions.assertThat(ReflectionUtils.getFieldNameFromMethod(isMethod)).contains("active");

        // Test setter method
        Method setMethod = TestClass.class.getMethod("setActive", boolean.class);

        Assertions.assertThat(ReflectionUtils.getFieldNameFromMethod(setMethod)).contains("active");

        // Test non-getter/setter method
        Method toStringMethod = Object.class.getMethod("toString");

        Assertions.assertThat(ReflectionUtils.getFieldNameFromMethod(toStringMethod)).isEmpty();
    }

    @Test
    void testGetGetter() {
        // Test existing getter
        Optional<Method> getter = ReflectionUtils.getGetter(TestClass.class, "something");

        Assertions.assertThat(getter).isPresent();
        Assertions.assertThat(getter.get().getName()).isEqualTo("getSomething");

        // Test non-existing getter
        Optional<Method> nonExistingGetter = ReflectionUtils.getGetter(TestClass.class, "nonExisting");

        Assertions.assertThat(nonExistingGetter).isEmpty();
    }

    @Test
    void testGetSetter() {
        // Test existing setter
        Optional<Method> setter = ReflectionUtils.getSetter(TestClass.class, "active");

        Assertions.assertThat(setter).isPresent();
        Assertions.assertThat(setter.get().getName()).isEqualTo("setActive");

        // Test non-existing setter
        Optional<Method> nonExistingSetter = ReflectionUtils.getSetter(TestClass.class, "nonExisting");

        Assertions.assertThat(nonExistingSetter).isEmpty();
    }

    @Test
    void testGetField() {
        // Test existing field
        Optional<Field> field = ReflectionUtils.getField(TestClass.class, "privateWithoutGetter");

        Assertions.assertThat(field).isPresent();
        Assertions.assertThat(field.get().getName()).isEqualTo("privateWithoutGetter");

        // Test non-existing field
        Optional<Field> nonExistingField = ReflectionUtils.getField(TestClass.class, "nonExisting");

        Assertions.assertThat(nonExistingField).isEmpty();
    }

    @Test
    void testGetAnnotationRecursive() throws NoSuchMethodException {
        // Test method with annotation in interface
        Method interfaceMethod = TestClass.class.getMethod("getInterfaceMethod");
        TestAnnotation annotation = ReflectionUtils.getAnnotationRecursive(interfaceMethod, TestAnnotation.class);

        Assertions.assertThat(annotation).isNotNull();
        Assertions.assertThat(annotation.value()).isEqualTo("interface");

        // Test method with annotation in superclass
        Method overriddenMethod = TestClass.class.getMethod("getOverriddenMethod");
        annotation = ReflectionUtils.getAnnotationRecursive(overriddenMethod, TestAnnotation.class);

        Assertions.assertThat(annotation).isNotNull();
        Assertions.assertThat(annotation.value()).isEqualTo("base");

        // Test method without annotation
        Method noAnnotationMethod = TestClass.class.getMethod("getSomething");
        annotation = ReflectionUtils.getAnnotationRecursive(noAnnotationMethod, TestAnnotation.class);

        Assertions.assertThat(annotation).isNull();
    }

    @Test
    void testGetSubject() throws NoSuchMethodException, NoSuchFieldException {
        TestClass testObject = new TestClass();

        // Test static method
        Method staticMethod = TestClass.class.getMethod("getStaticMethod");
        Assertions.assertThat(ReflectionUtils.getSubject(staticMethod, testObject)).isNull();

        // Test non-static method
        Method nonStaticMethod = TestClass.class.getMethod("getSomething");
        Assertions.assertThat(ReflectionUtils.getSubject(nonStaticMethod, testObject)).isSameAs(testObject);

        // Test static field
        Field staticField = TestClass.class.getDeclaredField("privateStaticWithoutGetter");
        Assertions.assertThat(ReflectionUtils.getSubject(staticField, testObject)).isNull();

        // Test non-static field
        Field nonStaticField = TestClass.class.getDeclaredField("privateWithoutGetter");
        Assertions.assertThat(ReflectionUtils.getSubject(nonStaticField, testObject)).isSameAs(testObject);
    }

    @Test
    void testIsTerminalType()
    {
        // Test primitive types (should be terminal)
        Assertions.assertThat(ReflectionUtils.isTerminalType(int.class)).isTrue();
        Assertions.assertThat(ReflectionUtils.isTerminalType(boolean.class)).isTrue();

        // Test enum types (should be terminal)
        Assertions.assertThat(ReflectionUtils.isTerminalType(java.time.Month.class)).isTrue();

        // Test array types (should NOT be terminal)
        Assertions.assertThat(ReflectionUtils.isTerminalType(int[].class)).isFalse();
        Assertions.assertThat(ReflectionUtils.isTerminalType(String[].class)).isFalse();
        Assertions.assertThat(ReflectionUtils.isTerminalType(Object[].class)).isFalse();

        // Test special Java types (should be terminal)
        Assertions.assertThat(ReflectionUtils.isTerminalType(java.util.Date.class)).isTrue();
        Assertions.assertThat(ReflectionUtils.isTerminalType(java.util.UUID.class)).isTrue();

        // Test Java standard library types (should be terminal)
        Assertions.assertThat(ReflectionUtils.isTerminalType(String.class)).isTrue();
        Assertions.assertThat(ReflectionUtils.isTerminalType(Integer.class)).isTrue();

        // Test custom types (should NOT be terminal)
        Assertions.assertThat(ReflectionUtils.isTerminalType(TestClass.class)).isFalse();
    }

    @Test
    void testIsTerminalTypeWithObject() {
        // Test with String object
        Assertions.assertThat(ReflectionUtils.isTerminalType("test")).isTrue();

        // Test with custom object
        Assertions.assertThat(ReflectionUtils.isTerminalType(new TestClass())).isFalse();

        // Note: We don't test with null object because the current implementation
        // throws NullPointerException for null objects
    }
}
