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
<%--
    CU Customization: Added the Notes/Attachments tab to the document.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="readOnly" scope="request" value="${!KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="readOnlyAssignees" scope="request" value="${!KualiForm.canAssignRole || readOnly}" />
<c:set var="canModifyAssignees" scope="request" value="${KualiForm.canModifyAssignees && !readOnlyAssignees}" />
<c:set var="editingDocument" scope="request" value="${KualiForm.document.editing}" />
<c:set var="memberSearchValue" scope="request" value="${KualiForm.memberSearchValue}" />
<c:set var="formAction" value="identityManagementRoleDocument" />

<kul:documentPage
    showDocumentInfo="true"
    htmlFormAction="${formAction}"
    documentTypeName="ROLE"
    renderMultipart="true"
    showTabButtons="true"
>
    <kul:hiddenDocumentFields />
    <kul:documentOverview editingMode="${KualiForm.editingMode}" />
    <kim:roleOverview />
    <kim:rolePermissions />
    <kim:roleResponsibilities />
    <kim:roleAssignees formAction="${formAction}"/>
    <kim:roleDelegations />
    <%-- CU Customization: Add the Notes/Attachments tab. --%>
    <c:set var="readOnly" scope="request" value="false" />
    <kul:notes/>
    <c:set var="readOnly" scope="request" value="${!KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}" />
    <%-- End CU Customization --%>
    <kul:adHocRecipients />
    <kul:routeLog />
    <kul:superUserActions />
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
</kul:documentPage>
