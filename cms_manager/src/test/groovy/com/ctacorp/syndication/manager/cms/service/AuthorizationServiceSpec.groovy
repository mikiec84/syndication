
/*
Copyright (c) 2014, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  */
package com.ctacorp.syndication.manager.cms.service

import com.ctacorp.syndication.manager.cms.AuthorizationService
import com.ctacorp.syndication.manager.cms.rest.security.ApiKeyUtils
import com.ctacorp.syndication.manager.cms.rest.security.AuthorizationRequest
import com.ctacorp.syndication.manager.cms.rest.security.AuthorizationResult
import com.ctacorp.syndication.manager.cms.rest.security.GrailsHttpServletRequestConverter
import grails.test.mixin.TestFor
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

@TestFor(AuthorizationService)
class AuthorizationServiceSpec extends Specification {

    def apiKeyUtils = Mock(ApiKeyUtils)
    def converter = Mock(GrailsHttpServletRequestConverter)
    def httpRequest = Mock(HttpServletRequest)
    def authorizationRequest = new AuthorizationRequest()
    def authorizationResult = new AuthorizationResult()

    def setup() {

        service.apiKeyUtils = apiKeyUtils
        service.httpRequestConverter = converter
    }

    void "authorize an authorization request"() {

        when: "authorizing an authorization request"

        def result = service.authorize(authorizationRequest)

        then: "get an authorization result using api key utils"

        apiKeyUtils.buildAuthorizationResult(authorizationRequest) >> authorizationResult

        and: "the authorization result should be the same"

        result == authorizationResult
    }

    void "authorize an http request"() {

        when: "authorizing an authorization request"

        def result = service.authorize(httpRequest)

        then: "convert the http request to an authorization request"

        converter.convert(httpRequest) >> authorizationRequest

        and: "get an authorization result using api key utils"

        apiKeyUtils.buildAuthorizationResult(authorizationRequest) >> authorizationResult

        and: "the authorization result should be the same"

        result == authorizationResult
    }
}