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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
/**
 * 
 * @author skumar81
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:camel/derbyPropertiesLoadingTest.xml" })
@ActiveProfiles("default")
public class ArchaiusJdbcPropertiesLoadingTest {

	@Autowired
	@Qualifier("camel")
	protected CamelContext context;

    private final String propertyArchaiusKey = "Error400";
    private final String expectedArchaiusPropertyValue = "Bad Request";

   
	
	@Test 
	public void propertiesAreLoadedFromDatabaseAndAccessedViaArchaiusDynamicStringProperty(){
		
		DynamicStringProperty prop1 = DynamicPropertyFactory.getInstance().getStringProperty(propertyArchaiusKey, propertyArchaiusKey);
		
		assertThat(prop1.get(),is(equalTo(expectedArchaiusPropertyValue)) );
	}
	
}