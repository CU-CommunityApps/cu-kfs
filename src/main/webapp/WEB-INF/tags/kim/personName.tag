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
    CU Customization: Reintroduced masking of names on the Person Document.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="personAttributes" value="${DataDictionary.Person.attributes}" />

<kul:subtab width="${tableWidth}" subTabTitle="Name" noShowHideButton="true">
    <table class="standard side-margins">
        <tr>
            <%-- CU Customization: Added potential masking of names. --%>
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.firstNameMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.middleNameMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.lastNameMaskedIfNecessary}" noColon="true" />
            <%-- End CU Customization --%>
        </tr>
        <tr>
            <%-- CU Customization: Added potential masking of names. --%>
            <c:choose>
                <c:when test="${readOnlyEntity}">
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.firstNameMaskedIfNecessary" attributeEntry="${personAttributes.firstNameMaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.middleNameMaskedIfNecessary" attributeEntry="${personAttributes.middleNameMaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.lastNameMaskedIfNecessary" attributeEntry="${personAttributes.lastNameMaskedIfNecessary}" readOnly="true" />
                </c:when>
                <c:otherwise>
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.firstName" attributeEntry="${personAttributes.firstName}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.middleName" attributeEntry="${personAttributes.middleName}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.lastName" attributeEntry="${personAttributes.lastName}" />
                </c:otherwise>
            </c:choose>
            <%-- End CU Customization --%>
        </tr>
    </table>
</kul:subtab>
