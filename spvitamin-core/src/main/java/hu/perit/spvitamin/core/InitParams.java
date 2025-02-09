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

package hu.perit.spvitamin.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Parameters from properties file.
 *
 * @author Peter Nagy
 */
@Slf4j
public class InitParams
{
    public static final String APPLICATION_PROPERTIES = "application.properties";
    private static InitParams myInstance = null;
    private static final String USER_DIR = "user.dir";

    @Getter
    private String configFileLocation;
    @Getter
    private String configFileName;
    private final Properties properties;
    private String computerName;


    public static synchronized InitParams getInstance()
    {
        if (myInstance == null)
        {
            try
            {
                myInstance = new InitParams();
            }
            catch (IOException e)
            {
                log.error(e.toString());
                myInstance = null;
            }
        }
        return myInstance;
    }


    private InitParams() throws IOException
    {
        final String defaultAppPropertiesFileConfigName = "application.configuration";

        this.computerName = getComputerName();

        String configFile = System.getProperty(defaultAppPropertiesFileConfigName);
        if (configFile == null)
        {
            // trying to load from application.properties
            configFile = APPLICATION_PROPERTIES;
            Properties props = this.loadPropertiesFromFile(configFile);
            configFile = props.getProperty(defaultAppPropertiesFileConfigName);
            if (configFile == null)
            {
                log.debug(String.format("%s loaded successfully for %s via location %s", this.configFileName, this.computerName, this.configFileLocation));
                this.properties = props;
                return;
            }
        }

        this.properties = this.loadPropertiesFromFile(configFile);
        log.debug(String.format("%s loaded successfully for %s via location %s", this.configFileName, this.computerName, this.configFileLocation));
    }


    private String getComputerName()
    {
        String computername = System.getenv("COMPUTERNAME"); // NOSONAR
        if (StringUtils.isNoneBlank(computername))
        {
            return StringUtils.toRootUpperCase(computername);
        }
        String hostname = System.getenv("HOSTNAME"); // NOSONAR
        return StringUtils.toRootUpperCase(hostname);
    }


    /**
     * Default config location is:
     * 1.) A /config subdirectory of the current directory
     * 2.) The current directory
     * 3.) A classpath /config package
     * 4.) The classpath root
     * The default location can be modified by:
     * 1.) spring.config.location
     * 2.) spring.config.additional-location
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    private Properties loadPropertiesFromFile(String fileName) throws IOException
    {
        List<String> locations = List.of("./config", "./");
        for (String location : locations)
        {
            try
            {
                return loadPropertiesFromFile(location, fileName);
            }
            catch (IOException e)
            {
                // Let's try the next location
            }
        }

        // trying to load from the resource
        IOException exception = null;
        List<String> resourceLocations = List.of("config", "");
        for (String location : resourceLocations)
        {
            try
            {
                return loadPropertiesFromResource(location, fileName);
            }
            catch (IOException e)
            {
                exception = e;
                // Let's try the next location
            }
        }

        throw exception;
    }


    private Properties loadPropertiesFromResource(String location, String fileName) throws IOException
    {
        String resourceLocation = location + "/" + fileName;
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceLocation))
        {
            if (stream == null)
            {
                this.configFileLocation = null;
                this.configFileName = null;
                throw new IOException("Resource not found " + resourceLocation);
            }

            this.configFileLocation = "classpath";
            this.configFileName = resourceLocation;
            Properties props = new Properties();
            props.load(stream);
            return props;
        }
    }


    private Properties loadPropertiesFromFile(String location, String fileName) throws IOException
    {
        String workingDir = System.getProperty(USER_DIR);
        File folder;
        if (StringUtils.isNotBlank(location))
        {
            folder = new File(workingDir, location);
        }
        else
        {
            folder = new File(workingDir);
        }
        File iniFile = new File(folder, fileName);
        try (InputStream stream = new FileInputStream(iniFile))
        {
            this.configFileLocation = location;
            this.configFileName = fileName;
            Properties props = new Properties();
            props.load(stream);
            return props;
        }
    }


    // For testing purposes
    public void setComputerName(String computerName)
    {
        log.warn(String.format("Real COMPUTERNAME environment variable is overritten to: '%s'", computerName));
        this.computerName = computerName;
    }


    public String getParam(String paramName, Boolean warn)
    {
        return this.internalGetParam(paramName, BooleanUtils.isTrue(warn));
    }


    public String getParam(String paramName)
    {
        return this.internalGetParam(paramName, true);
    }


    public boolean isParamAvailable(String paramName)
    {
        Properties propertiesWithPrefix = internalGetParams(paramName, false);
        String param = this.internalGetParam(paramName, false);
        return !param.isEmpty() || !propertiesWithPrefix.isEmpty();
    }


    private String internalGetParam(String paramName, boolean warn)
    {
        String paramNameComputerDep = paramName + '@' + this.computerName;
        String retval = properties.getProperty(paramNameComputerDep);
        if (retval == null || retval.isEmpty())
        {
            // processing included params
            for (String postfix : this.getIncludesFor(this.computerName))
            {
                String paramNameIncluded = paramName + '&' + postfix;
                retval = properties.getProperty(paramNameIncluded, "");
                if (!retval.isEmpty())
                {
                    return retval;
                }
            }

            //log.warn(String.format("WARNING: '%s' could not be found in '%s', trying: '%s'!", paramNameComputerDep, PROPERTY_FILE_NAME, paramName));
            retval = properties.getProperty(paramName);
            if (retval == null || retval.isEmpty())
            {
                if (warn)
                {
                    log.warn(
                            String.format("WARNING: '%s' could not be found in '%s' for '%s'!", paramName, this.configFileName, this.computerName));
                }
                retval = "";
            }
        }
        return retval;
    }


    private String[] getIncludesFor(String computer)
    {
        String includes = properties.getProperty("include@" + computer, "");

        String[] splitted = includes.split(",");

        String[] retval = new String[splitted.length];
        for (int i = splitted.length; i > 0; i--)
        {
            retval[splitted.length - i] = splitted[i - 1].trim();
        }

        return retval;
    }


    public String getParam(String paramName, String defaultValue)
    {
        String retval = this.getParam(paramName);
        if (retval == null || retval.isEmpty())
        {
            retval = defaultValue;
        }
        return retval;
    }


    /*
     * Returns all the key-values where key begins with paramPrefix
     */
    public Properties getParams(String paramPrefix)
    {
        return internalGetParams(paramPrefix, true);
    }


