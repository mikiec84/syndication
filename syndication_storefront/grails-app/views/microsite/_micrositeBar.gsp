<%@ page import="grails.util.Holders" %>
<sec:ifLoggedIn>
    <g:if test="${com.ctacorp.syndication.commons.util.Util.isTrue(params.showAdminControls)}">
        <g:render template="/microsite/topNav"/>

        <div class="microsite-bar">
            <div class="container">
                <form class="inline-form">
                    <div class="form-group">
                        <label for="microsite-url">Nice site! Now copy and share it:</label>
                        <input type="text" id="microsite-url" readonly value="${grails.util.Holders.config.STOREFRONT_SERVER_URL}/${params.controller}/show/${params.id}">
                    </div><!--end form-group-->
                </form>
            </div>
        </div><!-- end container-fluid microsite-bar -->
        <br/>
    </g:if>
</sec:ifLoggedIn>