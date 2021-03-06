<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="storefront"/>

        <title>Create List</title>
        <asset:stylesheet src="tokenInput.css"/>
        <asset:javascript src="/tokenInput/jquery.tokeninput.js"/>
        <g:javascript>
            $(document).ready(function () {
                $("#mediaItemIds").tokenInput("${g.createLink(controller: 'userMediaList', action: 'mediaSearch')}", {
                    prePopulate:${raw(mediaItemList ?: "[]")},
                    preventDuplicates:true
                });
            });
        </g:javascript>
	</head>
	<body>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${userMediaListInstance}">
        <div class="errors">
            <g:renderErrors bean="${userMediaListInstance}" as="list" />
        </div>
    </g:hasErrors>
        <div id="home">
            <div id="home_left">
                <h2><g:link class="list" action="index">View Lists</g:link></h2>
                <div id="contentListBox">
                        <div class="purplebox">
                            <img class="spinner" id="spinner" src="${assetPath(src: 'ajax-loader.gif')}" alt="spinner"/>
                            <h3>Create List</h3>

                            <div id="create-userMediaList" class="content scaffold-create" role="main">

                                <g:form url="[resource:userMediaListInstance, action:'save']" >
                                        <g:render template="form"/>
                                        <g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" />
                                </g:form>
                            </div>
                        </div>
                </div>
            </div>
            <g:render template="/storefront/rightBoxes" model="[featuredMedia: featuredMedia, API_SERVER_URL : API_SERVER_URL]"/>
        </div>
	</body>
</html>
