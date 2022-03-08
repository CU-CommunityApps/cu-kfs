<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2021 Kuali, Inc.

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
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="inquiry" scope="request" value="${KualiForm.inquiry}" />
<c:set var="readOnly" scope="request" value="${!KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] || inquiry}" />
<c:set var="readOnlyEntity" scope="request" value="${!KualiForm.canModifyEntity || readOnly}" />

<c:set var="formAction" value="identityManagementPersonDocument" />
<c:if test="${inquiry}">
    <c:set var="formAction" value="identityManagementPersonInquiry" />
</c:if>

<%-- CU Customization: Use a custom document page tag that supports modal inquiry display. --%>
<kul:documentPageWithModalInquiryMode
    modalInquiryMode="${inquiry && param.mode eq 'modal'}"
    showDocumentInfo="${!inquiry}"
    htmlFormAction="${formAction}"
    documentTypeName="PERS"
    renderMultipart="${inquiry}"
    showTabButtons="true"
    auditCount="0">

    <c:if test="${!inquiry}">
 	    <kul:hiddenDocumentFields />
	    <kul:documentOverview editingMode="${KualiForm.editingMode}" />
	</c:if>
    <c:if test="${inquiry}">
        <%-- CU Customization: Add an extra empty line. --%>
        <br/>
        <div id="workarea">
    </c:if>
	<kim:personOverview />
	<kim:personContact />
	<kim:personPrivacy />
	<kim:personMembership />

    <c:if test="${!inquiry}">    		
		<kul:adHocRecipients />
		<kul:routeLog />
	</c:if>
    <%-- CU Customization: Do not display the superuser actions when in inquiry mode. --%>
    <c:if test="${!inquiry}">
        <kul:superUserActions />
    </c:if>
    <c:if test="${inquiry}">
        </div>
    </c:if>
    <c:choose>
        <c:when test="${!inquiry}">
            <kul:documentControls transactionalDocument="false" />
        </c:when>
        <c:otherwise>
            <%-- CU Customization: Hide inquiry controls if form is displayed in a modal. --%>
            <c:if test="${param.mode ne 'modal'}">
                <kul:inquiryControls />
            </c:if>
            <input type="hidden" name="principalId" value="${KualiForm.document.principalId}" />
        </c:otherwise>
    </c:choose>

</kul:documentPageWithModalInquiryMode>
