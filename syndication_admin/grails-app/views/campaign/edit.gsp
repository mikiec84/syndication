%{--
Copyright (c) 2014, Health and Human Services - Web Communications (ASPA)
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--}%

<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.ctacorp.syndication.Campaign" %>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'campaign.label', default: 'Campaign')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
        <asset:javascript src="/tokenInput/jquery.tokeninput.js"/>
        <asset:stylesheet src="/tokenInput/token-input.css"/>
        <g:javascript>
        $(document).ready(function(){
            $("#mediaItems").tokenInput("${g.createLink(controller: 'mediaItem', action: 'tokenMediaSearch')}.json", {
                prePopulate:${featuredMediaForTokenInput.encodeAsRaw()}
            });
        });
        </g:javascript>
	</head>
	<body>
		<div id="edit-campaign" class="content scaffold-edit" role="main">
			<h1><g:message code="default.edit.label" args="[entityName]" /></h1>
			<synd:message/>
			<synd:errors/>
			<synd:error/>
			<div class="row">
				<div class="col-md-8">
					<g:form class="form-horizontal" url="[resource:campaignInstance, action:'update']" method="PUT" >
						<g:hiddenField name="version" value="${campaignInstance?.version}" />
						<fieldset class="form">
							<g:render template="form"/>
						</fieldset>
						<fieldset class="buttons">
							<div class="form-group">
								<label class="col-md-5 control-label" for="create"></label>
								<div class="col-md-7">
								<g:actionSubmit class="btn btn-success" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
								<g:link class="button" resource="${campaignInstance}" action="show">
									<button type="button" class="btn">Cancel</button>
								</g:link>
								</div>
							</div>
						</fieldset>
					</g:form>
				</div>
				<div class="col-md-4">
					<div class="panel panel-info">
						<div class="panel-heading">
							<h3 class="panel-title">MediaItem Selection</h3>
						</div>
						<div class="panel-body">
							If this Campaign belongs to a Subscriber then only MediaItems owned by that subscriber will be saved.
						</div>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
