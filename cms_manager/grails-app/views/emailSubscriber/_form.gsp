<%--
Copyright (c) 2014, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--%>
<%@ page import="com.ctacorp.syndication.manager.cms.Subscriber" %>

<div class="form-group ${hasErrors(bean: instance, field: 'email', 'has-error')} ">
    <label class="control-label col-sm-3" for="email">
        <g:message code="subscriber.email.label" default="Email" />
        <i class="fa fa-asterisk"></i>
    </label>
    <div class="col-sm-6">
        <g:textField class="form-control" name="email" value="${instance?.email}"/>
    </div>
</div>

<div class="form-group ${hasErrors(bean: instance, field: 'subscriber', 'error')}">
    <label class="control-label col-sm-3" for="subscriber">
        <g:message code="emailSubscriber.subscriber.label" default="Subscriber" />
        <i class="fa fa-asterisk"></i>
    </label>
    <div class="controls col-sm-6">
        <g:select class="form-control" id="subscriber" name="subscriber.id" from="${Subscriber.list()}" optionKey="id" optionValue="name" />
    </div>
</div>
