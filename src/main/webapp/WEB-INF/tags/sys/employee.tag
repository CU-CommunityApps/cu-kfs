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

<%@ attribute name="userIdFieldName" required="true" %>
<%@ attribute name="userNameFieldName" required="true" %>

<%@ attribute name="label" required="false" %>
<%@ attribute name="fieldConversions" required="false" %>
<%@ attribute name="lookupParameters" required="false" %>
<%@ attribute name="referencesToRefresh" required="false" %>

<%@ attribute name="hasErrors" required="false" %>
<%@ attribute name="readOnly" required="false" %>
<%@ attribute name="onblur" required="false" %>

<%@ attribute name="highlight" required="false"
              description="boolean indicating if this field is rendered as highlighted (to indicate old/new value change)" %>
               <%@ attribute name="forceRequired" required="false" %>

<script language="JavaScript" type="text/javascript" src="dwr/interface/PersonService.js"></script>
<script language="JavaScript" type="text/javascript" src="scripts/sys/objectInfo.js"></script>

<%-- CU Customization: Convert Person name references to the potentially masked equivalents when possible. --%>

<c:set var="oldUserNameFieldName" value="${!empty userNameFieldName ? userNameFieldName : ''}"/>

<c:if test="${!empty userNameFieldName && fn:endsWith(userNameFieldName, '.name') && !fn:contains(userNameFieldName, 'HocRoutePerson')}">
    <c:set var="userNameFieldName" value="${fn:replace(userNameFieldName, '.name', '.nameMaskedIfNecessary')}"/>
</c:if>

<c:if test="${!empty userName && !empty userNameFieldName && oldUserNameFieldName ne userNameFieldName}">
    <c:set var="userName" value="${kfsfunc:convertPersonNameForDisplayIfNecessary(userName, KualiForm, userNameFieldName)}"/>
</c:if>

<c:if test="${!empty fieldConversions}">
    <c:set var="fieldConversions" value="${kfsfunc:convertPersonFieldConversionsForMasking(fieldConversions)}"/>
</c:if>

<c:if test="${!empty lookupParameters}">
    <c:set var="lookupParameters" value="${kfsfunc:convertPersonLookupParametersForMasking(lookupParameters)}"/>
</c:if>

<%-- End CU Customization --%>

<kul:htmlControlAttribute property="${userIdFieldName}"
                    attributeEntry="${DataDictionary['Person'].attributes.employeeId}" forceRequired="${forceRequired}"
                    onblur="loadEmplInfo( '${userIdFieldName}', '${userNameFieldName}' );${onblur}" readOnly="${readOnly}"/>
<c:if test="${!readOnly}">
    <%-- CU Customization: Set property to indicate that the Person field references were already adjusted. --%>
    <kul:lookup boClassName="org.kuali.kfs.kim.impl.identity.Person"
                fieldConversions="${fieldConversions}"
                lookupParameters="${lookupParameters}"
                fieldLabel="${label}"
                referencesToRefresh="${referencesToRefresh}"
                anchor="${currentTabIndex}"
                alreadyAdjustedForPerson="${true}"/>
</c:if>

<c:if test="${readOnly}">
  <div>${userName}</div>
</c:if>

<div id="${userNameFieldName}.div">
    <html:hidden write="true" property="${userNameFieldName}"/>
</div>

<c:if test="${!empty universalIdFieldName}">
    <input type="hidden" name="${universalIdFieldName}" value="${universalId}" />
</c:if>
<c:if test="${!empty userNameFieldName}">
    <input type="hidden" name="${userNameFieldName}" value="${userName}" />
</c:if>

<c:if test="${highlight}">
<kul:fieldShowChangedIcon/>
</c:if>
