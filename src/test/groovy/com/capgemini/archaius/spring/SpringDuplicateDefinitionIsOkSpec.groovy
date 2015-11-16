package com.capgemini.archaius.spring

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

/**
 * @author: Scott Rankin
 * @version: 1.0
 */
@ActiveProfiles('default')
@ContextConfiguration(locations = 'classpath:spring/springDuplicateDefinitionIsOkTest.xml')
class SpringDuplicateDefinitionIsOkSpec extends Specification {
    @Autowired
    @Qualifier('configOne')
    private final ArchaiusPropertyPlaceholderConfigurer configOne

    @Autowired
    @Qualifier('configTwo')
    private final ArchaiusPropertyPlaceholderConfigurer configTwo

    def 'Duplicate definitions OK and config one loads properties' () {
        expect:
            configOne.resolvePlaceholder('var2', null, 0) == 'MY SECOND VAR'
    }

    def 'Duplicate definitions OK and config two does not load its specified properties' () {
        expect:
            configTwo.resolvePlaceholder('var3', null, 0) == null
    }
}
