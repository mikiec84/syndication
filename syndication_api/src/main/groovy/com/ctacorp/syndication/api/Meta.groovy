
/*
Copyright (c) 2014-2016, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package com.ctacorp.syndication.api

import com.ctacorp.grails.swagger.annotations.*

//@Model(id = "Meta", properties = [
//    @ModelProperty(propertyName = "status",         attributes = [@PropertyAttribute(type="integer", format="int64", required = true)]),
//    @ModelProperty(propertyName = "messages",       attributes = [@PropertyAttribute(type = "array", typeRef = "Message", required = true)]),
//    @ModelProperty(propertyName = "pagination",     attributes = [@PropertyAttribute(type = "Pagination", required = true)])
//])
@Definition
class Meta {
    @DefinitionProperty(name='status', type = DefinitionPropertyType.INTEGER)
    long status             = 200
    @DefinitionProperty(name='messages', type = DefinitionPropertyType.ARRAY, reference = 'Message')
    List<Message> messages  = []
    @DefinitionProperty(name='pagination', type=DefinitionPropertyType.OBJECT, reference = 'Pagination')
    Pagination pagination   = new Pagination()

    static Meta userMessage(String userMessage){
        Meta meta = new Meta()
        meta.messages << Message.userMessage(userMessage)
        this
    }

    def addUserMessage(String userMessage){
        messages << Message.userMessage(userMessage)
        this
    }

    def addMessage(Message message){
        this.messages << message
        this
    }

    def addMessages(List messages){
        messages.each{ message ->
            this.messages << message
        }
        this
    }

    def setMessage(Message message){
        this.messages = [message]
        this
    }

    def autoFill(params, int dataSize){
        status = params.int('status') ?: status
        if(status != 200){
            params.total = 0
        }
        pagination.autoFill(params, dataSize)
    }

    def generateMessagesBlock() {
        List<Message> messageList = []
        messages.each { message ->
            messageList << new Message([
                    errorMessage:message?.errorMessage,
                    errorDetail:message?.errorDetail,
                    errorCode:message?.errorCode,
                    userMessage:message?.userMessage
            ])
        }
        messageList
    }

    String toString(){
        String meta = "    - status: ${status}\n" +
                "    - messages:${messages}\n" +
                "    - pagination:\n${pagination}\n"
        meta
    }
}
