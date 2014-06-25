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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.capgemini.archaius.spring.jdbc.dataload.UpdateTestDataForArchaiusTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.not;

/**
 *
 * @author Sanjay Kumar
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:camel/jdbc/camelJdbcPropertiesLoadingTest.xml"})
@ActiveProfiles("default")
public class CamelJdbcPropertiesLoadingTest extends JdbcTestSuper {

    @Autowired
    @Qualifier("camel")
    protected CamelContext context;

    private final String propertyArchaiusKeyTwo = "Error404";
    private final String expectedArchaiusPropertyValueTwo = "Page not found";

    private final String propertyArchaiusKeyThree = "Error500";
    private final String expectedArchaiusPropertyValueThree = "Internal Server Error";

    private final String newArchaiusPropertyKeyTwo = "Error404";
    private final String newExpectedArchaiusPropertyValueTwo = "New Page not found";

    private final String newArchaiusPropertyKeyThree = "Error500";
    private final String newExpectedArchaiusPropertyValueThree = "New Internal Server Error";

    @DirtiesContext
    @Test
    public void propertiesAreLoadedFromDatabaseAndAccessedViaCamelValueAnnotation() throws Exception {
        //data loded from the DB using Archaius
        String camelPropertyValueTwo = context.resolvePropertyPlaceholders("{{" + propertyArchaiusKeyTwo + "}}");
        String camelPropertyValueThree = context.resolvePropertyPlaceholders("{{" + propertyArchaiusKeyThree + "}}");

        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueTwo, is(equalTo(expectedArchaiusPropertyValueTwo)));

        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueThree, is(equalTo(expectedArchaiusPropertyValueThree)));

        // when updating the data in DB
        UpdateTestDataForArchaiusTest updateTestData = new UpdateTestDataForArchaiusTest();
        updateTestData.initializedDerby();
        Thread.sleep(100);

        //then  still camel context will have old data not the new values
        camelPropertyValueTwo = context.resolvePropertyPlaceholders("{{" + newArchaiusPropertyKeyTwo + "}}");
        camelPropertyValueThree = context.resolvePropertyPlaceholders("{{" + newArchaiusPropertyKeyThree + "}}");

        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueTwo, is(equalTo(expectedArchaiusPropertyValueTwo)));
        assertThat(camelPropertyValueTwo, is(not(newExpectedArchaiusPropertyValueTwo)));

        assertThat("The context cannot be null.", context != null);
        assertThat(camelPropertyValueThree, is(equalTo(expectedArchaiusPropertyValueThree)));
        assertThat(camelPropertyValueThree, is(not(newExpectedArchaiusPropertyValueThree)));
    
    }
}
