[ ![Download](https://api.bintray.com/packages/capgeminiuk/maven/archaius-spring-adapter/images/download.svg) ](https://bintray.com/capgeminiuk/maven/archaius-spring-adapter/_latestVersion)

Archaius Spring Adapter
===============================

Welcome to the archaius-spring-adapter. As we say in the POM, our aim is to 
simply extend the Spring (and Camel) PropertyPlaceholders in order to support 
Netflix's Archaius as the single source of all property information.

Why would we go to this trouble?  Properties can be loaded fine into Spring, and
now, with the BridgePropertyPlaceholder from Camel you can use that same file 
for your Camel properties too.

But what if you want more? Specifically, the itch that we scratched
was the desire to use the Netflix Hystrix circuit breakers in our Spring/Camel 
projects.  These depend upon Archaius for their configuration, and as you need to tune
them, this configuration is important.  Having already got all our Camel and Spring
properties in one place, why would we want to accept _another_ properties source just for one 
more component in our architecture? After some googling, the archaius-spring-adapter was born.

At this point, it should be noted right up front that while the itch for this 
scratching was ours, we weren't alone. The initial example upon 
which this code is based can be seen in this Archaius issue thread: 

https://github.com/Netflix/archaius/issues/113

We'd like to thank the guys there for their support, especially @mumrah whose gist kicked this all off and @chriswhitcombe for his technical input. We'd also like to thank them for the permission to release this code licensed under the Apache v2.0 OSS licence.

Getting Started
---------------

It's dead simple to use the adapter. Just download one of the releases from this repo, or 
add our maven bintray repo (find the details in pom.xml) to your maven project. 

Now you're free to start bringing in the archaius goodness.  We're (still) using
Spring XML config and all you need do if you have Spring alone (i.e. no Camel)
is something like:

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:context="http://www.springframework.org/schema/context"
           xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                               http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.1.xsd">

        <!-- Config loading via Spring-Archaius-->
        <bean class="com.capgemini.archaius.spring.ArchaiusPropertyPlaceholderConfigurer">
            <property name="location" value="classpath:/META-INF/system.properties" />
        </bean>

    </beans>

The "location" property works just as you would expect it to.  By this we mean 
you can have an ordered list of properties files:

    <!-- Config loading via Spring-Archaius-->
    <bean class="com.capgemini.archaius.spring.ArchaiusPropertyPlaceholderConfigurer">
        <property name="locations">
            <list>
                <value>classpath:/META-INF/system.properties</value>
                <value>classpath:/META-INF/even-more-system.properties</value>
            </list>
        </property>
    </bean>

With property overloading as you'd expect from standard Spring properties.

Additionally, and again as Spring users will expect, you can let Spring know whether 
it can ignore missing resource files:

    <bean class="com.capgemini.archaius.spring.ArchaiusPropertyPlaceholderConfigurer">
        <property name="ignoreResourceNotFound" value="false" />
        <property name="locations">
            <list>
                <value>classpath:/META-INF/system.properties</value>
                <value>classpath:/META-INF/file-not-there.properties</value>
            </list>
        </property>
    </bean>

If you're using Camel, everything in the above examples works as you'd expect, 
but you need to use a different bean class:

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:context="http://www.springframework.org/schema/context"
           xmlns:camel="http://camel.apache.org/schema/spring"
           xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                               http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
                               http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring-2.12.2.xsd">

        <!-- Config loading via Spring-Archaius-->
        <bean class="com.capgemini.archaius.spring.ArchaiusBridgePropertyPlaceholderConfigurer">
            <property name="locations">
                <list>
                    <value>classpath:/META-INF/system.properties</value>
                    <value>classpath:/META-INF/file-not-there.properties</value>
                </list>
            </property>
            <property name="ignoreResourceNotFound" value="true" />
        </bean>

        <camel:camelContext id="camel" />

    </beans>

That's it!

Pushing Things a Little Further
-------------------------------
Archaius can do quite a few clever things, and we wanted to support them but couldn't manage it without "extending" the standard Spring idioms a little. 

First up is property polling.  You can read more about the details of this over at the 
Archaius project (https://github.com/Netflix/archaius), but to get things going, all you need add to your spring XMl file is the following enclosed within the standard propertyPlaceholder "bean" tags:

    <property name="initialDelayMillis" value="1" />
    <property name="delayMillis" value="10" />
    <property name="ignoreDeletesFromSource" value="false" />
    
Notes: 
* this works with both the Spring and Camel-bridge placeholders.
* this doesn't (currently) make Spring or Camel properties dynamic, as that's a fair old task to implement, but you can get access to your properties in your code via the standard Archaius methods, and these properties _will_ be dynamic.

Second is storing properties in a JDBC-accessed datastore. To do this, you simply need to add this line to your Spring XML:

    <property name="jdbcLocation" value="driverClassName#org.apache.derby.jdbc.EmbeddedDriver||dbURL#jdbc:derby:memory:jdbcDemoDB;create=false||username#admin||password#nimda||sqlQuery#select distinct property_key, property_value from MYSITEPROPERTIES||keyColumnName#property_key||valueColumnName#property_value"  />

We'll put up more documentation on this when we get a chance, but thee URL format is pretty self-explanatory.

Notes:
* this also works with both the Spring and Camel-bridge placeholders.
* it also works with the dynamic polling support detailed above

Third is adding in system properties (as read by Apache Commons [System Configuration](https://commons.apache.org/proper/commons-configuration/javadocs/v1.10/apidocs/org/apache/commons/configuration/SystemConfiguration.html) ). 
This is really helpful if you want to override properties in Archaius from the command line.  You can enable this behavior by setting the following property in your Spring XML:
    
    <property name="includeSystemConfiguration" value="true"/>

This will read any properties from the command line (passed via -D parameters) and add them in at the end, meaning they override any properties defined in the 
other property sources.  

Getting Involved
----------------

We're patently working on support tpo solve our own problems first, but it's also clear that 
there is much more we could add to this project.  We'd love to have contributions from folks in all the standard ways:

1. Questions and Answers via the Google Group - https://groups.google.com/forum/#!forum/archaius-spring-adapter
1. Issue Reports via GitHub
1. Pull Requests (fixes, more tests, new features, typo-corrections etc) via GitHub (we follow the standard workflow)
1. Wiki documentation

We maintain a list of the issues that we're working on as you'd expect.  By
looking there you can see what our priorities are.  Please feel free to comment
on any of them or add more.

Users
-----
We love to know who uses archaius-spring-adapter.  If you are a user, please add yourself to the list in the USERS.md page via a pull request.

Developers
----------

* [Andrew Harmel-Law](https://github.com/andrewharmellaw)
* [Gaythu Rajan](https://github.com/gaythu-rajan)
* [Nick Walter](https://github.com/nickjwalter)
* [Russell Hart](https://github.com/rhart)
* [Sanjay Kumar](https://github.com/sanjaykumar81)
* [arsenaid](https://github.com/arsenaid)
* [Scott D.W. Rankin](https://github.com/sdwr98)
