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

package hu.perit.spvitamin.spring.admin.serverparameter;

import hu.perit.spvitamin.core.exception.UnexpectedConditionException;
import hu.perit.spvitamin.spring.config.ConfigProperty;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Peter Nagy
 */


@Getter
public class ServerParameterList {

    private final List<ServerParameter> parameter = new ArrayList<>();

    private static final Set<Class<?>> WRAPPER_TYPES = new HashSet<>();

    static {
        WRAPPER_TYPES.add(Boolean.class);
        WRAPPER_TYPES.add(boolean.class);
        WRAPPER_TYPES.add(Byte.class);
        WRAPPER_TYPES.add(byte.class);
        WRAPPER_TYPES.add(Character.class);
        WRAPPER_TYPES.add(char.class);
        WRAPPER_TYPES.add(Short.class);
        WRAPPER_TYPES.add(short.class);
        WRAPPER_TYPES.add(Integer.class);
        WRAPPER_TYPES.add(int.class);
        WRAPPER_TYPES.add(Long.class);
        WRAPPER_TYPES.add(long.class);
        WRAPPER_TYPES.add(Double.class);
        WRAPPER_TYPES.add(double.class);
        WRAPPER_TYPES.add(Float.class);
        WRAPPER_TYPES.add(float.class);
        WRAPPER_TYPES.add(String.class);
        WRAPPER_TYPES.add(TimeZone.class);
    }


    public static ServerParameterList of(Object o) {
        return of(o, null);
    }


    public static ServerParameterList of(Object o, String namePrefix) {
        ServerParameterList serverParameterList = new ServerParameterList();

        if (o != null) {
            getPropertiesByFields(serverParameterList, o, namePrefix);
            getPropertiesByGetters(serverParameterList, o, namePrefix);
        }

        return serverParameterList;
    }


    private static void getPropertiesByFields(ServerParameterList serverParameterList, Object o, String namePrefix) {

        Class<?> objectClass = o.getClass();

        Field[] fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = String.format("%s.%s", StringUtils.isNotBlank(namePrefix) ? namePrefix : objectClass.getSimpleName(), field.getName());
            if (isHidden(field)) {
                serverParameterList.add(new ServerParameter(fieldName, "*** [hidden]", false));
            }
            else {
                try {
                    Object fieldValue = getFieldValue(field, o);
                    if (fieldValue != null) {
                        NestedConfigurationProperty nestedConfigurationProperty = field.getAnnotation(NestedConfigurationProperty.class);
                        if (nestedConfigurationProperty == null) {
                            serverParameterList.add(new ServerParameter(fieldName, fieldValue.toString(), false));
                        }
                        else {
                            serverParameterList.add(ServerParameterList.of(fieldValue, fieldName));
                        }
                    }
                }
                catch (IllegalAccessException e) {
                    // private property
                }
                catch (Exception e) {
                    serverParameterList.parameter.add(new ServerParameter(fieldName, String.format("Conversion error: %s", e.getMessage()), false));
                }
            }
        }
    }


    private static Object getFieldValue(Field field, Object o) throws InvocationTargetException, IllegalAccessException {
        Object subject = getSubject(field, o);
        if (field.canAccess(subject)) {
            return field.get(subject);
        }
        else {
            return tryInvokeGetter(field, o);
        }
    }


    private static Object getSubject(Field field, Object o) {
        Object subject = o;
        int modifiers = field.getModifiers();
        if ((modifiers & Modifier.STATIC) != 0) {
            subject = null;
        }
        return subject;
    }


    private static Object getSubject(Method method, Object o) {
        Object subject = o;
        int modifiers = method.getModifiers();
        if ((modifiers & Modifier.STATIC) != 0) {
            subject = null;
        }
        return subject;
    }


    private static void getPropertiesByGetters(ServerParameterList serverParameterList, Object o, String namePrefix) {

        Class<?> objectClass = o.getClass();

        Method[] declaredMethods = objectClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (isGetter(method)) {
                String fieldName = String.format("%s.%s", StringUtils.isNotBlank(namePrefix) ? namePrefix : objectClass.getSimpleName(), getFieldNameFromGetter(method));
                if (isHidden(fieldName)) {
                    serverParameterList.add(new ServerParameter(fieldName, "*** [hidden]", false));
                }
                else {
                    try {
                        Object fieldValue = invokeGetter(method, o);
                        if (fieldValue != null) {
                            serverParameterList.add(new ServerParameter(fieldName, fieldValue.toString(), false));
                        }
                    }
                    catch (IllegalAccessException e) {
                        // private property
                    }
                    catch (Exception e) {
                        serverParameterList.add(new ServerParameter(fieldName, String.format("Conversion error: %s", e.getMessage()), false));
                    }
                }
            }
        }
    }


    private static String getFieldNameFromGetter(Method method) {
        if (method.getName().startsWith("get")) {
            return method.getName().substring(3);
        }
        else if (method.getName().startsWith("is")) {
            return method.getName().substring(2);
        }

        throw new UnexpectedConditionException(String.format("'%s' is not a getter method!", method.getName()));
    }


    private static boolean isGetter(Method method) {
        return method.getParameters().length == 0 && (method.getName().startsWith("get") || method.getName().startsWith("is"));
    }


    private static boolean isHidden(Field field) {
        ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);
        if (configProperty == null) {
            return false;
        }
        return configProperty.hidden();
    }

    private static boolean isHidden(String fieldName) {
        if (fieldName.toLowerCase().contains("password")) {
            return true;
        }

        return false;
    }

    private static boolean isPrimitiveType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }


    private static Object tryInvokeGetter(Field field, Object o) throws InvocationTargetException, IllegalAccessException {
        for (Method method : o.getClass().getDeclaredMethods()) {
            if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())) {
                if ((method.getName().startsWith("get") && (method.getName().length() == (field.getName().length() + 3)))
                        || (method.getName().startsWith("is") && (method.getName().length() == (field.getName().length() + 2)))
                ) {
                    Object subject = getSubject(method, o);
                    if (method.canAccess(subject)) {
                        return method.invoke(subject);
                    }
                }
            }
        }

        return null;
    }


    private static Object invokeGetter(Method method, Object o) throws InvocationTargetException, IllegalAccessException {
        if (isGetter(method)) {
            Object subject = getSubject(method, o);
            if (method.canAccess(subject)) {
                return method.invoke(subject);
            }
        }

        return null;
    }


    public void add(ServerParameter serverParameter) {
        if (this.parameter.stream().noneMatch(i -> i.equals(serverParameter))) {
            this.parameter.add(serverParameter);
        }
    }


    public void add(ServerParameterList serverParameterList) {
        serverParameterList.parameter.forEach(this::add);
    }
}
