%{--
Copyright (c) 2014-2016, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--}%
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.ctacorp.syndication.ExtendedAttribute" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'extendedAttribute.label', default:'Extended Attribute')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<div id="edit-extendedAttribute" class="content scaffold-edit" role="main">
			<h1><g:message code="default.edit.label" args="[entityName]" /></h1>
			<synd:message/>
			<synd:errors/>
			<synd:error/>
			<g:form class="form-horizontal" url="[resource:extendedAttributeInstance, action:'update']" method="PUT" >
				<g:hiddenField name="version" value="${extendedAttributeInstance?.version}" />
				<fieldset class="form">
					<g:render template="form"/>
				</fieldset>
				<g:hiddenField name="mediaId" value="${params.mediaId}"/>
				<fieldset class="buttons">
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="edit"></label>
                        <div class="col-md-8">
					        <g:actionSubmit id="edit" class="btn btn-success" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                            <g:link class="button" params="[mediaId:params.mediaId]" id="${extendedAttributeInstance.id}" resource="${extendedAttributeInstance}" action="show">
                                <button type="button" class="btn">Cancel</button>
                            </g:link>
                        </div>
                    </div>
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
