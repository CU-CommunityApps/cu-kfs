<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2023 Kuali, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="userIdFieldName" required="true" description="The name of the property which will be populated with the principal name." %>
<%@ attribute name="universalIdFieldName" required="true" description="The name of the field which will be populated with the principal id." %>
<%@ attribute name="userNameFieldName" required="true" description="The field which will be populated with the principal name, for displaying." %>
<%@ attribute name="userId" required="true" description="The passed in principal name." %>
<%@ attribute name="universalId" required="true" description="The passed in principal id." %>
<%@ attribute name="userName" required="true" description="The passed in principal name, for display." %>

<%@ attribute name="label" required="false" description="The label to be shown on the linked lookup screen." %>
<%@ attribute name="fieldConversions" required="false" description="Pre-set values to populate within the lookup form." %>
<%@ attribute name="lookupParameters" required="false" description="On return from lookup, these parameters describe which attributes of the business object to populate in the lookup parent." %>
<%@ attribute name="referencesToRefresh" required="false" description="On return from the lookup, the references on the parent business object which will be refreshed." %>

<%@ attribute name="hasErrors" required="false" description="Whether an error icon should be displayed by the field or not." %>
<%@ attribute name="readOnly" required="false" description="Whether this field should be displayed as read only or not." %>
<%@ attribute name="onblur" required="false" description="Javascript code which will be executed with the input field's onblur event is triggered." %>
<%@ attribute name="onchange" required="false" description="Javascript code which will be executed with the input field's onchange event is triggered." %>

<%@ attribute name="helpLink" required="false" description="HTML - not just a URL - to be displayed next to the input field, presumably linking to a help resource." %>

<%@ attribute name="highlight" required="false"
              description="boolean indicating if this field is rendered as highlighted (to indicate old/new value change)" %>
<%@ attribute name="tabIndex" required="false" description="Tab index to use for next field" %>

<%@ attribute name="hideEmptyCell" required="false" description="whether to hide the empty cell before the user name field" %>

<%@ attribute name="newRow" required="false" description="whether to start a new table row for the user or not" %>

<%-- CU Customization: Convert Person name references to the potentially masked equivalents when possible. --%>

<c:set var="oldUserNameFieldName" value="${!empty userNameFieldName ? userNameFieldName : ''}"/>

<c:if test="${!empty userNameFieldName && !empty userIdFieldName && fn:endsWith(userNameFieldName, '.name') && fn:endsWith(userIdFieldName, '.principalName')}">
    <c:set var="userNameFieldName" value="${fn:replace(userNameFieldName, '.name', '.nameMaskedIfNecessary')}"/>
</c:if>

<c:if test="${!empty userName && !empty userNameFieldName && oldUserNameFieldName ne userNameFieldName}">
    <c:set var="userName" value="${kfsfunc:convertPersonNameForDisplayIfNecessary(userName, KualiForm, userNameFieldName)}"/>
</c:if>

<c:if test="${!empty fieldConversions}">
    <c:set var="fieldConversions" value="${kfsfunc:convertPersonFieldConversionsForMasking(fieldConversions, oldUserNameFieldName, userNameFieldName)}"/>
</c:if>

<c:if test="${!empty lookupParameters}">
    <c:set var="lookupParameters" value="${kfsfunc:convertPersonLookupParametersForMasking(lookupParameters, oldUserNameFieldName, userNameFieldName)}"/>
</c:if>

<%-- End CU Customization --%>

<%-- if the universal user ID field is a key field on this document, lock-down the user ID field --%>
<c:choose>
  <c:when test="${readOnly}">
    <input type="hidden" id='<c:out value="${userIdFieldName}"/>' name='<c:out value="${userIdFieldName}"/>' value='<c:out value="${userId}"/>' />
    <kul:inquiry boClassName="org.kuali.kfs.kim.impl.identity.Person" keyValues="principalId=${universalId}&principalName=${userId}" render="true"><c:out value="${userId}" /></kul:inquiry>&nbsp;
  </c:when>
  <c:otherwise>
    ${kfunc:registerEditableProperty(KualiForm, userIdFieldName)}
    <input type="text" id='<c:out value="${userIdFieldName}"/>' name='<c:out value="${userIdFieldName}"/>' value='<c:out value="${userId}"/>'
    title='${DataDictionary.Person.attributes.principalName.label}'
    size='${DataDictionary.Person.attributes.principalName.control.size}'
    maxlength='${DataDictionary.Person.attributes.principalName.maxLength}' style="${textStyle}"
    onBlur="loadUserInfo( '${userIdFieldName}', '${universalIdFieldName}', '${userNameFieldName}' );${onblur}" onchange="${onchange}" tabIndex="${tabIndex}"/>
    <c:if test="${hasErrors}">
     <kul:fieldShowErrorIcon />
    </c:if>
    <kul:lookup boClassName="org.kuali.kfs.kim.impl.identity.Person"
          fieldConversions="${fieldConversions}"
          lookupParameters="${lookupParameters}"
          fieldLabel="${label}"
          referencesToRefresh="${referencesToRefresh}"
          anchor="${currentTabIndex}" />
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${readOnly}">
    -
  </c:when>
  <c:otherwise>
    ${helpLink}
  </c:otherwise>
</c:choose>

<c:if test="${newRow}">
    </tr>
    <tr>

    <c:if test="${not hideEmptyCell}">
        <td width="50%">&nbsp;</td>
    </c:if>
    <td width="50%">
</c:if>
        <c:choose>
            <c:when test="${!empty userNameFieldName}">
                <span style="white-space:nowrap;" id="${userNameFieldName}.div">${userName}&nbsp;</span>
            </c:when>
            <c:otherwise><%-- guess at the name if the name field is not being rendered --%>
                <%-- CU Customization: Reference the potentially masked name field instead. --%>
                <span style="white-space:nowrap;" id='${fn:replace( userIdFieldName, ".principalName", ".nameMaskedIfNecessary" )}.div'>${userName}&nbsp;</span>
                <%-- When the user name field is not set, most likely, the name is not passed through
                     (It is also not available to be passed in, since only the Field objects are present
                     for use by rowDisplay.tag.  So, we fire off the needed JS to update the name. --%>
                <c:if test="${empty userName && !(empty userId)}">
                    <script type="text/javascript">loadUserInfo( "${userIdFieldName}", "", "" );</script>
                </c:if>
            </c:otherwise>
        </c:choose>
<c:if test="${newRow}">
    </td>
</c:if>

<c:if test="${!empty universalIdFieldName}">
  ${kfunc:registerEditableProperty(KualiForm, universalIdFieldName)}
  <input type="hidden" name="${universalIdFieldName}" id="${universalIdFieldName}" value="${universalId}" />
</c:if>
<c:if test="${!empty userNameFieldName}">
  ${kfunc:registerEditableProperty(KualiForm, userNameFieldName)}
  <input type="hidden" name="${userNameFieldName}" id="${userNameFieldName}" value="${userName}" />
</c:if>

<c:if test="${highlight}">
<kul:fieldShowChangedIcon/>
</c:if>
