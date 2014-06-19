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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.capgemini.archaius.spring.jdbc.dataload.DeleteTestDataAndSchemaForArchaiusTest;
import com.capgemini.archaius.spring.jdbc.dataload.ResetTestDataForArchaiusTest;
import com.capgemini.archaius.spring.jdbc.dataload.UpdateTestDataForArchaiusTest;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

/**
 * 
 * @author Sanjay Kumar.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:archaiusJdbc/derbyPropertiesLoadingTest.xml" })
@ActiveProfiles("default")
public class ArchaiusJdbcPropertiesLoadingTest {

	@Autowired
	@Qualifier("camel")
	protected CamelContext context;
	
	private final String propertyArchaiusKeyOne = "Error400";
	private final String expectedArchaiusPropertyValueOne = "Bad Request";

	private final String propertyArchaiusKeyTwo = "Error404";
	private final String expectedArchaiusPropertyValueTwo = "Page not found";

	private final String propertyArchaiusKeyThree = "Error500";
	private final String expectedArchaiusPropertyValueThree = "Internal Server Error";
	
	private final String newArchaiusPropertyKeyOne = "Error400";
	private final String newExpectedArchaiusPropertyValueOne = "New Bad Request";

	private final String newArchaiusPropertyKeyTwo = "Error404";
	private final String newExpectedArchaiusPropertyValueTwo = "New Page not found";

	private final String newArchaiusPropertyKeyThree = "Error500";
	private final String newExpectedArchaiusPropertyValueThree = "New Internal Server Error";
	
	public Logger LOGGER = LoggerFactory.getLogger(ArchaiusJdbcPropertiesLoadingTest.class);
	
	
	
	@Test
	public void propertiesAreLoadedFromDatabaseAndAccessedViaArchaiusDynamicStringProperty() throws InterruptedException {
		
		// when  initial value at set in DB
		ResetTestDataForArchaiusTest resetTestData=new ResetTestDataForArchaiusTest();
		resetTestData.initializedDerby();
		Thread.sleep(100);

		LOGGER.info("runnig test for initial values");
		// then  initial value should be retrieved from DB.
		DynamicStringProperty prop1 = DynamicPropertyFactory.getInstance().getStringProperty(propertyArchaiusKeyOne, propertyArchaiusKeyOne);

		assertThat(prop1.get(), is(equalTo(expectedArchaiusPropertyValueOne)));

		DynamicStringProperty prop2 = DynamicPropertyFactory.getInstance().getStringProperty(propertyArchaiusKeyTwo, propertyArchaiusKeyTwo);

		assertThat(prop2.get(), is(equalTo(expectedArchaiusPropertyValueTwo)));

		DynamicStringProperty prop3 = DynamicPropertyFactory.getInstance().getStringProperty(propertyArchaiusKeyThree, propertyArchaiusKeyThree);

		assertThat(prop3.get(), is(equalTo(expectedArchaiusPropertyValueThree)));
		
		// when  updated the value in db
		UpdateTestDataForArchaiusTest updateTestData=new UpdateTestDataForArchaiusTest();
		updateTestData.initializedDerby();
		Thread.sleep(100);
		
		LOGGER.info("runnig test for updated values");
		// then   new value should be reflected.
		prop1 = DynamicPropertyFactory.getInstance().getStringProperty(newArchaiusPropertyKeyOne, newArchaiusPropertyKeyOne);

		assertThat(prop1.get(), is(equalTo(newExpectedArchaiusPropertyValueOne)));

		prop2 = DynamicPropertyFactory.getInstance().getStringProperty(newArchaiusPropertyKeyTwo, newArchaiusPropertyKeyTwo);

		assertThat(prop2.get(), is(equalTo(newExpectedArchaiusPropertyValueTwo)));

		prop3 = DynamicPropertyFactory.getInstance().getStringProperty(newArchaiusPropertyKeyThree, newArchaiusPropertyKeyThree);

		assertThat(prop3.get(), is(equalTo(newExpectedArchaiusPropertyValueThree)));
		
		//resetting the data to initial value
		ResetTestDataForArchaiusTest resetData=new ResetTestDataForArchaiusTest();
		resetData.initializedDerby();
		Thread.sleep(100);
		
		//shutting down the in memory database.
		DeleteTestDataAndSchemaForArchaiusTest deleteDB= new DeleteTestDataAndSchemaForArchaiusTest();
		deleteDB.deleteDatabase();
		
	}

}