    private Properties internalGetParams(String paramPrefix, boolean warn)
    {
        String prefix = paramPrefix;
        if (!paramPrefix.endsWith("."))
        {
            prefix = prefix + ".";
        }
        Properties result = new Properties();
        for (String propName : properties.stringPropertyNames())
        {
            if (propName.startsWith(prefix))
            {
                String baseName = this.getPropertyBaseName(propName);
                String value = this.internalGetParam(baseName, false);
                if (!value.isEmpty())
                {
                    result.setProperty(baseName, value);
                }
            }
        }
        if (result.isEmpty())
        {
            if (warn)
            {
                log.warn(String.format("WARNING: '%s' could not be found in '%s' for '%s'!", paramPrefix, this.configFileName, this.computerName));
            }
        }
        return result;
    }


    public List<String> fromProperties(Properties props)
    {
        List<String> retval = new ArrayList<>();

        Enumeration iter = props.propertyNames();
        while (iter.hasMoreElements())
        {
            String key = iter.nextElement().toString();
            retval.add(props.getProperty(key, ""));
        }
        return retval;
    }


    // ad.url.1@INNODOX-32 returns ad.url.1
    private String getPropertyBaseName(String name)
    {
        int posAt = name.indexOf('@');
        if (posAt > 0)
        {
            return name.substring(0, posAt);
        }

        int posAnd = name.indexOf('&');

        if (posAnd > 0)
        {
            return name.substring(0, posAnd);
        }

        return name;
    }


    public boolean getBooleanParam(String paramName, boolean defaultValue)
    {
        String val = this.getParam(paramName);
        return switch (val)
        {
            case "0" -> false;
            case "1" -> true;
            case "true", "True" -> true;
            case "false", "False" -> false;
            default -> defaultValue;
        };
    }


    public Integer getIntegerParam(String paramName)
    {
        return getIntegerParam(paramName, null);
    }


    public Integer getIntegerParam(String paramName, Integer defaultValue)
    {
        String val = this.getParam(paramName);
        if ((val == null || val.isEmpty()))
        {
            return defaultValue;
        }
        return Integer.parseInt(val);
    }


    public Long getLongParam(String paramName)
    {
        return getLongParam(paramName, null);
    }


    public Long getLongParam(String paramName, Long defaultValue)
    {
        String val = this.getParam(paramName);
        if ((val == null || val.isEmpty()))
        {
            return defaultValue;
        }
        return Long.parseLong(val);
    }


    public List<String> getKeys()
    {
        return this.properties.stringPropertyNames().stream().filter(this::isPropertyForThisComputer).map(this::getKeyBaseName).sorted().distinct().toList();
    }


    private String getKeyBaseName(String key)
    {
        if (key == null)
        {
            return "";
        }

        if (key.contains("@"))
        {
            return key.substring(0, key.indexOf('@'));
        }
        if (key.contains("&"))
        {
            return key.substring(0, key.indexOf('&'));
        }

        return key;
    }


    protected boolean isPropertyForThisComputer(String key)
    {
        if (key == null)
        {
            return false;
        }

        // General parameters
        if (!(key.contains("@") || key.contains("&")))
        {
            return true;
        }

        // Parameters for this computer
        if (key.contains("@") && this.computerName.equalsIgnoreCase(key.substring(key.indexOf('@') + 1)))
        {
            return true;
        }

        // Included parameters
        String[] includes = this.getIncludesFor(this.computerName);
        return this.isKeyIncluded(key, includes);
    }


    private boolean isKeyIncluded(String key, String[] includes)
    {
        if (key.contains("&"))
        {
            String include = key.substring(key.indexOf('&') + 1);
            return Arrays.asList(includes).contains(include);
        }

        return false;
    }


    public Properties getAllProperties()
    {
        Properties props = new Properties();
        getKeys().stream().sorted().forEach(k -> {
            if (!"include".equalsIgnoreCase(k))
            {
                log.debug("key: {}, param: {}", k, getParam(k));
                props.put(k, getParam(k));
            }
        });
        return props;
    }
}
