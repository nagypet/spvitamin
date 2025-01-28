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

package hu.perit.spvitamin.spring.admin.serverparameter;

import hu.perit.spvitamin.core.reflection.Property;
import hu.perit.spvitamin.core.reflection.ReflectionUtils;
import hu.perit.spvitamin.spring.config.ConfigProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Peter Nagy
 */


@Getter
@NoArgsConstructor
public class ServerParameterListImpl implements ServerParameterList
{
    private final Map<String, Set<ServerParameter>> parameters = new TreeMap<>();


    public ServerParameterListImpl(Object object, String namePrefix)
    {
        this(object != null ? object.getClass().getSimpleName() : null, object, namePrefix);
    }


    public ServerParameterListImpl(String group, Object object, String namePrefix)
    {
        if (object != null)
        {
            getProperties(group, object, namePrefix);
        }
    }


    @Override
    public void add(String group, ServerParameter serverParameter)
    {
        Set<ServerParameter> parameterList = this.parameters.computeIfAbsent(group, k -> new TreeSet<>());
        if (parameterList.stream().noneMatch(i -> i.equals(serverParameter)))
        {
            parameterList.add(serverParameter);
        }
    }


    @Override
    public void add(ServerParameterList serverParameterList)
    {
        add(null, serverParameterList);
    }


    @Override
    public void add(String group, ServerParameterList serverParameterList)
    {
        if (serverParameterList != null && serverParameterList.getParameters() != null)
        {
            for (Map.Entry<String, Set<ServerParameter>> entry : serverParameterList.getParameters().entrySet())
            {
                entry.getValue().forEach(i -> this.add(group != null ? group : entry.getKey(), i));
            }
        }
    }


    private void getProperties(String group, Object object, String namePrefix)
    {

        Class<?> objectClass = object.getClass();

        List<Property> properties = ReflectionUtils.allPropertiesOf(objectClass, false);
        for (Property property : properties)
        {
            if (isIgnored(property))
            {
                continue;
            }
            String propertyName = getPropertyName(namePrefix, property.getName());
            if (isHidden(property) || isHidden(propertyName))
            {
                add(group, new ServerParameter(propertyName, "*** [hidden]", false));
            }
            else
            {
                try
                {
                    if (property.canAccess(object))
                    {
                        Object propertyValue = property.get(object);

                        if (isPrimitiveType(propertyValue))
                        {
                            add(group, new ServerParameter(propertyName, String.valueOf(propertyValue), false));
                        }
                        else
                        {
                            add(new ServerParameterListImpl(group, propertyValue, propertyName));
                        }
                    }
                }
                catch (IllegalAccessException e)
                {
                    // private property
                }
                catch (Exception e)
                {
                    add(group, new ServerParameter(propertyName, String.format("Conversion error: %s", e.getMessage()), false));
                }
            }
        }
    }


    private static String getPropertyName(String namePrefix, String propertyName)
    {
        if (StringUtils.isNotBlank(namePrefix))
        {
            return String.format("%s.%s", namePrefix, propertyName);
        }

        return propertyName;
    }


    private static boolean isHidden(Property property)
    {
        ConfigProperty configProperty = property.getAnnotation(ConfigProperty.class);
        if (configProperty == null)
        {
            return false;
        }
        return configProperty.hidden();
    }


    private static boolean isIgnored(Property property)
    {
        ConfigProperty configProperty = property.getAnnotation(ConfigProperty.class);
        if (configProperty == null)
        {
            return false;
        }
        return configProperty.ignored();
    }


    private static boolean isHidden(String fieldName)
    {
        return fieldName.toLowerCase().contains("password") || fieldName.toLowerCase().contains("secret");
    }


    private static boolean isPrimitiveType(Object object)
    {
        if (object == null)
        {
            return true;
        }

        Class<?> clazz = object.getClass();
        if (clazz.getName().startsWith("com.netflix"))
        {
            return true;
        }

        return ReflectionUtils.isTerminalType(clazz);
    }
}
