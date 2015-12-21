/*
Copyright (c) 2014, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package syndication.mq

import com.ctacorp.syndication.GenericQueueService
import com.ctacorp.syndication.commons.mq.Message
import com.ctacorp.syndication.jobs.DelayedNotificationJob

import javax.annotation.PostConstruct

class QueueService implements GenericQueueService{
    static transactional = false
    def grailsApplication
    def guavaCacheService
    def rabbitMessagePublisher
    String updateExchangeName

    @PostConstruct
    def init(){
        updateExchangeName = grailsApplication.config.syndication.mq.updateExchangeName
    }

    @Override
    void sendMessage(Message msg) {
        def message = msg.toJsonString()

        log.info("Sending message: ${message}")

        try {
            rabbitMessagePublisher.send {
                exchange = updateExchangeName
                body = message
            }
        } catch(e){
            log.error "A rabbitMQ error occurred on message ${msg.toJsonString()}"
            e.printStackTrace()
        }
    }

    @Override
    void sendDelayedMessage(Message msg) {
        DelayedNotificationJob.schedule(new Date(System.currentTimeMillis() + 10000), [msg: msg])
    }

    void flushCache(){
        guavaCacheService.flushAllCaches()
    }
}