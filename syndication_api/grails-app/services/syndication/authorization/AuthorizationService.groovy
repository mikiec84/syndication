/*
Copyright (c) 2014-2016, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package syndication.authorization

import com.ctacorp.commons.api.key.utils.AuthorizationHeaderGenerator
import com.ctacorp.syndication.exception.UnauthorizedException
import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import grails.util.Holders

import javax.annotation.PostConstruct

class AuthorizationService {

    static transactional = false
    def config = Holders.config
    def grailsApplication

    private AuthorizationHeaderGenerator generator
    private AuthorizationHeaderGenerator.KeyAgreement keyAgreement
    private RestBuilder rest

    @PostConstruct
    void init() {
        String privateKey = config?.CMSMANAGER_PRIVATEKEY
        String publicKey = config?.CMSMANAGER_PUBLICKEY
        String secret = config?.CMSMANAGER_SECRET

        if (privateKey && publicKey && secret) {
            rest = new RestBuilder()
            rest.restTemplate.messageConverters.removeAll { it.class.name == 'org.springframework.http.converter.json.GsonHttpMessageConverter' }
            keyAgreement = new AuthorizationHeaderGenerator.KeyAgreement()

            keyAgreement.setPublicKey(publicKey)
            keyAgreement.setSecret(secret)

            generator = new AuthorizationHeaderGenerator(Holders.config.apiKey.keyName ?: "syndication_api_key", keyAgreement)
            generator.printToConsole = false
        } else{
            log.error("Keys were left undefined!!! Verify config files. public: ${publicKey} private: ${privateKey} secret: ${secret}")
        }
    }

    String hashBody(String body){
        generator.hashData(body)
    }

    boolean checkAuthorization(Map thirdPartyRequest) {
        def authorizationRequest = (thirdPartyRequest as JSON).toString()
        log.debug "API: checkAuthorization: Third Party Request: ${authorizationRequest}"

        String date = new Date().toString()
        String requestUrl = config?.CMSMANAGER_SERVER_URL + config?.CMSMANAGER_VERIFYAUTHPATH
        log.debug "API: VerifyAuthPath: ${requestUrl}"

        def requestHeaders = [                                                                               //headers
             date: date,
             "content-type": "application/json",
             "content-length": authorizationRequest.bytes.size() as String
        ]

        String apiKeyHeaderValue = generator.getApiKeyHeaderValue(
            requestHeaders,
            requestUrl,                                                                     //requestURL
            "POST",                                                                         //HTTP Method
            authorizationRequest                                                            //Third party request as json string
        )

        log.debug("API: Internal Auth header value: ${apiKeyHeaderValue}\nThe header values are:\n" +
                "${requestHeaders}\n" +
                "requestUrl: ${requestUrl}\n" +
                "authorizationRequest: ${authorizationRequest}")

        def resp = rest.post(requestUrl) {
            header 'Date', date
            header 'Authorization', apiKeyHeaderValue

            json thirdPartyRequest
        }
        log.debug("API: Authorization Response Status: ${resp.status}")

        if(resp.status != 204){
            try{
                log.error("API: The detailed response from CMS Manager: ${(resp.json as JSON).toString(true)}")
            } catch(ignored){}

            log.error("Non 204 response code for authorization check: ${resp.status}")
            return false
        }

        true
    }

    def post(String body, String url){
        def requestHeaders = [
            'Date': new Date().toString(),
            'Content-Type': "application/json",
            'Content-Length': body.bytes.size() as String,
            'Accept': 'application/json'
        ]

        def apiKeyHeaderValue = generator.getApiKeyHeaderValue(requestHeaders, url, 'POST', body)

        def resp

        try {
            resp = rest.post(url) {
                header 'Date', requestHeaders.Date
                header 'Authorization', apiKeyHeaderValue
                contentType "application/json"
                accept "application/json"

                json body
            }

        }catch(e){
            log.error("Couldn't post to server - maybe the server isn't running? ${e}")
//            StringWriter sw = new StringWriter()
//            PrintWriter pw = new PrintWriter(sw)
//            e.printStackTrace(pw)
//            log.error sw.toString()
            return null
        }

        switch(resp?.status){
            case 200: return resp.json
            case 204: return resp
            case 201: return [success:true] as JSON
            default:
                log.error "The response status from the authentication service was ${resp.status} - (It should be 200, or 204)\n--------------------------------------------------\nThe post that failed was to: ${url}\nand the body was: ${body}\n--------------------------------------------------"
                return [success:false] as JSON
        }
    }

    def sendAuthorizedRequest(url, body, method){
        def requestHeaders = [
                'Date': new Date().toString(),
                'Accept': 'application/json'
        ]
        if (body) {
            requestHeaders['Content-Type'] = "application/json"
            requestHeaders['Content-Length'] = body.bytes.size() as String
        } else {

            def headerContentLength = config?.hasProperty('CMSMANAGER_HEADERCONTENTLENGTH') ? config.CMSMANAGER_HEADERCONTENTLENGTH : true

            if(!headerContentLength) {
                requestHeaders['Content-Length'] = '0'
            }
        }

        def apiKeyHeaderValue = generator.getApiKeyHeaderValue(requestHeaders, url, method, body)
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
            throw new UnauthorizedException("Access Denied - Your authorization keys have been denied.")
        }

        resp.json
    }

    def getRest(String url){
        sendAuthorizedRequest(url, null, "GET")
    }

    boolean amIAuthorized() {
        String date = new Date().toString()
        String requestUrl = config?.CMSMANAGER_SERVER_URL + config?.CMSMANAGER_SELFAUTHPATH
        String apiKeyHeaderValue = generator.getApiKeyHeaderValue([date: date], requestUrl, "GET", null)
        def resp = rest.get(requestUrl) {
            header 'Date', date
            header 'Authorization', apiKeyHeaderValue
        }

        log.debug resp.json
        log.debug resp.status
        resp.json.isSecure as Boolean
    }
}
