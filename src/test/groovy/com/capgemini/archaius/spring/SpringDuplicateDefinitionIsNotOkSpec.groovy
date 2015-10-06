package com.capgemini.archaius.spring

import org.springframework.beans.factory.BeanCreationException
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

/**
 * @author: Scott Rankin
 * @version: 1.0
 */
@ActiveProfiles('default')
class SpringDuplicateDefinitionIsNotOkSpec extends Specification {

    def "missing spring properties files is not ok if IgnoreResourceNotFound property set to false" () {
        when:
            ctx = new ClassPathXmlApplicationContext('spring/springDuplicateDefinitionIsNotOkTest.xml')
        then:
            BeanCreationException bce = thrown()
            bce.cause.message == 'Failed properties: Property \'locations\' threw exception; nested exception is ' +
                    'java.lang.IllegalStateException: Archaius is already configured with a property source/sources.'
    }
}
