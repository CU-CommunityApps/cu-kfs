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
<c:set var="readOnlyAssignees" scope="request" value="${!KualiForm.canAssignRole || readOnly}" />
<c:set var="canModifyAssignees" scope="request" value="${KualiForm.canModifyAssignees && !readOnlyAssignees}" />
<c:set var="editingDocument" scope="request" value="${KualiForm.document.editing}" />
<c:set var="memberSearchValue" scope="request" value="${KualiForm.memberSearchValue}" />


<c:set var="formAction" value="identityManagementRoleDocument" />
<c:if test="${inquiry}">
	<c:set var="formAction" value="identityManagementRoleInquiry" />
</c:if>

<%-- CU Customization: Use a custom document page tag that supports modal inquiry display. --%>
<kul:documentPageWithModalInquiryMode
    modalInquiryMode="${inquiry && param.mode eq 'modal'}"
    showDocumentInfo="${!inquiry}"
    htmlFormAction="${formAction}"
    documentTypeName="ROLE"
    renderMultipart="${!inquiry}"
    showTabButtons="true">

    <c:if test="${!inquiry}">
	 	<kul:hiddenDocumentFields />
        <kul:documentOverview editingMode="${KualiForm.editingMode}" />
	</c:if>
    <c:if test="${inquiry}">
        <%-- CU Customization: Add an extra empty line. --%>
        <br/>
        <div id="workarea">
    </c:if>
    <kim:roleOverview />
	<kim:rolePermissions />
	<kim:roleResponsibilities />
	<kim:roleAssignees formAction="${formAction}"/>
	<kim:roleDelegations />

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
            <input type="hidden" name="roleId" value="${KualiForm.document.roleId}" />
            <script type="text/javascript">
			function changeDelegationMemberTypeCode( frm ) {
                postMethodToCall( frm, "changeDelegationMemberTypeCode" );
            }
            function changeMemberTypeCode( frm ) {
                postMethodToCall( frm, "changeMemberTypeCode" );
            }
            function namespaceChanged( frm ) {
                postMethodToCall( frm, "changeNamespace" );
            }
            function postMethodToCall( frm, methodToCall ) {
                var methodToCallElement=document.createElement("input");
                methodToCallElement.setAttribute("type","hidden");
                methodToCallElement.setAttribute("name","methodToCall");
                methodToCallElement.setAttribute("value", methodToCall );
                frm.appendChild(methodToCallElement);
                frm.submit();
            } 
            </script>
	    </c:when>
	    <c:otherwise>
            <%-- CU Customization: Hide inquiry controls if form is displayed in a modal. --%>
            <c:if test="${param.mode ne 'modal'}">
                <kul:inquiryControls />
            </c:if>
            <input type="hidden" name="roleId" value="${KualiForm.document.roleId}" />
	    </c:otherwise>
    </c:choose>
</kul:documentPageWithModalInquiryMode>