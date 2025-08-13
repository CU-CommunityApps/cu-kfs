<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2024 Kuali, Inc.

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
    CU Customization: Reintroduced masking of phone numbers on the Person Document.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="personAttributes" value="${DataDictionary.Person.attributes}" />
<%-- CU Customization: Added masking-related helper variable. --%>
<c:set var="maskReadOnlyDataIfNecessary" value="${!KualiForm.canOverridePrivacyPreferences}"/>

<kul:subtab width="${tableWidth}" subTabTitle="Phone Number" noShowHideButton="true">
    <table class="standard side-margins">
        <tr>
            <%-- CU Customization: Add potential masking of phone numbers. --%>
            <th><div align="left"><kul:htmlAttributeLabel attributeEntry="${personAttributes.phoneNumberMaskedIfNecessary}" noColon="true" /></div></th>
            <%-- End CU Customization --%>
        </tr>
        <tr>
            <td>
                <div align="left">
                    <%-- CU Customization: Add potential masking of phone numbers. --%>
                    <c:choose>
                        <c:when test="${readOnlyEntity && maskReadOnlyDataIfNecessary}">
                            <kul:htmlControlAttribute property="document.phoneNumberMaskedIfNecessary" attributeEntry="${personAttributes.phoneNumberMaskedIfNecessary}" readOnly="true" />
                        </c:when>
                        <c:otherwise>
                            <kul:htmlControlAttribute property="document.phoneNumber" attributeEntry="${personAttributes.phoneNumber}" />
                        </c:otherwise>
                    </c:choose>
                    <%-- End CU Customization --%>
                </div>
            </td>
        </tr>
    </table>
</kul:subtab>
