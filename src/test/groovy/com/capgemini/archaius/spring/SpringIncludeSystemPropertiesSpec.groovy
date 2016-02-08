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
package com.capgemini.archaius.spring

import com.netflix.config.DynamicPropertyFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PropertiesLoaderUtils
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author Scott Rankin (srankin@motus.com)
 */
@ContextConfiguration(locations = 'classpath:spring/springIncludeSystemConfigurationTest.xml')
@ActiveProfiles('default')
class SpringIncludeSystemPropertiesSpec extends Specification {

    // TODO Spring @Configuration tests
    // TODO Spring @Environment.getProperty tests

    private final String propertyKey = 'var2'
    private final String expectedPropertyValue = 'MY SECOND VAR'
    private final String systemPropertyKey = 'var3'
    private final String expectedSystemPropertyValue = 'MY THIRD VAR'
    private final String systemPropertyOverrideKey = 'varSystem'
    private final String expectedSystemPropertyOverrideValue = 'override'

    @SuppressWarnings('GStringExpressionWithinString')
    @Value('${var2}') private final String propertyValue
    private String systemPropertyValue

    def setup() throws IOException, InterruptedException {
        System.setProperty(systemPropertyKey, expectedSystemPropertyValue)
        System.setProperty(systemPropertyOverrideKey, 'override')
    }

    def "can load Spring properties from a single file and access via an annotation"() {
        expect:
            propertyValue == expectedPropertyValue
    }

    def "can also load System properties from the configuration"() {
        when: 'check system value exists'
            systemPropertyValue = DynamicPropertyFactory.instance.getStringProperty(systemPropertyKey, null).value
        then:
            systemPropertyValue == expectedSystemPropertyValue

        when: 'system value overrides all other values'
            systemPropertyValue =
                DynamicPropertyFactory.instance.getStringProperty(systemPropertyOverrideKey, null).value
        then:
            systemPropertyValue == expectedSystemPropertyOverrideValue
    }

    /**
     * Of course this works as we're just testing Spring - instead we need to document that you can't do this any more
     * as it will ignore Archaius changes.
     */
    def "can load Spring properties from a single file and access via properties loader util"() {
        given:
            Resource resource = new ClassPathResource('properties/system.properties')

        when:
            Properties props = PropertiesLoaderUtils.loadProperties(resource)

        then:
            props.containsKey(propertyKey)
    }
}
