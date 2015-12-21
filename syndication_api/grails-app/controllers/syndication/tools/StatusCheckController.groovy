
/*
Copyright (c) 2014, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package syndication.tools

import grails.converters.JSON

class StatusCheckController {
    def tinyUrlService
    def tagsService

    def beforeInterceptor = {
        response.characterEncoding = 'UTF-8' //workaround for https://jira.grails.org/browse/GRAILS-11830
    }

    def index() {
        boolean allSystemsGo = true
        if(!tinyUrlService.status()){
            allSystemsGo = false
        }
        if(!tagsService.status()){
            allSystemsGo = false
        }

        response.contentType = "application/json"
        if(allSystemsGo){
            render "${params.callback}(${([running:"roger"] as JSON)});"
        } else{
            render "${params.callback}(${([running:"partial"] as JSON)});"
        }
    }

    def tinyUrlStatus(){
        if(tinyUrlService.status()){
            render "${params.callback}(${([running:"roger"] as JSON)});"
        } else{
            render "${params.callback}(${([running:"error"] as JSON)});"
        }
    }

    def tagCloudStatus(){
        if(tagsService.status()){
            render "${params.callback}(${([running:"roger"] as JSON)});"
        } else{
            render "${params.callback}(${([running:"error"] as JSON)});"
        }
    }
}