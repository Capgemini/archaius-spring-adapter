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
package com.capgemini.archaius.spring.jdbc;

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
@ContextConfiguration(locations = { "classpath:archaiusJdbc/derbyPropertiesLoadingTest.xml" })
@ActiveProfiles("default")
public class ArchaiusJdbcPropertiesLoadingTestToRunAfterUpdateData {

	@Autowired
	@Qualifier("camel")
	protected CamelContext context;

	private final String newArchaiusPropertyKeyOne = "Error400";
	private final String newExpectedArchaiusPropertyValueOne = "New Bad Request";

	private final String newArchaiusPropertyKeyTwo = "Error404";
	private final String newExpectedArchaiusPropertyValueTwo = "New Page not found";

	private final String newArchaiusPropertyKeyThree = "Error500";
	private final String newExpectedArchaiusPropertyValueThree = "Internal Server Error";

	@Test
	public void updatedPropertiesAreLoadedFromDatabaseAndAccessedViaArchaiusDynamicStringProperty() {

		DynamicStringProperty prop1 = DynamicPropertyFactory.getInstance().getStringProperty(newArchaiusPropertyKeyOne, newArchaiusPropertyKeyOne);

		assertThat(prop1.get(), is(equalTo(newExpectedArchaiusPropertyValueOne)));

		DynamicStringProperty prop2 = DynamicPropertyFactory.getInstance().getStringProperty(newArchaiusPropertyKeyTwo, newArchaiusPropertyKeyTwo);

		assertThat(prop2.get(), is(equalTo(newExpectedArchaiusPropertyValueTwo)));

		DynamicStringProperty prop3 = DynamicPropertyFactory.getInstance().getStringProperty(newArchaiusPropertyKeyThree, newArchaiusPropertyKeyThree);

		assertThat(prop3.get(), is(equalTo(newExpectedArchaiusPropertyValueThree)));
	}


   
	
	
	
}
