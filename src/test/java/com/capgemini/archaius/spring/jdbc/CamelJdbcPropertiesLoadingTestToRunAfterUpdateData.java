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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
/**
 * 
 * @author skumar81
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:archaiusJdbc/derbyPropertiesLoadingTest.xml" })
@ActiveProfiles("default")
public class CamelJdbcPropertiesLoadingTestToRunAfterUpdateData {

	@Autowired
	@Qualifier("camel")
	protected CamelContext context;

	private final String propertyArchaiusKeyOne = "Error400";
	private final String expectedArchaiusPropertyValueOne = "New Bad Request";

	private final String propertyArchaiusKeyTwo = "Error404";
	private final String expectedArchaiusPropertyValueTwo = "New Page not found";

	private final String propertyArchaiusKeyThree = "Error500";
	private final String expectedArchaiusPropertyValueThree = "New Internal Server Error";

    @DirtiesContext
    @Test
    public void propertiesAreLoadedFromDatabaseAndAccessedViaCamelValueAnnotation() throws Exception {
        
        String camelPropertyValueOne = context.resolvePropertyPlaceholders("{{" + propertyArchaiusKeyOne + "}}");
        String camelPropertyValueTwo = context.resolvePropertyPlaceholders("{{" + propertyArchaiusKeyTwo + "}}");
        String camelPropertyValueThree = context.resolvePropertyPlaceholders("{{" + propertyArchaiusKeyThree + "}}");
        
        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueOne, is(equalTo(expectedArchaiusPropertyValueOne)));
        
        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueTwo, is(equalTo(expectedArchaiusPropertyValueTwo)));
        
        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueThree, is(equalTo(expectedArchaiusPropertyValueThree)));
    }

}
