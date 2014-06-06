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

import java.util.Map;
import java.util.Properties;

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

/**
 * This class builds the property configuration factory for the location(s)
 * provided.
 * 
 * @author Andrew Harmel-Law
 * @author Nick Walter
 */
class ArchaiusSpringPropertyPlaceholderSupport {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArchaiusSpringPropertyPlaceholderSupport.class);

	protected String resolvePlaceholder(String placeholder, Properties props,
			int systemPropertiesMode) {
		return DynamicPropertyFactory.getInstance()
				.getStringProperty(placeholder, null).get();
	}

	protected void setLocation(Resource location, int initialDelayMillis,
			int delayMillis, boolean ignoreDeletesFromSource) throws Exception {

		if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
			LOGGER.error("There was already a config source (or sources) configured.");
			throw new RuntimeException(
					"Archaius is already configured with a property source/sources.");
		}

		final String locationURL = location.getURL().toString();
		final DynamicURLConfiguration urlConfiguration = new DynamicURLConfiguration(
				initialDelayMillis, delayMillis, ignoreDeletesFromSource,
				locationURL);

		DynamicPropertyFactory.initWithConfigurationSource(urlConfiguration);
	}

	protected void setLocations(Resource[] locations,
			boolean ignoreResourceNotFound, int initialDelayMillis,
			int delayMillis, boolean ignoreDeletesFromSource) throws Exception {

		if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
			LOGGER.error("There was already a config source (or sources) configured.");
			throw new Exception(
					"Archaius is already configured with a property source/sources.");
		}

		ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();
		for (int i = locations.length - 1; i >= 0; i--) {
			try {
				final String locationURL = locations[i].getURL().toString();
				config.addConfiguration(new DynamicURLConfiguration(
						initialDelayMillis, delayMillis,
						ignoreDeletesFromSource, locationURL));
			} catch (Exception ex) {
				if (ignoreResourceNotFound != true) {
					LOGGER.error(
							"Exception thrown when adding a configuration location.",
							ex);
					throw ex;
				}
			}
		}

		DynamicPropertyFactory.initWithConfigurationSource(config);
	}

	public DynamicConfiguration setJdbcResourceAsPropetiesSource(
			Map<String, String> jdbcConnectionDetailMap) throws Exception {
		
		if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
			LOGGER.error("There was already a config source (or sources) configured.");
			throw new Exception(
					"Archaius is already configured with a property source/sources.");
		}

		String dbURL = jdbcConnectionDetailMap.get("url");
		String driverClassName = jdbcConnectionDetailMap.get("driverClassName");
		String username= jdbcConnectionDetailMap.get("username"); 
		String password=jdbcConnectionDetailMap.get("password");
		String querry = jdbcConnectionDetailMap.get("querry");
		String property_key = jdbcConnectionDetailMap.get("property_key");
		String property_value = jdbcConnectionDetailMap.get("property_value");

		DriverManagerDataSource ds = new DriverManagerDataSource(
				driverClassName, dbURL,username, password); //TODO FIX it have to  pass username and  password

		JDBCConfigurationSource source = new JDBCConfigurationSource(ds,
				querry, property_key, property_value);

		FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(
				0, 10, false);
		DynamicConfiguration configuration = new DynamicConfiguration(source,
				scheduler);
		DynamicPropertyFactory.initWithConfigurationSource(configuration);
		return configuration;
	}

	protected ConcurrentCompositeConfiguration setMixResourcesAsPropertySource(Resource[] locations,
			boolean ignoreResourceNotFound, int initialDelayMillis,
			int delayMillis, boolean ignoreDeletesFromSource,
			Map<String, String> jdbcConnectionDetailMap) throws Exception {

		if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
			LOGGER.error("There was already a config source (or sources) configured.");
			throw new Exception(
					"Archaius is already configured with a property source/sources.");
		}
		
		// adding file or classpath properties to the Archaius 
		ConcurrentCompositeConfiguration concurrentCompositeConfiguration = new ConcurrentCompositeConfiguration();
		for (int i = locations.length - 1; i >= 0; i--) {
			try {
				final String locationURL = locations[i].getURL().toString();
				concurrentCompositeConfiguration.addConfiguration(new DynamicURLConfiguration(
						initialDelayMillis, delayMillis,
						ignoreDeletesFromSource, locationURL));
			} catch (Exception ex) {
				if (ignoreResourceNotFound != true) {
					LOGGER.error(
							"Exception thrown when adding a configuration location.",
							ex);
					throw ex;
				}
			}
		}
		
		//adding jdbc tables to the Archaius 
		String dbURL = jdbcConnectionDetailMap.get("url");
		String driverClassName = jdbcConnectionDetailMap.get("driverClassName");
		String username= jdbcConnectionDetailMap.get("username"); 
		String password=jdbcConnectionDetailMap.get("password");
		String querry = jdbcConnectionDetailMap.get("querry");
		String property_key = jdbcConnectionDetailMap.get("property_key");
		String property_value = jdbcConnectionDetailMap.get("property_value");

		DriverManagerDataSource ds = new DriverManagerDataSource(
				driverClassName, dbURL, username, password); //TODO fix this here we have to suer name password.

		JDBCConfigurationSource source = new JDBCConfigurationSource(ds,
				querry, property_key, property_value);
		FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(
				0, 10, false);
		
		DynamicConfiguration dynamicConfiguration = new DynamicConfiguration(source,
				scheduler);
		
		
		concurrentCompositeConfiguration.addConfiguration(dynamicConfiguration);
		

		DynamicPropertyFactory.initWithConfigurationSource(concurrentCompositeConfiguration);
		
		return concurrentCompositeConfiguration;
	}
	
	protected ConcurrentCompositeConfiguration setMixResourcesAsPropertySource(Resource location,
			int initialDelayMillis, int delayMillis, boolean ignoreDeletesFromSource,
			Map<String, String> jdbcConnectionDetailMap) throws Exception {

		if (DynamicPropertyFactory.getBackingConfigurationSource() != null) {
			LOGGER.error("There was already a config source (or sources) configured.");
			throw new Exception(
					"Archaius is already configured with a property source/sources.");
		}
		
		ConcurrentCompositeConfiguration concurrentCompositeConfiguration = new ConcurrentCompositeConfiguration();
		
		// adding file or classpath properties to the Archaius 
		final String locationURL = location.getURL().toString();
		final DynamicURLConfiguration urlConfiguration = new DynamicURLConfiguration(
				initialDelayMillis, delayMillis, ignoreDeletesFromSource,
				locationURL);
		
		concurrentCompositeConfiguration.addConfiguration(urlConfiguration);
		
		//adding database tables to the Archaius  
		String dbURL = jdbcConnectionDetailMap.get("url");
		String driverClassName = jdbcConnectionDetailMap.get("driverClassName");
		String username= jdbcConnectionDetailMap.get("username"); 
		String password=jdbcConnectionDetailMap.get("password");
		String querry = jdbcConnectionDetailMap.get("querry");
		String property_key = jdbcConnectionDetailMap.get("property_key");
		String property_value = jdbcConnectionDetailMap.get("property_value");

		DriverManagerDataSource ds = new DriverManagerDataSource(
				driverClassName, dbURL, username, password); //TODO fix this here we have to suer name password.

		JDBCConfigurationSource source = new JDBCConfigurationSource(ds,
				querry, property_key, property_value);
		FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(
				0, 10, false);
		
		DynamicConfiguration dynamicConfiguration = new DynamicConfiguration(source,
				scheduler);
		
		
		concurrentCompositeConfiguration.addConfiguration(dynamicConfiguration);
		

		DynamicPropertyFactory.initWithConfigurationSource(concurrentCompositeConfiguration);
		
		return concurrentCompositeConfiguration;
	}


}
