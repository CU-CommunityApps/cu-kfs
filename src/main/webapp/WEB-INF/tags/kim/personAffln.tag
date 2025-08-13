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
    CU Customization: Added CU-specific affiliation fields and sections.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="personAttributes" value="${DataDictionary.Person.attributes}"/>
<%-- CU Customization: Add reference to Person Affiliation attributes. --%>
<c:set var="docAffilAttributes" value="${DataDictionary.PersonDocumentAffiliation.attributes}"/>

<%-- CU Customization: Changed sub-tab title to "Primary Affiliation" instead. --%>
<kul:subtab width="${tableWidth}" subTabTitle="Primary Affiliation" noShowHideButton="true">
    <table class="standard side-margins">
        <tr>
            <th>
                <div align="left">&nbsp;</div>
            </th>
            <kim:cell isLabel="true" textAlign="center"
                      attributeEntry="${personAttributes.affiliationTypeCode}" noColon="true"/>
            <kim:cell isLabel="true" textAlign="center"
                      attributeEntry="${personAttributes.campusCode}" noColon="true"/>
        </tr>
        <tr>
            <th rowspan="1" class="infoline"/>
            <kim:cell valign="middle" cellClass="infoline" textAlign="center"
                      property="document.affiliationTypeCode"
                      attributeEntry="${personAttributes.affiliationTypeCode}"
                      readOnly="${readOnlyEntity}"/>
            <kim:cell valign="middle" cellClass="infoline" textAlign="center"
                      property="document.campusCode"
                      attributeEntry="${personAttributes.campusCode}" readOnly="${readOnlyEntity}"/>
        </tr>
    </table>
</kul:subtab>

<%--
    CU Customization: Added a sub-tab for handling Person Affiliations list.
    Has a structure similar to that of "personGroup.tag" in base code.
--%>
<kul:subtab width="${tableWidth}" subTabTitle="All Affiliations" noShowHideButton="true">
    <table class="standard side-margins">
        <tr>
            <th><div align="left">&nbsp;</div></th>
            <kim:cell isLabel="true" textAlign="center"
                      attributeEntry="${docAffilAttributes.affiliationTypeCode}" noColon="true"/>
            <kim:cell isLabel="true" textAlign="center"
                      attributeEntry="${docAffilAttributes.affiliationStatus}" noColon="true"/>
            <kim:cell isLabel="true" textAlign="center"
                      attributeEntry="${docAffilAttributes.primary}" noColon="true"/>
            <c:if test="${not readOnlyEntity}">
                <kul:htmlAttributeHeaderCell literalLabel="Actions" scope="col"/>
            </c:if>
        </tr>
        <c:if test="${not readOnlyEntity}">
            <tr>
                <th class="infoline">
                    <c:out value="Add:"/>
                </th>
                <kim:cell valign="middle" cellClass="infoline" textAlign="center"
                      property="newAffiliation.affiliationTypeCode"
                      attributeEntry="${docAffilAttributes.affiliationTypeCode}"
                      readOnly="${readOnlyEntity}"/>
                <kim:cell valign="middle" cellClass="infoline" textAlign="center"
                      property="newAffiliation.affiliationStatus"
                      attributeEntry="${docAffilAttributes.affiliationStatus}"
                      readOnly="${readOnlyEntity}"/>
                <kim:cell valign="middle" cellClass="infoline" textAlign="center"
                      property="newAffiliation.primary"
                      attributeEntry="${docAffilAttributes.primary}"
                      readOnly="${readOnlyEntity}"/>
                <td align="left" valign="middle" class="infoline">
                    <div align="center">
                        <html:submit property="methodToCall.addAffiliation.anchor${tabKey}"
                              value="Add" styleClass="btn btn-green"/>
                    </div>
                </td>
            </tr>
        </c:if>
        <c:forEach var="affiliation" items="${KualiForm.document.extension.affiliations}" varStatus="status">
            <tr>
                <th class="infoline">
                    <c:out value="${status.index + 1}"/>
                </th>
                <kim:cell valign="middle" cellClass="infoline" textAlign="center"
                      property="document.extension.affiliations[${status.index}].affiliationTypeCode"
                      attributeEntry="${docAffilAttributes.affiliationTypeCode}"
                      readOnly="true"/>
                <kim:cell valign="middle" cellClass="infoline" textAlign="center"
                      property="document.extension.affiliations[${status.index}].affiliationStatus"
                      attributeEntry="${docAffilAttributes.affiliationStatus}"
                      readOnly="${readOnlyEntity}"/>
                <kim:cell valign="middle" cellClass="infoline" textAlign="center"
                      property="document.extension.affiliations[${status.index}].primary"
                      attributeEntry="${docAffilAttributes.primary}"
                      readOnly="${readOnlyEntity}"/>
                <c:if test="${not readOnlyEntity}">
                    <td align="left" valign="middle" class="infoline">
                        <div align="center">&nbsp;</div>
                    </td>
                </c:if>
            </tr>
        </c:forEach>
    </table>
</kul:subtab>
<%-- End CU Customization --%>
