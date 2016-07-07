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

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicStringProperty;
import org.apache.commons.configuration.ConfigurationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.*;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author Andrew Harmel-Law
 */
public class ArchaiusPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchaiusPropertyPlaceholderConfigurer.class);
    
    public static final int DEFAULT_DELAY = 1000;
    private transient int initialDelayMillis = DEFAULT_DELAY;
    private transient int delayMillis = DEFAULT_DELAY;
    private transient boolean ignoreResourceNotFound = false;
    private transient boolean ignoreDeletesFromSource = true;
    private transient boolean allowMultiplePlaceholders = false;
    private transient boolean includeSystemConfiguration = false;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final transient ArchaiusSpringPropertyPlaceholderSupport propertyPlaceholderSupport 
            = new ArchaiusSpringPropertyPlaceholderSupport();
    private transient Map<String, String> jdbcConnectionDetailMap = null;
    //Only for programmable purpose
    private String beanName;
    private BeanFactory beanFactory;

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

    /**
     * Should the system allow duplicate beans and just read from the initial one? 
     * This helps in the case where you want to define beans in both a parent web application
     * context and a servlet-specific context
     */
    public void setAllowMultiplePlaceholders(boolean allowMultiplePlaceholders) {
        this.allowMultiplePlaceholders = allowMultiplePlaceholders;
    }
    
    /**
     * If set to true, includes a {@link org.apache.commons.configuration.SystemConfiguration} in the 
     * configuration set to allow overriding/setting of parameters from the command line. 
     * Defaults to false.
     * 
     * @param includeSystemConfiguration
     */
    public void setIncludeSystemConfiguration(boolean includeSystemConfiguration) {
        this.includeSystemConfiguration = includeSystemConfiguration;
    }
    
    @Override
    public void setIgnoreResourceNotFound(boolean setting) {
        ignoreResourceNotFound = setting;
        super.setIgnoreResourceNotFound(setting);
    }
    
    @Override
    protected String resolvePlaceholder(final String placeholder, Properties props, int systemPropertiesMode) {
        final DynamicStringProperty property = propertyPlaceholderSupport.resolvePlaceholder(placeholder, props, systemPropertiesMode);
        property.addCallback(new Runnable() {
            @Override
            public void run() {
                resetPropertyValue(placeholder, property);
            }
        });

        return property.get();
    }

    /**
     * Archaius JDBC Connection URI.
     * 
     * @param jdbcLocation the URI from the jdbcLocation property in the Spring config
     */
    public void setJdbcLocation(String jdbcLocation) {
        jdbcConnectionDetailMap = propertyPlaceholderSupport.extractJdbcParameters(jdbcLocation);
    }
    
    @Override
    public void setLocation(Resource location) {
        Resource[] locations = { location };
        setLocations(locations);
    }
    
    @Override
    public void setLocations(Resource[] locations) {
        try {        
            Map parameterMap = propertyPlaceholderSupport.getParameterMap(delayMillis, initialDelayMillis, ignoreDeletesFromSource, 
                                                                          ignoreResourceNotFound, allowMultiplePlaceholders, includeSystemConfiguration);
            
            // If there is not also a JDBC properties location to consider
            if (jdbcConnectionDetailMap == null) {
                propertyPlaceholderSupport.setLocations(parameterMap, locations);
                super.setLocations(locations);
            } else {
                ConcurrentCompositeConfiguration conComConfiguration = propertyPlaceholderSupport.setMixedResourcesAsPropertySources(parameterMap, locations, jdbcConnectionDetailMap);
                super.setProperties(ConfigurationConverter.getProperties(conComConfiguration));
            }
        } catch (IOException ex) {
            LOGGER.error("Problem setting the locations", ex);
            throw new RuntimeException("Problem setting the locations.", ex);
        }
    }

    private class PlaceholderResolvingBeanDefinitionVisitor extends BeanDefinitionVisitor {

        private final Properties props;

        public PlaceholderResolvingBeanDefinitionVisitor(Properties props) {
            this.props = props;
        }

        protected void visitPropertyValues(MutablePropertyValues pvs) {
            PropertyValue[] pvArray = pvs.getPropertyValues();
            for (PropertyValue pv : pvArray) {
                Object newVal = resolveValue(pv.getValue());
                if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
                    pvs.addPropertyValue(pv.getName(), newVal);
                }
            }
        }

        protected String resolveStringValue(String strVal) throws BeansException {
            return parseStringValue(strVal, this.props, new HashSet());
        }
    }
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException{
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        BeanDefinitionVisitor visitor = new PlaceholderResolvingBeanDefinitionVisitor(props);
        for (String beanDefinitionName : beanDefinitionNames){
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            beanDefinitionMap.put(beanDefinitionName, beanDefinition);

            if (!(beanDefinitionName.equals(this.beanName) && beanFactory.equals(this.beanFactory))){
                try {
                    visitor.visitBeanDefinition(beanDefinition);
                }catch (BeanDefinitionStoreException e){
                    throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanDefinitionName,
                            e.getMessage());
                }
            }
        }
    }

    @Override
    public void setBeanName(String beanName){
        this.beanName = beanName;
        super.setBeanName(beanName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        super.setBeanFactory(beanFactory);
    }


    private void resetPropertyValue(String placeholder, DynamicStringProperty property){
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
}
