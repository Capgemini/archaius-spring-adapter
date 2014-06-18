package com.capgemini.archaius.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.spring.spi.BridgePropertyPlaceholderConfigurer;
import org.apache.commons.configuration.ConfigurationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicConfiguration;

public class ArchaiusJdbcBridgePropertyPlaceholderConfigurer extends
		BridgePropertyPlaceholderConfigurer {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArchaiusBridgePropertyPlaceholderConfigurer.class);

	private final ArchaiusSpringPropertyPlaceholderSupport propertyPlaceholderSupport = new ArchaiusSpringPropertyPlaceholderSupport();
	private boolean ignoreResourceNotFound;

	// settings for dynamic property configuration
	private int initialDelayMillis = 1000;
	private int delayMillis = 1000;
	private boolean ignoreDeletesFromSource = true;
	private String jdbcConnectionDetail = null;
	private String jdbcConnectionLocation = null;
	private Map<String, String> jdbcConnectionDetailMap = null;

	@Override
	public void setLocation(Resource location) {

		ConcurrentCompositeConfiguration concurrentCompositeConfiguration = null;

		try {

			if (jdbcConnectionDetailMap == null) {
				
				propertyPlaceholderSupport.setLocation(location, initialDelayMillis, delayMillis, ignoreDeletesFromSource);
				
				super.setLocation(location);
				
			} else {

				concurrentCompositeConfiguration = propertyPlaceholderSupport.setMixResourcesAsPropertySource(location,
								initialDelayMillis, delayMillis, ignoreDeletesFromSource, jdbcConnectionDetailMap);
				
				super.setProperties(ConfigurationConverter.getProperties(concurrentCompositeConfiguration));
			}

		} catch (Exception ex) {
			LOGGER.error("Problem setting the location.", ex);
			throw new RuntimeException("Problem setting the location.", ex);
		}
	}

	@Override
	public void setLocations(Resource[] locations) {
		ConcurrentCompositeConfiguration concurrentCompositeConfiguration = null;

		try {
			
			if (jdbcConnectionDetailMap == null) {
				
				propertyPlaceholderSupport.setLocations(locations, ignoreResourceNotFound, initialDelayMillis,
						delayMillis, ignoreDeletesFromSource);
				
				super.setLocations(locations);
				
			} else {
				
				propertyPlaceholderSupport.setMixResourcesAsPropertySource(	locations, ignoreResourceNotFound, initialDelayMillis,
						delayMillis, ignoreDeletesFromSource, jdbcConnectionDetailMap);
				
				super.setProperties(ConfigurationConverter.getProperties(concurrentCompositeConfiguration));
			}
			
		} catch (Exception ex) {
			LOGGER.error("Problem setting the locations", ex);
			throw new RuntimeException("Problem setting the locations.", ex);
		}
	}

	public void setJdbcConnectionDetail(String jdbcConnectionDetail) throws Exception {
		if (jdbcConnectionDetail != null) {
			jdbcConnectionDetailMap = createDatabaseKeyValueMap(jdbcConnectionDetail);

			DynamicConfiguration configuration = propertyPlaceholderSupport
					.setJdbcResourceAsPropetiesSource(jdbcConnectionDetailMap, initialDelayMillis, delayMillis, ignoreDeletesFromSource);

			super.setProperties(ConfigurationConverter.getProperties(configuration));
		}
	}

	public void setJdbcConnectionLocation(String jdbcConnectionLocation) throws Exception{
		if (jdbcConnectionLocation != null) {
			this.jdbcConnectionLocation = jdbcConnectionLocation;
			jdbcConnectionDetailMap = createDatabaseKeyValueMap(jdbcConnectionLocation);
		}
	}
	
	private Map<String, String> createDatabaseKeyValueMap(String jdbcUri) throws Exception {
		Map<String, String> jdbcMap = new HashMap<>();

		String delims = "[|][|]";
		
		if(jdbcUri==null){
			
			LOGGER.info("Argument passed Cant be null. ");
			LOGGER.error("The argument passes is not correct");
			LOGGER.error("Argument format to be passes is : driverClassName=<com.mysql.jdbc.Driver>||dbURL#<jdbc:mysql://localhost:3306/java>||username#<root>||password=<password>||sqlQuerry#s<elect distinct property_key, property_value from MySiteProperties>||keyColumnName#<property_key>||valueColumnName#<property_value>");
			throw new Exception("Parameter passed are not valid. See the Error log for more detail");
		}
		
		String[] tokens = jdbcUri.split(delims);

		if (tokens.length != 7) {
			LOGGER.info("Argument passed : " + jdbcUri);
			LOGGER.error("The argument passes is not correct");
			LOGGER.error("Argument format to be passes is : driverClassName=<com.mysql.jdbc.Driver>||dbURL#<jdbc:mysql://localhost:3306/java>||username#<root>||password=<password>||sqlQuerry#s<elect distinct property_key, property_value from MySiteProperties>||keyColumnName#<property_key>||valueColumnName#<property_value>");
			throw new Exception("Parameter passed are not valid. See the Error log for more detail");
		} else {

			delims = "[#]";
			for (String keyValue : tokens) {

				String[] keyAndValue = keyValue.split(delims);
				jdbcMap.put(keyAndValue[0], keyAndValue[1]);
			}
		}
		return jdbcMap;
	}
	
	@Override
	public void setIgnoreResourceNotFound(boolean setting) {
		ignoreResourceNotFound = setting;
		super.setIgnoreResourceNotFound(setting);
	}

	/**
	 * The initial delay before the property values are re-read from the
	 * location, in milliseconds
	 * 
	 * @param initialDelayMillis
	 */
	public void setInitialDelayMillis(int initialDelayMillis) {
		this.initialDelayMillis = initialDelayMillis;
	}

	/**
	 * Set the delay for the property values to re-read from the location, in
	 * milliseconds
	 * 
	 * @param delayMillis
	 */
	public void setDelayMillis(int delayMillis) {
		this.delayMillis = delayMillis;
	}

	/**
	 * Should the dynamic property loader ignore deletes from the location
	 * source.
	 * 
	 * @param ignoreDeletesFromSource
	 */
	public void setIgnoreDeletesFromSource(boolean ignoreDeletesFromSource) {
		this.ignoreDeletesFromSource = ignoreDeletesFromSource;
	}

	@Override
	protected String resolvePlaceholder(String placeholder, Properties props,
			int systemPropertiesMode) {
		return propertyPlaceholderSupport.resolvePlaceholder(placeholder,
				props, systemPropertiesMode);
	}
	
	public String getJdbcConnectionLocation() {
		return jdbcConnectionLocation;
	}
	
	public String getJdbcConnectionDetail() {
		return jdbcConnectionDetail;
	}

}
