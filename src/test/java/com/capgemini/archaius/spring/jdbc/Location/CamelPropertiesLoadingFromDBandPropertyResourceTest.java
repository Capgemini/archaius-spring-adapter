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
package com.capgemini.archaius.spring.jdbc.Location;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

/**
 *
 * @author skumar81
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:archaiusJdbc/archaiusJdbcPropertiesLoadingTest.xml"})
@ActiveProfiles("default")
public class CamelPropertiesLoadingFromDBandPropertyResourceTest {

    private final String propertyKey = "var2";
    private final String nonExistentPropertyKey = "bad_key";
    private final String expectedPropertyValue = "MY SECOND VAR";
    private final String propertyArchaiusKey = "Error400";
    private final String expectedArchaiusPropertyValue = "Bad Request";
    
    @Autowired
    @Qualifier("camel")
    protected CamelContext context;
    @Value("${" + propertyKey + "}") private String springPropertyValue;
    
    @Value("${" + propertyArchaiusKey + "}") private String springArchaiusPropertyValue;
    
    @Test 
	public void propertiesAreLoadedFromDatabaseAndAccessedViaArchaiusDynamicStringProperty() throws InterruptedException{
		
		DynamicStringProperty prop1 = DynamicPropertyFactory.getInstance().getStringProperty(propertyArchaiusKey, propertyArchaiusKey);
		
		assertThat(prop1.get(),is(equalTo(expectedArchaiusPropertyValue)) );
	}
    
    @DirtiesContext
    @Test
    public void camelPropertiesAreLoadedFromSingleFileAndAccessedViaTheCamelValueAnnotation() throws Exception {
    	
        String camelPropertyValue = context.resolvePropertyPlaceholders("{{" + propertyKey + "}}");
        
        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValue, is(equalTo(expectedPropertyValue)));
    }
    
    @Test
    public void springPropertiesAreAlsoLoadedOKFromSingleFileAndAccessedViaTheSpringValueAnnotation() throws InterruptedException {
        assertThat(springPropertyValue, equalTo(expectedPropertyValue));
        assertThat(springArchaiusPropertyValue, is(equalTo(expectedArchaiusPropertyValue)));
    }
    
    @Test
    public void nonExistentPropertiesWhenrequestedViaCamelThrowIllegalArgumentExceptions() throws InterruptedException {
        try {
            context.resolvePropertyPlaceholders("{{" + nonExistentPropertyKey + "}}");
            fail("An IllegalArgument Exception shound be thrown");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is(equalTo("Property with key [" + nonExistentPropertyKey + "] not found in properties from text: {{" + nonExistentPropertyKey + "}}")));
        }catch (Exception ex) {
            fail("All other exceptions should not be thrown: " + ex.getMessage());
        }
    }
}
