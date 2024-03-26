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
    CU Customization: Copied this file from the 2023-01-25 financials patch, and modified it to be compatible
                      with our custom preservation of the Privacy Preferences data (which had been removed
                      from base code in the 2023-01-29 financials patch).
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="personExtensionAttributes" value="${DataDictionary.PersonExtension.attributes}" />

<kul:tab tabTitle="Privacy Preferences" defaultOpen="false">
    <div class="tab-container" align="center">
        <table class="standard side-margins">
            <tr>
                <kim:cell cellWidth="30%" isLabel="true" textAlign="right" attributeEntry="${personExtensionAttributes.suppressName}" /> 
                <kim:cell cellWidth="20%" textAlign="center" property="document.extension.suppressName" attributeEntry="${personExtensionAttributes.suppressName}" readOnly="${readOnlyEntity}" />
                <kim:cell cellWidth="30%" isLabel="true" textAlign="right" attributeEntry="${personExtensionAttributes.suppressPhone}" />
                <kim:cell cellWidth="20%" textAlign="center" property="document.extension.suppressPhone" attributeEntry="${personExtensionAttributes.suppressPhone}" readOnly="${readOnlyEntity}" />
            </tr>
            <tr>
                <kim:cell cellWidth="30%" isLabel="true" textAlign="right" attributeEntry="${personExtensionAttributes.suppressPersonal}" />
                <kim:cell cellWidth="20%" textAlign="center" property="document.extension.suppressPersonal" attributeEntry="${personExtensionAttributes.suppressPersonal}" readOnly="${readOnlyEntity}" />
                <kim:cell cellWidth="30%" isLabel="true" textAlign="right" attributeEntry="${personExtensionAttributes.suppressEmail}" />
                <kim:cell cellWidth="20%" textAlign="center" property="document.extension.suppressEmail" attributeEntry="${personExtensionAttributes.suppressEmail}" readOnly="${readOnlyEntity}" />
            </tr>
        </table>        
    </div>
</kul:tab>
