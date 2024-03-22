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

<c:set var="personAttributes" value="${DataDictionary.IdentityManagementPersonDocument.attributes}" />

<%-- CU Customization: Added and modified entries in the "tabErrorKey" property. --%>
<kul:tab tabTitle="Overview" defaultOpen="true" transparentBackground="${!KualiForm.hasWorkflowDocument}"
         tabErrorKey="document.pr*,document.univ*,document.active,document.affiliation*,document.campus*,newAffiliation.*,document.extension.affiliations*">
    <c:set var="principalNameNote" value="" />
    <c:if test="${not readOnlyEntity}">
      <c:set var="principalNameNote" value="<br/><label class='fineprint'>(${personAttributes.principalName.label} must be lower case)</label>"/>
    </c:if>
    <div class="tab-container" align="center">
        <table class="standard side-margins">
            <tr>
                <kim:cell isLabel="true" textAlign="right" attributeEntry="${personAttributes.entityId}" />
                <kim:cell property="document.entityId" attributeEntry="${personAttributes.entityId}" readOnly="true" />
                <kim:cell isLabel="true" textAlign="right" attributeEntry="${personAttributes.principalId}" />
                <kim:cell property="document.principalId" attributeEntry="${personAttributes.principalId}" readOnly="true" />
            </tr>
            <tr>
                <kim:cell isLabel="true" textAlign="right" attributeEntry="${personAttributes.principalName}" />
                <kim:cell property="document.principalName" attributeEntry="${personAttributes.principalName}" readOnly="${readOnlyEntity}"
                          postText="${principalNameNote}" />
                <kim:cell isLabel="true" textAlign="right" attributeEntry="${personAttributes.active}" />
                <kim:cell property="document.active" attributeEntry="${personAttributes.active}" readOnly="${readOnlyEntity}" />
            </tr>
        </table>
        <kim:personAffln />
        <kim:personEmpInfo/>
    </div>
</kul:tab>
