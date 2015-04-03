package com.ctacorp.syndication

/*
Copyright (c) 2014, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.ctacorp.commons.api.key.utils.AuthorizationHeaderGenerator
import com.icegreen.greenmail.imap.AuthorizationException
import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder

import javax.annotation.PostConstruct

class AuthorizationService {
    static transactional = false

    def grailsApplication

    private AuthorizationHeaderGenerator generator
    private AuthorizationHeaderGenerator.KeyAgreement keyAgreement
    private RestBuilder rest= new RestBuilder()

    @PostConstruct
    void init() {
        String privateKey = grailsApplication.config.cmsManager.privateKey
        String publicKey = grailsApplication.config.cmsManager.publicKey
        String secret = grailsApplication.config.cmsManager.secret
        if (privateKey && publicKey && secret) {
            rest
            keyAgreement = new AuthorizationHeaderGenerator.KeyAgreement()

            keyAgreement.setPrivateKey(privateKey)
            keyAgreement.setPublicKey(publicKey)
            keyAgreement.setSecret(secret)

            generator = new AuthorizationHeaderGenerator("syndication_api_key", keyAgreement)
        }
    }

    String hashBody(String body){
        generator.hashData(body)
    }

    boolean checkAuthorization(Map thirdPartyRequest) {
        def authorizationRequest = (thirdPartyRequest as JSON).toString()
        log.debug "The authorizationRequest is \n${authorizationRequest}"

        String date = new Date().toString()
        String requestUrl = grailsApplication.config.cmsManager.serverUrl + grailsApplication.config.cmsManager.verifyAuthPath
        log.debug "The requestUrl is ${requestUrl}"
        String apiKeyHeaderValue = generator.getApiKeyHeaderValue([
                date: date,
                "content-type": "application/json",
                "content-length": authorizationRequest.bytes.size() as String
        ],
                requestUrl, "POST", authorizationRequest)

        def resp = rest.post(requestUrl) {
            header 'Date', date
            header 'Authorization', apiKeyHeaderValue

            json thirdPartyRequest
        }

        resp.status == 204
    }

    private sendAuthorizedRequest(String url, String body, String method){
        def requestHeaders = [
                'Date': new Date().toString(),
                'Accept': 'application/json'
        ]
        if (body) {
            requestHeaders['Content-Type'] = "application/json"
            requestHeaders['Content-Length'] = body.bytes.size() as String
        }

        log.info "Making authorized request -------------------"
        log.info "headers: $requestHeaders"
        log.info "url: $url"
        log.info "method: $method"
        log.info "body: $body"

        def apiKeyHeaderValue = generator.getApiKeyHeaderValue(requestHeaders, url, method, body)
        log.info apiKeyHeaderValue
        def resp

        switch (method) {
            case "POST":
                resp = rest.post(url) {
                    header 'Date', requestHeaders.Date
                    header 'Authorization', apiKeyHeaderValue
                    accept "application/json"

                    json body
                }
                break
            case "DELETE":
                resp = rest.delete(url) {
                    header 'Date', requestHeaders.Date
                    header 'Authorization', apiKeyHeaderValue
                }
                break
            case "GET":
                resp = rest.get(url) {
                    header 'Date', requestHeaders.Date
                    header 'Authorization', apiKeyHeaderValue
                    accept "application/json"
                }
                break
            default: break; //do nothing
        }

        if (resp?.status == 403) {
            String responseDetails = "Status code:${resp.status}\nJsonBody: ${resp.json}"
            log.error(responseDetails)
            throw new AuthorizationException("Access Denied - Your authorization keys have been denied.")
        }

        resp.json
    }

    def post(String url, params = [:]){
        sendAuthorizedRequest(url, (params as JSON).toString(), "POST")
    }

    def getRest(String url){
        sendAuthorizedRequest(url, null, "GET")
    }

    def delete(String url){
        sendAuthorizedRequest(url, null, "DELETE")
    }

    boolean amIAuthorized() {
        String date = new Date().toString()
        String requestUrl = grailsApplication.config.cmsManager.serverUrl + grailsApplication.config.cmsManager.selfAuthPath
        String apiKeyHeaderValue = generator.getApiKeyHeaderValue([date: date], requestUrl, "GET", null)
        def resp = rest.get(requestUrl) {
            header 'Date', date
            header 'Authorization', apiKeyHeaderValue
        }

        resp.json.isSecure as Boolean
    }
}