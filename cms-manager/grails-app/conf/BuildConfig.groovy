/*
Copyright (c) 2014, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
grails.servlet.version = "3.0"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.fork = [
        test: false,
        run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve: false],
        war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve: false],
        console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {

    inherits("global") {}

    log "error"
    checksums true
    legacyResolve false

    repositories {
        mavenRepo("http://54.234.21.193:8080/artifactory/central") {
            updatePolicy "always"
        }

        mavenLocal()

        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()

        mavenRepo "http://repo.grails.org/grails/core"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
    }

    dependencies {

        test 'org.hamcrest:hamcrest-all:1.3'
        test 'org.objenesis:objenesis:1.4'
        test 'cglib:cglib:2.2'
        test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"
        test 'nl.flotsam:xeger:1.0'

        runtime 'mysql:mysql-connector-java:5.1.38'
        runtime 'com.ctacorp.commons:multi-read-servlet-filter:1.0.0'

        compile 'org.springframework:spring-web:4.2.1.RELEASE'
        compile 'org.bouncycastle:bcprov-jdk16:1.46'
        compile 'com.ctacorp.commons:api-key-utils:1.6.0'
        compile 'com.ctacorp:syndication-commons:1.3.0'
        compile ('com.ctacorp.syndication:rsrw:0.1.2') {
            excludes "jboss-common"
        }
        compile 'com.ctacorp.syndication:syndication-java-sdk-v2:0.1.6'
        compile 'com.ctacorp.syndication:syndication-swagger-rest-client-v2:0.1.6'
        compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.7.1'
        compile "com.google.guava:guava:18.0"
    }

    plugins {

        build ':tomcat:8.0.22'
        build (":release:3.1.1"){
            excludes "rest-client-builder"
        }

        compile ":scaffolding:2.1.2"
        compile ":cache:1.1.8"
        compile ":spring-security-core:2.0.0"
        compile ":quartz:1.0.2"
        compile ":mail:1.0.7"
        compile ":rest-client-builder:2.1.1"
        compile ":asset-pipeline:2.6.5"
        compile ":less-asset-pipeline:2.3.0"
        compile ":rabbitmq-native:3.1.2"                       //mq
        compile ":greenmail:1.3.4"
        compile ":bruteforce-defender:1.1"

        runtime ":hibernate4:4.3.8.1"
        runtime ":database-migration:1.4.0"
        runtime ":jquery:1.11.1"

        test ":build-test-data:2.4.0"
    }
}