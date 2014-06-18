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

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.capgemini.archaius.spring.jdbc.dataload.DeleteTestDataAndSchemaForArchaiusTest;
import com.capgemini.archaius.spring.jdbc.dataload.ResetTestDataForArchaiusTest;
import com.capgemini.archaius.spring.jdbc.dataload.UpdateTestDataForArchaiusTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.not;

/**
 * 
 * @author  Sanjay Kumar
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:archaiusJdbc/derbyPropertiesLoadingTest.xml" })
@ActiveProfiles("default")
public class CamelJdbcPropertiesLoadingTest {

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
	
    @DirtiesContext
    @Test
    public void propertiesAreLoadedFromDatabaseAndAccessedViaCamelValueAnnotation() throws Exception {
    	//data loded from the DB using Archaius
        String camelPropertyValueOne = context.resolvePropertyPlaceholders("{{" + propertyArchaiusKeyOne + "}}");
        String camelPropertyValueTwo = context.resolvePropertyPlaceholders("{{" + propertyArchaiusKeyTwo + "}}");
        String camelPropertyValueThree = context.resolvePropertyPlaceholders("{{" + propertyArchaiusKeyThree + "}}");
        
        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueOne, is(equalTo(expectedArchaiusPropertyValueOne)));
        
        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueTwo, is(equalTo(expectedArchaiusPropertyValueTwo)));
        
        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueThree, is(equalTo(expectedArchaiusPropertyValueThree)));
        
        // when updating the data in DB
     	UpdateTestDataForArchaiusTest updateTestData=new UpdateTestDataForArchaiusTest();
     	updateTestData.initializedDerby();
     	Thread.sleep(100);
     	
     	//then  still camel context will have old data not the new values
     	 camelPropertyValueOne = context.resolvePropertyPlaceholders("{{" + newArchaiusPropertyKeyOne + "}}");
         camelPropertyValueTwo = context.resolvePropertyPlaceholders("{{" + newArchaiusPropertyKeyTwo + "}}");
         camelPropertyValueThree = context.resolvePropertyPlaceholders("{{" + newArchaiusPropertyKeyThree + "}}");
         
         assertThat("The context cannot be null.", context != null);
         assertThat(camelPropertyValueOne, is( equalTo(expectedArchaiusPropertyValueOne)));
         assertThat(camelPropertyValueOne, is( not(newExpectedArchaiusPropertyValueOne)));
         
         assertThat("The context cannot be null.", context != null);
         assertThat(camelPropertyValueTwo, is(equalTo(expectedArchaiusPropertyValueTwo)));
         assertThat(camelPropertyValueTwo, is(not(newExpectedArchaiusPropertyValueTwo)));
         
         assertThat("The context cannot be null.", context != null);
         assertThat(camelPropertyValueThree, is(equalTo(expectedArchaiusPropertyValueThree)));
         assertThat(camelPropertyValueThree, is(not(newExpectedArchaiusPropertyValueThree)));
         
     	//resetting the data to initial value
 		ResetTestDataForArchaiusTest resetData=new ResetTestDataForArchaiusTest();
 		resetData.initializedDerby();
 		Thread.sleep(100);
 		
        //shutting down the in memory database.
 		 DeleteTestDataAndSchemaForArchaiusTest deleteDB= new DeleteTestDataAndSchemaForArchaiusTest();
		 deleteDB.deleteDatabase();
    }
}
