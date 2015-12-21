<%@ page import="com.ctacorp.syndication.commons.util.Util" %>
<div style="width: 100%; overflow: hidden;">
    <div style="width: 400px; float: left;">
        <span>Copy and paste this snippet into your existing web-page, and when the page is viewed by a user, the syndicated content will inject itself automatically!</span>
        <p>
            <g:radioGroup name="jsOrIframe"
                          id="jsOrIframe"
                          labels="['Javascript', 'Iframe (no javascript)']"
                          values="['javascript', 'iframe']"
                          value="javascript">
                ${it.radio} ${it.label}
            </g:radioGroup>
        </p>
        <div style="width: 100%; overflow: hidden;">
            <div style="width: 200px; float: left;">
                <g:checkBox name="includeJquery"    class="extractionCheckbox" checked="true"  id="includeJquery"/><label>Include JQuery</label><br/>
                <g:checkBox name="stripClasses"     class="extractionCheckbox" checked="true" id="stripClasses"/><label>Strip Classes</label><br/>
                <g:checkBox name="stripStyles"      class="extractionCheckbox" checked="true" id="stripStyles"/><label>Strip Styles</label><br/>
                <label for="width">Width</label>
                <g:textField name="width" type="number" style="width: 50px" value="775" disabled="disabled" id="iframeWidth" class="iframe_size_control"/>
                <br>
                <label for="height">Height</label>
                <g:textField name="height" type="number" style="width: 50px" value="650" disabled="disabled" id="iframeHeight" class="iframe_size_control"/>
            </div>
            <div style="margin-left: 120px;">
                <g:checkBox name="stripIds"         class="extractionCheckbox" checked="true" id="stripIds"/><label>Strip Ids</label><br/>
                <g:checkBox name="stripScripts"     class="extractionCheckbox" checked="false" id="stripScripts"/><label>Strip Scripts</label><br/>
                <g:checkBox name="stripImages"      class="extractionCheckbox" checked="false" id="stripImages"/><label>Strip Images</label><br/>
                <g:checkBox name="stripBreaks"      class="extractionCheckbox" checked="false" id="stripBreaks"/><label>Strip Breaks</label><br/>
            </div>
        </div>
    </div>
    <div style="margin-left: 420px;">
        <g:textArea name="embedCode" cols="80" rows="10" id="snippetCode"/>
    </div>
</div>
<g:if test="${(mediaItemInstance && mediaItemInstance instanceof com.ctacorp.syndication.media.Collection) || (userMediaListInstance) || (renderTagList) || (renderSourceList)}">
    <div style="margin-top: 25px;">
        <h3 style="margin-bottom: 10px;">Collection Display Options</h3>
        <p>
            <g:radioGroup name="displayMethod"
                          id="displayMethod"
                          labels="['Feed', 'List', 'Media Viewer']"
                          values="['feed', 'list', 'mv']"
                          value="feed">
                ${it.radio} ${it.label}
            </g:radioGroup>
        </p>
    </div>
</g:if>