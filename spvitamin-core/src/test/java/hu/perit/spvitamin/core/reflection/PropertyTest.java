package hu.perit.spvitamin.core.reflection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyTest {

    // Test classes
    private static class TestClass {
        public String publicField = "publicField";
        private String privateField = "privateField";
        
        @JsonIgnore
        public String ignoredField = "ignoredField";
        
        public String getPublicGetter() {
            return "publicGetter";
        }
        
        @JsonIgnore
        public String getIgnoredGetter() {
            return "ignoredGetter";
        }
        
        public boolean isActive() {
            return true;
        }
    }
    
    @Test
    void testFromField() throws NoSuchFieldException {
        // Arrange
        Field field = TestClass.class.getField("publicField");
        
        // Act
        Property property = Property.fromField(field);
        
        // Assert
        assertThat(property.getKind()).isEqualTo(Property.Kind.FIELD);
        assertThat(property.getName()).isEqualTo("publicField");
        assertThat(property.getField()).isEqualTo(field);
        assertThat(property.getGetter()).isNull();
    }
    
    @Test
    void testFromGetter() throws NoSuchMethodException {
        // Arrange
        Method method = TestClass.class.getMethod("getPublicGetter");
        
        // Act
        Property property = Property.fromGetter(method);
        
        // Assert
        assertThat(property.getKind()).isEqualTo(Property.Kind.GETTER);
        assertThat(property.getName()).isEqualTo("publicGetter");
        assertThat(property.getGetter()).isEqualTo(method);
        assertThat(property.getField()).isNull();
    }
    
    @Test
    void testFromGetterWithIsMethod() throws NoSuchMethodException {
        // Arrange
        Method method = TestClass.class.getMethod("isActive");
        
        // Act
        Property property = Property.fromGetter(method);
        
        // Assert
        assertThat(property.getKind()).isEqualTo(Property.Kind.GETTER);
        assertThat(property.getName()).isEqualTo("active");
        assertThat(property.getGetter()).isEqualTo(method);
        assertThat(property.getField()).isNull();
    }
    
    @Test
    void testFromGetterWithNonGetterMethod() throws NoSuchMethodException {
        // Arrange
        Method method = String.class.getMethod("length");
        
        // Act & Assert
        assertThrows(ReflectionException.class, () -> {
            Property.fromGetter(method);
        });
    }
    
    @Test
    void testGetDeclaringClass() throws NoSuchFieldException, NoSuchMethodException {
        // Arrange
        Field field = TestClass.class.getField("publicField");
        Property fieldProperty = Property.fromField(field);
        
        Method method = TestClass.class.getMethod("getPublicGetter");
        Property getterProperty = Property.fromGetter(method);
        
        // Act & Assert
        assertThat(fieldProperty.getDeclaringClass()).isEqualTo(TestClass.class);
        assertThat(getterProperty.getDeclaringClass()).isEqualTo(TestClass.class);
    }
    
    @Test
    void testGetModifiers() throws NoSuchFieldException, NoSuchMethodException {
        // Arrange
        Field field = TestClass.class.getField("publicField");
        Property fieldProperty = Property.fromField(field);
        
        Method method = TestClass.class.getMethod("getPublicGetter");
        Property getterProperty = Property.fromGetter(method);
        
        // Act & Assert
        assertThat(fieldProperty.getModifiers()).isEqualTo(field.getModifiers());
        assertThat(getterProperty.getModifiers()).isEqualTo(method.getModifiers());
    }
    
    @Test
    void testIsSynthetic() throws NoSuchFieldException, NoSuchMethodException {
        // Arrange
        Field field = TestClass.class.getField("publicField");
        Property fieldProperty = Property.fromField(field);
        
        Method method = TestClass.class.getMethod("getPublicGetter");
        Property getterProperty = Property.fromGetter(method);
        
        // Act & Assert
        assertThat(fieldProperty.isSynthetic()).isFalse();
        assertThat(getterProperty.isSynthetic()).isFalse();
    }
    
    @Test
    void testIsIgnored() throws NoSuchFieldException, NoSuchMethodException {
        // Arrange
        Field regularField = TestClass.class.getField("publicField");
        Property regularFieldProperty = Property.fromField(regularField);
        
        Field ignoredField = TestClass.class.getField("ignoredField");
        Property ignoredFieldProperty = Property.fromField(ignoredField);
        
        Method regularMethod = TestClass.class.getMethod("getPublicGetter");
        Property regularGetterProperty = Property.fromGetter(regularMethod);
        
        Method ignoredMethod = TestClass.class.getMethod("getIgnoredGetter");
        Property ignoredGetterProperty = Property.fromGetter(ignoredMethod);
        
        // Act & Assert
        assertThat(regularFieldProperty.isIgnored()).isFalse();
        assertThat(ignoredFieldProperty.isIgnored()).isTrue();
        assertThat(regularGetterProperty.isIgnored()).isFalse();
        assertThat(ignoredGetterProperty.isIgnored()).isTrue();
    }
    
    @Test
    void testGetAnnotation() throws NoSuchFieldException, NoSuchMethodException {
        // Arrange
        Field regularField = TestClass.class.getField("publicField");
        Property regularFieldProperty = Property.fromField(regularField);
        
        Field ignoredField = TestClass.class.getField("ignoredField");
        Property ignoredFieldProperty = Property.fromField(ignoredField);
        
        Method regularMethod = TestClass.class.getMethod("getPublicGetter");
        Property regularGetterProperty = Property.fromGetter(regularMethod);
        
        Method ignoredMethod = TestClass.class.getMethod("getIgnoredGetter");
        Property ignoredGetterProperty = Property.fromGetter(ignoredMethod);
        
        // Act & Assert
        assertThat(regularFieldProperty.getAnnotation(JsonIgnore.class)).isNull();
        assertThat(ignoredFieldProperty.getAnnotation(JsonIgnore.class)).isNotNull();
        assertThat(regularGetterProperty.getAnnotation(JsonIgnore.class)).isNull();
        assertThat(ignoredGetterProperty.getAnnotation(JsonIgnore.class)).isNotNull();
    }
    
    @Test
    void testCanAccess() throws NoSuchFieldException, NoSuchMethodException {
        // Arrange
        TestClass testObject = new TestClass();
        Field field = TestClass.class.getField("publicField");
        Property fieldProperty = Property.fromField(field);
        
        Method method = TestClass.class.getMethod("getPublicGetter");
        Property getterProperty = Property.fromGetter(method);
        
        // Act & Assert
        assertThat(fieldProperty.canAccess(testObject)).isTrue();
        assertThat(getterProperty.canAccess(testObject)).isTrue();
    }
    
    @Test
    void testSetAccessible() throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Arrange
        TestClass testObject = new TestClass();
        Field privateField = TestClass.class.getDeclaredField("privateField");
        Property fieldProperty = Property.fromField(privateField);
        
        // Act
        fieldProperty.setAccessible(true);
        
        // Assert
        assertThat(fieldProperty.get(testObject)).isEqualTo("privateField");
    }
    
    @Test
    void testGet() throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Arrange
        TestClass testObject = new TestClass();
        Field field = TestClass.class.getField("publicField");
        Property fieldProperty = Property.fromField(field);
        
        Method method = TestClass.class.getMethod("getPublicGetter");
        Property getterProperty = Property.fromGetter(method);
        
        // Act & Assert
        assertThat(fieldProperty.get(testObject)).isEqualTo("publicField");
        assertThat(getterProperty.get(testObject)).isEqualTo("publicGetter");
    }
    
    @Test
    void testGetWithNullObject() throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Arrange
        Field field = TestClass.class.getField("publicField");
        Property fieldProperty = Property.fromField(field);
        
        Method method = TestClass.class.getMethod("getPublicGetter");
        Property getterProperty = Property.fromGetter(method);
        
        // Act & Assert
        assertThat(fieldProperty.get(null)).isNull();
        assertThat(getterProperty.get(null)).isNull();
    }
    
    @Test
    void testGetType() throws NoSuchFieldException, NoSuchMethodException {
        // Arrange
        Field field = TestClass.class.getField("publicField");
        Property fieldProperty = Property.fromField(field);
        
        Method method = TestClass.class.getMethod("getPublicGetter");
        Property getterProperty = Property.fromGetter(method);
        
        Method booleanMethod = TestClass.class.getMethod("isActive");
        Property booleanProperty = Property.fromGetter(booleanMethod);
        
        // Act & Assert
        assertThat(fieldProperty.getType()).isEqualTo(String.class);
        assertThat(getterProperty.getType()).isEqualTo(String.class);
        assertThat(booleanProperty.getType()).isEqualTo(boolean.class);
    }
    
    @Test
    void testGetGenericType() throws NoSuchFieldException, NoSuchMethodException {
        // Arrange
        Field field = TestClass.class.getField("publicField");
        Property fieldProperty = Property.fromField(field);
        
        Method method = TestClass.class.getMethod("getPublicGetter");
        Property getterProperty = Property.fromGetter(method);
        
        // Act & Assert
        assertThat(fieldProperty.getGenericType()).isEqualTo(field.getGenericType());
        assertThat(getterProperty.getGenericType()).isEqualTo(method.getGenericReturnType());
    }
    
    @Test
    void testToString() throws NoSuchFieldException, NoSuchMethodException {
        // Arrange
        Field field = TestClass.class.getField("publicField");
        Property fieldProperty = Property.fromField(field);
        
        Method method = TestClass.class.getMethod("getPublicGetter");
        Property getterProperty = Property.fromGetter(method);
        
        // Act & Assert
        assertThat(fieldProperty.toString()).contains("FIELD");
        assertThat(fieldProperty.toString()).contains("publicField");
        
        assertThat(getterProperty.toString()).contains("GETTER");
        assertThat(getterProperty.toString()).contains("publicGetter");
    }
}
