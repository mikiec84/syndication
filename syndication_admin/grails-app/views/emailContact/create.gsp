%{--
Copyright (c) 2014-2016, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--}%

<%--
  Created by IntelliJ IDEA.
  User: sgates
  Date: 11/25/15
  Time: 12:33 PM
--%>

<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.ctacorp.syndication.contact.EmailContact" %>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'emailContact.label', default: 'Email Contact')}" />
    <title><g:message code="default.create.label" args="[entityName]" /></title>
</head>
<body>
<div id="create-emailContact" class="content scaffold-create" role="main">
    <h1><g:message code="default.create.label" args="[entityName]" /></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${emailContactInstance}">
        <ul class="errors" role="alert">
            <g:eachError bean="${emailContactInstance}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
            </g:eachError>
        </ul>
    </g:hasErrors>

    <synd:message/>
    <synd:errors/>
    <synd:error/>

    <div class="row">
        <div class="col-md-10">
            <g:form class="form-horizontal" url="[resource:emailContactInstance, action:'save']" >
                <fieldset class="form">
                    <g:render template="form" model="[emailContactInstance:emailContactInstance]"/>
                </fieldset>
                <fieldset class="buttons">
                    <g:submitButton name="create" class="btn btn-default btn-success" value="${message(code: 'default.button.create.label', default: 'Create')}" />
                    <g:link class="button" action="index">
                        <button type="button" class="btn">Cancel</button>
                    </g:link>
                </fieldset>
            </g:form>
        </div>
    </div>
</div>
</body>
</html>
