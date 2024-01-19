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
    CU Customization: Added CU-specific affiliation fields.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="personAttributes" value="${DataDictionary.Person.attributes}"/>

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

<kul:subtab width="${tableWidth}" subTabTitle="All Affiliations" noShowHideButton="true">
    <table class="standard side-margins">
        <tr>
            <kim:cell isLabel="true" textAlign="left"
                      attributeEntry="${personAttributes.academicAffiliation}" noColon="true"/>
            <kim:cell isLabel="true" textAlign="left"
                      attributeEntry="${personAttributes.affiliateAffiliation}" noColon="true"/>
            <kim:cell isLabel="true" textAlign="left"
                      attributeEntry="${personAttributes.alumniAffiliation}" noColon="true"/>
            <kim:cell isLabel="true" textAlign="left"
                      attributeEntry="${personAttributes.exceptionAffiliation}" noColon="true"/>
            <kim:cell isLabel="true" textAlign="left"
                      attributeEntry="${personAttributes.facultyAffiliation}" noColon="true"/>
            <kim:cell isLabel="true" textAlign="left"
                      attributeEntry="${personAttributes.staffAffiliation}" noColon="true"/>
            <kim:cell isLabel="true" textAlign="left"
                      attributeEntry="${personAttributes.studentAffiliation}" noColon="true"/>
        </tr>
        <tr>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left"
                      property="document.academicAffiliation"
                      attributeEntry="${personAttributes.academicAffiliation}" readOnly="${readOnlyEntity}"/>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left"
                      property="document.affiliateAffiliation"
                      attributeEntry="${personAttributes.affiliateAffiliation}" readOnly="${readOnlyEntity}"/>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left"
                      property="document.alumniAffiliation"
                      attributeEntry="${personAttributes.alumniAffiliation}" readOnly="${readOnlyEntity}"/>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left"
                      property="document.exceptionAffiliation"
                      attributeEntry="${personAttributes.exceptionAffiliation}" readOnly="${readOnlyEntity}"/>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left"
                      property="document.facultyAffiliation"
                      attributeEntry="${personAttributes.facultyAffiliation}" readOnly="${readOnlyEntity}"/>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left"
                      property="document.staffAffiliation"
                      attributeEntry="${personAttributes.staffAffiliation}" readOnly="${readOnlyEntity}"/>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left"
                      property="document.studentAffiliation"
                      attributeEntry="${personAttributes.studentAffiliation}" readOnly="${readOnlyEntity}"/>
        </tr>
    </table>
</kul:subtab>