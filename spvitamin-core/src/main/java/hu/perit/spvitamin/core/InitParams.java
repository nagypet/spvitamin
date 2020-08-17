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

package hu.perit.spvitamin.core;

import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Parameters from properties file.
 * The property file must be located in the classes folder.
 *
 * @author Peter Nagy
 */

@Log4j
public class InitParams {

    private static InitParams myInstance = null;
    private static final String USER_DIR = "user.dir";

    private String configFileLocation;
    private String configFileName = "application.properties";
    private Properties properties = null;

    private InitParams() throws IOException {
        this.properties = this.loadPropertiesFromFile(this.configFileName);
    }

    public static synchronized InitParams getInstance() {
        if (myInstance == null) {
            try {
                myInstance = new InitParams();
            }
            catch (IOException e) {
                log.error(e.toString());
                myInstance = null;
            }
        }
        return myInstance;
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
    private Properties loadPropertiesFromFile(String fileName) throws IOException {
        String workingDir = System.getProperty(USER_DIR);
        this.configFileLocation = new File(workingDir, "config").getAbsolutePath();
        File iniFile = new File(this.configFileLocation, fileName);
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(iniFile);
        }
        catch (IOException ioe) {
            //log.warn(String.format("%s - trying to load from resources", ioe.toString()));
            // trying to load from the resource
            inStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        }

        if (inStream == null) {
            throw new IOException("File not found " + fileName);
        }

        log.debug(String.format("Loaded config file '%s'", iniFile.getAbsolutePath()));

        Properties props = new Properties();
        props.load(inStream);
        return props;
    }


    public String getParam(String paramName) {
        return this._getParam(paramName, true);
    }


    public boolean isParamAvailable(String paramName) {
        return !this._getParam(paramName, false).isEmpty();
    }


    private String _getParam(String paramName, boolean warn) {
        //log.warn(String.format("WARNING: '%s' could not be found in '%s', trying: '%s'!", paramNameComputerDep, PROPERTY_FILE_NAME, paramName));
        String retval = properties.getProperty(paramName);
        if (retval == null || retval.isEmpty()) {
            if (warn) {
                log.warn(String.format("WARNING: '%s' could not be found in '%s'! Origin: %s", paramName, this.configFileName, StackTracer.currentThreadToString()));
            }
            retval = "";
        }
        return retval;
    }


    public String getParam(String paramName, String defaultValue) {
        String retval = this.getParam(paramName);
        if (retval == null || retval.isEmpty()) {
            retval = defaultValue;
        }
        return retval;
    }


    /*
     * Returns all the key-values where key begins with paramPrefix
     */
    public Properties getParams(String paramPrefix) {
        Properties result = new Properties();
        for (String propName : properties.stringPropertyNames()) {
            if (propName.startsWith(paramPrefix)) {
                String value = this._getParam(propName, false);
                if (!value.isEmpty()) {
                    result.setProperty(propName, value);
                }
            }
        }
        if (result.isEmpty()) {
            log.warn(String.format("WARNING: '%s' could not be found in '%s'! Origin: %s", paramPrefix, this.configFileName, StackTracer.currentThreadToString()));
        }
        return result;
    }


    public List<String> fromProperties(Properties props) {
        List<String> retval = new ArrayList<>();

        Enumeration iter = props.propertyNames();
        while (iter.hasMoreElements()) {
            String key = iter.nextElement().toString();
            retval.add(props.getProperty(key, ""));
        }
        return retval;
    }


    public boolean getBooleanParam(String paramName, boolean defaultValue) {
        String val = this.getParam(paramName);
        switch (val) {
            case "0":
                return false;
            case "1":
                return true;
            case "true":
            case "True":
                return true;
            case "false":
            case "False":
                return false;
            default:
                return defaultValue;
        }
    }


    public List<String> getKeys() {
        return new ArrayList<>(this.properties.stringPropertyNames());
    }
}
