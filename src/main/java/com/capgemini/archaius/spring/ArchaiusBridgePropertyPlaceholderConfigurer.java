/*
 * Copyright (C) 2014 Capgemini (oss@capgemini.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.capgemini.archaius.spring;

import com.capgemini.archaius.spring.util.JdbcContants;
import com.netflix.config.ConcurrentCompositeConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.spring.spi.BridgePropertyPlaceholderConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.Properties;
import org.apache.commons.configuration.ConfigurationConverter;

/**
 *
 * @author Andrew Harmel-Law
 */
public class ArchaiusBridgePropertyPlaceholderConfigurer extends BridgePropertyPlaceholderConfigurer {

    public static final int DEFAULT_DELAY = 1000;
    // settings for dynamic property configuration
    private transient int initialDelayMillis = DEFAULT_DELAY;
    private transient int delayMillis = DEFAULT_DELAY;
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchaiusBridgePropertyPlaceholderConfigurer.class);
    private final transient ArchaiusSpringPropertyPlaceholderSupport propertyPlaceholderSupport
            = new ArchaiusSpringPropertyPlaceholderSupport();
    private transient boolean ignoreResourceNotFound=false;
    private transient boolean ignoreDeletesFromSource = true;
    private transient Map<String, String> jdbcConnectionDetailMap = null;

    @Override
    public void setIgnoreResourceNotFound(boolean setting) {
        ignoreResourceNotFound = setting;
        super.setIgnoreResourceNotFound(setting);
    }

    /**
     * The initial delay before the property values are re-read from the location, in milliseconds
     *
     * @param initialDelayMillis
     */
    public void setInitialDelayMillis(int initialDelayMillis) {
        this.initialDelayMillis = initialDelayMillis;
    }

    /**
     * Set the delay for the property values to re-read from the location, in milliseconds
     *
     * @param delayMillis
     */
    public void setDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    /**
     * Should the dynamic property loader ignore deletes from the location source.
     *
     * @param ignoreDeletesFromSource
     */
    public void setIgnoreDeletesFromSource(boolean ignoreDeletesFromSource) {
        this.ignoreDeletesFromSource = ignoreDeletesFromSource;
    }

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
        return propertyPlaceholderSupport.resolvePlaceholder(placeholder, props, systemPropertiesMode);
    }

    @Override
    public void setLocation(Resource location) {
        ConcurrentCompositeConfiguration conComConfiguration = null;
        try {
            if (jdbcConnectionDetailMap == null) {
                propertyPlaceholderSupport.setLocation(location, initialDelayMillis, delayMillis, ignoreDeletesFromSource);
                super.setLocation(location);
            } else {

                Map<String, String> defaultParameterMap = getDefaultParamMap();
                conComConfiguration = propertyPlaceholderSupport.setMixResourcesAsPropertySource(location,
                        defaultParameterMap, jdbcConnectionDetailMap);
                super.setProperties(ConfigurationConverter.getProperties(conComConfiguration));
            }
        } catch (Exception ex) {
            LOGGER.error("Problem setting the location.", ex);
            throw new RuntimeException("Problem setting the location.", ex);
        }
    }

    @Override
    public void setLocations(Resource[] locations) {
        ConcurrentCompositeConfiguration conComConfiguration = null;
        try {
            if (jdbcConnectionDetailMap == null) {
                propertyPlaceholderSupport.setLocations(locations, ignoreResourceNotFound, initialDelayMillis,
                        delayMillis, ignoreDeletesFromSource);
                super.setLocations(locations);
            } else {
                Map<String, String> defaultParameterMap = getDefaultParamMap();
                conComConfiguration=propertyPlaceholderSupport.setMixResourcesAsPropertySource(locations, defaultParameterMap, jdbcConnectionDetailMap);
                super.setProperties(ConfigurationConverter.getProperties(conComConfiguration));
            }
        } catch (Exception ex) {
            LOGGER.error("Problem setting the locations", ex);
            throw new RuntimeException("Problem setting the locations.", ex);
        }
    }
    
    public void setjdbcLocation(String jdbcLocation) {
        if (jdbcLocation != null) {
            jdbcConnectionDetailMap = createDatabaseKeyValueMap(jdbcLocation);
        }
    }

    private Map<String, String> createDatabaseKeyValueMap(String jdbcUri) {
        Map<String, String> jdbcMap = new HashMap<>();

        String delims = "[|][|]";

        if (jdbcUri == null) {
            LOGGER.info("Argument passed Cant be null. ");
            LOGGER.error("The argument passes is not correct");
            LOGGER.error("Argument format to be passes is : driverClassName=<com.mysql.jdbc.Driver>||"
                    + "dbURL#<jdbc:mysql://localhost:3306/java>||username#<root>||password=<password>||"
                    + "sqlQuerry#s<elect distinct property_key, property_value from MySiteProperties>||"
                    + "keyColumnName#<property_key>||valueColumnName#<property_value>");
        }

        String[] tokens = jdbcUri.split(delims);

        if (tokens.length != JdbcContants.EXPECTED_JDBC_PARAM_COUNT) {
            LOGGER.info("Argument passed : " + jdbcUri);
            LOGGER.error("The argument passes is not correct");
            LOGGER.error("Argument format to be passes is : driverClassName=<com.mysql.jdbc.Driver>||"
                    + "dbURL#<jdbc:mysql://localhost:3306/java>||username#<root>||password=<password>||"
                    + "sqlQuerry#s<elect distinct property_key, property_value from MySiteProperties>||"
                    + "keyColumnName#<property_key>||valueColumnName#<property_value>");
        } else {
            delims = "[#]";
            for (String keyValue : tokens) {
                String[] keyAndValue = keyValue.split(delims);
                jdbcMap.put(keyAndValue[0], keyAndValue[1]);
            }
        }
        return jdbcMap;
    }

    private Map<String, String> getDefaultParamMap() {
        Map<String, String> defaultParameterMap = new HashMap<>();
        defaultParameterMap.put(JdbcContants.DELAY_MILLIS, String.valueOf(delayMillis));
        defaultParameterMap.put(JdbcContants.INITIAL_DELAY_MILLIS, String.valueOf(initialDelayMillis));
        defaultParameterMap.put(JdbcContants.IGNORE_DELETE_FROMSOURCE, String.valueOf(ignoreDeletesFromSource));
        defaultParameterMap.put(JdbcContants.IGNORE_RESOURCE_NOTFOUND, String.valueOf(ignoreResourceNotFound));
        return defaultParameterMap;

    }
}
