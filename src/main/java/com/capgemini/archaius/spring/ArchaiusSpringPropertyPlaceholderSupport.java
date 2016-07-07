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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.netflix.config.*;
import com.netflix.config.sources.JDBCConfigurationSource;

import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.capgemini.archaius.spring.constants.JdbcContants;

/**
 * This class builds the property configuration factory for the location(s)
 * provided.
 *
 * @author Andrew Harmel-Law
 * @author Nick Walter
 * @author Sanjay Kumar
 */
class ArchaiusSpringPropertyPlaceholderSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchaiusSpringPropertyPlaceholderSupport.class);

    private static final String ARG_FORMAT_TIP_MSG = "Argument format is : driverClassName=<com.mysql.jdbc.Driver>||"
            + "dbURL#<jdbc:mysql://localhost:3306/java>||username#<root>||password=<password>||"
            + "sqlQuery#s<elect distinct property_key, property_value from MySiteProperties>||"
            + "keyColumnName#<property_key>||valueColumnName#<property_value>";
    
    protected DynamicStringProperty resolvePlaceholder(final String placeholder, Properties props, int systemPropertiesMode) {
        return DynamicPropertyFactory.getInstance().getStringProperty(placeholder, null);
    }

    private void resetPropertyValue(String placeholder, DynamicStringProperty property,
                                    ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap,
                                    BeanFactory beanFactory){
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()){
            BeanDefinition beanDefinition = entry.getValue();
            MutablePropertyValues mpv = beanDefinition.getPropertyValues();
            for (PropertyValue pv: mpv.getPropertyValues()){
                if (pv.getName().equals(placeholder)){
                    Object bean = beanFactory.getBean(entry.getKey());
                    BeanWrapper wrapper = new BeanWrapperImpl(bean);
                    wrapper.setPropertyValue(placeholder, property.get());
                }
            }
        }
    }

    /**
     * This supports setLocation and setLocations in the *PropertyPlaceholderConfigurer classes.
     * 
     * @param locations
     * @param ignoreResourceNotFound
     * @param initialDelayMillis
     * @param delayMillis
     * @param ignoreDeletesFromSource
     * @throws IOException 
     */
    protected void setLocations(Map<String, String> parameterMap, Resource[] locations) throws IOException {

        ConcurrentCompositeConfiguration config = getExistingConfigIfAllowed(parameterMap);
        if (config == null) {
            config = addFileAndClasspathPropertyLocationsToConfiguration(parameterMap, locations);
            DynamicPropertyFactory.initWithConfigurationSource(config);
        }
    }
    
    /**
     * This is called when there is a mix of JDBC and non-JDBC (file, classpath and URL) property sources.
     * 
     * It is important at the moment that the jdbcLocations are listed first in the Spring files and 
     * handled first in the code.
     * 
     * @param parameterMap
     * @param locations
     * @param jdbcConnectionDetailMap
     * @return
     * @throws IOException 
     */
    protected ConcurrentCompositeConfiguration setMixedResourcesAsPropertySources(
            Map<String, String> parameterMap, 
            Resource[] locations, 
            Map<String, String> jdbcConnectionDetailMap) throws IOException {
        
        ConcurrentCompositeConfiguration config = getExistingConfigIfAllowed(parameterMap);
        if (config == null) {
            DynamicConfiguration dynamicConfiguration = buildDynamicConfigFromConnectionDetailsMap(jdbcConnectionDetailMap, parameterMap);
            config = new ConcurrentCompositeConfiguration();
            config.addConfiguration(dynamicConfiguration);
            config = addFileAndClasspathPropertyLocationsToConfiguration(config, parameterMap, locations);
            DynamicPropertyFactory.initWithConfigurationSource(config);
        }

        return config;
    }
    
    private ConcurrentCompositeConfiguration addFileAndClasspathPropertyLocationsToConfiguration(
            Map<String, String> parameterMap, 
            Resource[] locations) throws IOException {
        
        return addFileAndClasspathPropertyLocationsToConfiguration(new ConcurrentCompositeConfiguration(), parameterMap, locations);
    }
    
    
    private ConcurrentCompositeConfiguration addFileAndClasspathPropertyLocationsToConfiguration(
            ConcurrentCompositeConfiguration conComConfiguration,
            Map<String, String> parameterMap, 
            Resource[] locations) throws IOException {
        
        int initialDelayMillis = Integer.valueOf(parameterMap.get(JdbcContants.INITIAL_DELAY_MILLIS));
        int delayMillis = Integer.valueOf(parameterMap.get(JdbcContants.DELAY_MILLIS));
        boolean ignoreDeletesFromSource = Boolean.parseBoolean(parameterMap.get(JdbcContants.IGNORE_DELETE_FROM_SOURCE));
        boolean ignoreResourceNotFound = Boolean.parseBoolean(parameterMap.get(JdbcContants.IGNORE_RESOURCE_NOT_FOUND));
        boolean includeSystemConfiguration = Boolean.parseBoolean(parameterMap.get(JdbcContants.INCLUDE_SYSTEM_CONFIGURATION));
        
        for (int i = locations.length - 1; i >= 0; i--) {
            try {
                conComConfiguration.addConfiguration(new DynamicURLConfiguration(initialDelayMillis, delayMillis, ignoreDeletesFromSource, locations[i].getURL().toString()));
            } catch (Exception ex) {
                if (!ignoreResourceNotFound) {
                    LOGGER.error("Exception thrown when adding a configuration location.", ex);
                    throw ex;
                }
            }
        }
        
        if (includeSystemConfiguration) {
            conComConfiguration.addConfiguration(new SystemConfiguration());
        }
        
        return conComConfiguration;
    }
    
    private ConcurrentCompositeConfiguration getExistingConfigIfAllowed(Map<String, String> parameterMap) {
        
        boolean allowMultiplePlaceholders = Boolean.parseBoolean(parameterMap.get(JdbcContants.ALLOW_MULTIPLE_PLACEHOLDERS));            
        
        if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
            if (allowMultiplePlaceholders) {
                LOGGER.warn("Archaius is already configured with a property source/sources. Reusing those instead. ");
                return (ConcurrentCompositeConfiguration) DynamicPropertyFactory.getBackingConfigurationSource();
            } else {
                LOGGER.error("There was already a config source (or sources) configured.");
                throw new IllegalStateException("Archaius is already configured with a property source/sources.");
            }
        } else {
            return null;
        }
    }

    protected Map<String, String> getParameterMap(int delayMillis, int initialDelayMillis, 
                                                  boolean ignoreDeleteFromSource, boolean ignoreResourceNotFound,
                                                  boolean allowMultiplePlaceholders, boolean includeSystemConfiguration) {

        Map parameterMap = new HashMap();

        parameterMap.put(JdbcContants.DELAY_MILLIS, String.valueOf(delayMillis));
        parameterMap.put(JdbcContants.INITIAL_DELAY_MILLIS, String.valueOf(initialDelayMillis));
        parameterMap.put(JdbcContants.IGNORE_DELETE_FROM_SOURCE, String.valueOf(ignoreDeleteFromSource));
        parameterMap.put(JdbcContants.IGNORE_RESOURCE_NOT_FOUND, String.valueOf(ignoreResourceNotFound));
        parameterMap.put(JdbcContants.ALLOW_MULTIPLE_PLACEHOLDERS, String.valueOf(allowMultiplePlaceholders));
        parameterMap.put(JdbcContants.INCLUDE_SYSTEM_CONFIGURATION, String.valueOf(includeSystemConfiguration));

        return parameterMap;
    }
    
    public Map<String, String> extractJdbcParameters(String jdbcLocation) {
        if (jdbcLocation != null) {
            return createDatabaseKeyValueMap(jdbcLocation);
        } else {
            return null;
        }
    }
    
    private Map<String, String> createDatabaseKeyValueMap(String jdbcUri) {
        
        if (jdbcUri == null) {
            LOGGER.info("Argument passed can't be null.");
            LOGGER.error("The arguments passed are not correct");
            LOGGER.error(ARG_FORMAT_TIP_MSG);
        }
        
        Map<String, String> jdbcMap = new HashMap<>();
        String delims = "[|][|]";
        String[] tokens = jdbcUri.split(delims);

        if (tokens.length != JdbcContants.EXPECTED_JDBC_PARAM_COUNT) {
            LOGGER.info("Argument passed: " + jdbcUri);
            LOGGER.error("The arguments passed are not correct");
            LOGGER.error(ARG_FORMAT_TIP_MSG);
        } else {
            delims = "[#]";
            for (String keyValue : tokens) {
                String[] keyAndValue = keyValue.split(delims);
                jdbcMap.put(keyAndValue[0], keyAndValue[1]);
            }
        }
        return jdbcMap;
    }
    
    private DynamicConfiguration buildDynamicConfigFromConnectionDetailsMap(Map<String, String> jdbcConnectionDetailMap, Map<String, String> parameterMap) {
        
        int initialDelayMillis = Integer.valueOf(parameterMap.get(JdbcContants.INITIAL_DELAY_MILLIS));
        int delayMillis = Integer.valueOf(parameterMap.get(JdbcContants.DELAY_MILLIS));
        boolean ignoreDeletesFromSource = Boolean.parseBoolean(parameterMap.get(JdbcContants.IGNORE_DELETE_FROM_SOURCE));
        
        DriverManagerDataSource ds = buildDataSourceFromConnectionDetailsMap(jdbcConnectionDetailMap);
        JDBCConfigurationSource source = buildJdbcConfigSourceFromConnectionDetailsMap(ds, jdbcConnectionDetailMap);
        FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(initialDelayMillis, delayMillis, ignoreDeletesFromSource);
        return new DynamicConfiguration(source, scheduler);
    }
    
    private JDBCConfigurationSource buildJdbcConfigSourceFromConnectionDetailsMap(DriverManagerDataSource ds, Map<String, String> jdbcConnectionDetailMap) {
        JDBCConfigurationSource source = new JDBCConfigurationSource(ds,
                jdbcConnectionDetailMap.get(JdbcContants.SQL_QUERY),
                jdbcConnectionDetailMap.get(JdbcContants.KEY_COLUMN_NAME),
                jdbcConnectionDetailMap.get(JdbcContants.VALUE_COLUMN_NAME));
        return source;
    }
    
    private DriverManagerDataSource buildDataSourceFromConnectionDetailsMap(Map<String, String> jdbcConnectionDetailMap) {
        DriverManagerDataSource ds = new DriverManagerDataSource(jdbcConnectionDetailMap.get(JdbcContants.DB_URL),
                jdbcConnectionDetailMap.get(JdbcContants.USERNAME),
                jdbcConnectionDetailMap.get(JdbcContants.PASSWORD));
        ds.setDriverClassName(jdbcConnectionDetailMap.get(JdbcContants.DB_DRIVER));
        return ds;
    }
}
