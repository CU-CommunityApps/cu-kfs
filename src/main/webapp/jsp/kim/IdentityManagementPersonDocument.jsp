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
    CU Customization: Reinserted the Privacy Preferences tab from the 2023-01-25 financials version of the document.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="readOnly" scope="request" value="${!KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="readOnlyEntity" scope="request" value="${!KualiForm.canModifyPerson || readOnly}" />
<c:set var="formAction" value="identityManagementPersonDocument" />

<kul:documentPage
  showDocumentInfo="true"
  htmlFormAction="${formAction}"
    documentTypeName="PERS"
    renderMultipart="false"
    showTabButtons="true"
>
  <kul:hiddenDocumentFields />
  <kul:documentOverview editingMode="${KualiForm.editingMode}" />
  <kim:personOverview />
  <kim:personContact />
  <%-- CU Customization: Reinserted the Privacy Preferences tab. --%>
  <kim:personPrivacy />
  <%-- End CU Customization --%>
  <kim:personMembership />
  <kul:adHocRecipients />
  <kul:routeLog />
  <kul:superUserActions />
  <kul:documentControls transactionalDocument="false" />
</kul:documentPage>
