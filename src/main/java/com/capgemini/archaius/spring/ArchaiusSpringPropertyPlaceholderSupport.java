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
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicURLConfiguration;
import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.sources.JDBCConfigurationSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * This class builds the property configuration factory for the location(s)
 * provided.
 *
 * @author Andrew Harmel-Law
 * @author Nick Walter
 */
class ArchaiusSpringPropertyPlaceholderSupport {

    private transient String dbURL;
    private transient String driverClassName;
    private transient String username;
    private transient String password;
    private transient String sqlQuerry;
    private transient String keyColumnName;
    private transient String valueColumnName;

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchaiusSpringPropertyPlaceholderSupport.class);

    protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
        return DynamicPropertyFactory.getInstance().getStringProperty(placeholder, null).get();
    }

    protected void setLocation(Resource location,
            int initialDelayMillis,
            int delayMillis,
            boolean ignoreDeletesFromSource) throws IOException {

        if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
            LOGGER.error("There was already a config source (or sources) configured.");
            throw new IllegalStateException("Archaius is already configured with a property source/sources.");
        }

        final String locationURL = location.getURL().toString();
        final DynamicURLConfiguration urlConfiguration = new DynamicURLConfiguration(
                initialDelayMillis, delayMillis, ignoreDeletesFromSource, locationURL
        );

        DynamicPropertyFactory.initWithConfigurationSource(urlConfiguration);
    }

    protected void setLocations(Resource[] locations,
            boolean ignoreResourceNotFound,
            int initialDelayMillis,
            int delayMillis,
            boolean ignoreDeletesFromSource) throws IOException {

        if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
            LOGGER.error("There was already a config source (or sources) configured.");
            throw new IllegalStateException("Archaius is already configured with a property source/sources.");
        }

        ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();
        for (int i = locations.length - 1; i >= 0; i--) {
            try {
                final String locationURL = locations[i].getURL().toString();
                config.addConfiguration(new DynamicURLConfiguration(
                        initialDelayMillis, delayMillis, ignoreDeletesFromSource, locationURL
                ));
            } catch (Exception ex) {
                if (!ignoreResourceNotFound) {
                    LOGGER.error("Exception thrown when adding a configuration location.", ex);
                    throw ex;
                }
            }
        }

        DynamicPropertyFactory.initWithConfigurationSource(config);
    }

    public DynamicConfiguration setJdbcResourceAsArchaiusPropetiesSource(Map<String, String> jdbcConnectionDetailMap,
            int initialDelayMillis,
            int delayMillis,
            boolean ignoreDeletesFromSource) {

        if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
            LOGGER.error("There was already a config source (or sources) configured.");
            throw new IllegalStateException(
                    "Archaius is already configured with a property source/sources.");
        }

        setJdbcConfigurationParameter(jdbcConnectionDetailMap);

        DriverManagerDataSource ds = new DriverManagerDataSource(driverClassName, dbURL, username, password);

        JDBCConfigurationSource source = new JDBCConfigurationSource(ds,
                sqlQuerry, keyColumnName, valueColumnName);

        FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(initialDelayMillis, delayMillis, ignoreDeletesFromSource);

        DynamicConfiguration configuration = new DynamicConfiguration(source, scheduler);

        DynamicPropertyFactory.initWithConfigurationSource(configuration);

        return configuration;
    }

    protected ConcurrentCompositeConfiguration setMixResourcesAsPropertySource(Resource[] locations,
            Map<String, String> defaultParameterMap, 
            Map<String, String> jdbcConnectionDetailMap) throws IOException {

        int initialDelayMillis = Integer.parseInt(defaultParameterMap.get(JdbcContants.INITIAL_DELAY_MILLIS));
        int delayMillis = Integer.parseInt(defaultParameterMap.get(JdbcContants.DELAY_MILLIS));
        boolean ignoreDeletesFromSource = Boolean.parseBoolean(defaultParameterMap.get(JdbcContants.IGNORE_DELETE_FROMSOURCE));
        boolean ignoreResourceNotFound = Boolean.parseBoolean(defaultParameterMap.get(JdbcContants.IGNORE_RESOURCE_NOTFOUND));

        if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
            LOGGER.error("There was already a config source (or sources) configured.");
            throw new IllegalStateException(
                    "Archaius is already configured with a property source/sources.");
        }

        //TODO add documentation for the effect of loading jdbc first and location as it divert from normal way of property overloading of Archaius.
        ConcurrentCompositeConfiguration conComConfiguration = new ConcurrentCompositeConfiguration();
        //adding database tables to the Archaius  
        setJdbcConfigurationParameter(jdbcConnectionDetailMap);

        DriverManagerDataSource ds = new DriverManagerDataSource(driverClassName, dbURL, username, password);

        JDBCConfigurationSource source = new JDBCConfigurationSource(ds, sqlQuerry, keyColumnName, valueColumnName);

        FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(initialDelayMillis, delayMillis, ignoreDeletesFromSource);

        DynamicConfiguration dynamicConfiguration = new DynamicConfiguration(source, scheduler);

        conComConfiguration.addConfiguration(dynamicConfiguration);

        // adding file or classpath properties to the Archaius 
        for (int i = locations.length - 1; i >= 0; i--) {
            try {
                final String locationURL = locations[i].getURL().toString();
                conComConfiguration.addConfiguration(new DynamicURLConfiguration(
                        initialDelayMillis, delayMillis,
                        ignoreDeletesFromSource, locationURL));
            } catch (Exception ex) {
                if (!ignoreResourceNotFound) {
                    LOGGER.error(
                            "Exception thrown when adding a configuration location.",
                            ex);
                    throw ex;
                }
            } 
        }

        DynamicPropertyFactory.initWithConfigurationSource(conComConfiguration);

        return conComConfiguration;
    }

    protected ConcurrentCompositeConfiguration setMixResourcesAsPropertySource(Resource location,
            Map<String, String> defaultParameterMap, 
            Map<String, String> jdbcConnectionDetailMap) throws IOException {

        final String locationURL = location.getURL().toString();
        int initialDelayMillis = Integer.parseInt(defaultParameterMap.get(JdbcContants.INITIAL_DELAY_MILLIS));
        int delayMillis = Integer.parseInt(defaultParameterMap.get(JdbcContants.DELAY_MILLIS));
        boolean ignoreDeletesFromSource = Boolean.parseBoolean(defaultParameterMap.get(JdbcContants.IGNORE_DELETE_FROMSOURCE));

        if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
            LOGGER.error("There was already a config source (or sources) configured.");
            throw new IllegalStateException(
                    "Archaius is already configured with a property source/sources.");
        }

        ConcurrentCompositeConfiguration conComConfiguration = new ConcurrentCompositeConfiguration();

        //adding database tables to the Archaius  
        setJdbcConfigurationParameter(jdbcConnectionDetailMap);

        DriverManagerDataSource ds = new DriverManagerDataSource(driverClassName, dbURL, username, password);

        JDBCConfigurationSource source = new JDBCConfigurationSource(ds, sqlQuerry, keyColumnName, valueColumnName);

        FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(initialDelayMillis, delayMillis, ignoreDeletesFromSource);

        DynamicConfiguration dynamicConfiguration = new DynamicConfiguration(source, scheduler);

        conComConfiguration.addConfiguration(dynamicConfiguration);

        // adding file or classpath properties to the Archaius 
        final DynamicURLConfiguration urlConfiguration = new DynamicURLConfiguration(initialDelayMillis, delayMillis, ignoreDeletesFromSource, locationURL);

        conComConfiguration.addConfiguration(urlConfiguration);

        DynamicPropertyFactory.initWithConfigurationSource(conComConfiguration);

        return conComConfiguration;
    }

    private void setJdbcConfigurationParameter(Map<String, String> jdbcConnectionDetailMap) {
        this.dbURL = jdbcConnectionDetailMap.get(JdbcContants.DB_URL);
        this.driverClassName = jdbcConnectionDetailMap.get(JdbcContants.DRIVER_CLASS_NAME);
        this.username = jdbcConnectionDetailMap.get(JdbcContants.USERNAME);
        this.password = jdbcConnectionDetailMap.get(JdbcContants.PASSWORD);
        this.sqlQuerry = jdbcConnectionDetailMap.get(JdbcContants.SQL_QUERRY);
        this.keyColumnName = jdbcConnectionDetailMap.get(JdbcContants.KEY_COLUMN_NAME);
        this.valueColumnName = jdbcConnectionDetailMap.get(JdbcContants.VALUE_COLUMN_NAME);
    }
}
