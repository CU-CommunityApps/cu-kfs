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
    CU Customization: Added an extra sub-tab for including our custom Alternate Address fields.
                      Also backported the FINP-9391 changes into this file.
--%>
<style type="text/css">
  select.fixed-size-200-select {
    width:200px;
   }
</style>

<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<c:set var="personAttributes" value="${DataDictionary.Person.attributes}" />

<kul:subtab width="${tableWidth}" subTabTitle="Address" noShowHideButton="true">
    <table class="standard side-margins">
        <tr>
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.addressTypeCode}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.addressLine1MaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.addressLine2MaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.addressLine3MaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.addressCityMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.addressStateProvinceCodeMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.addressPostalCodeMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.addressCountryCodeMaskedIfNecessary}" noColon="true" />
        </tr>
        <tr>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressTypeCode" attributeEntry="${personAttributes.addressTypeCode}" readOnly="${readOnlyEntity}" />
            <%-- Start of FINP-9391 changes. --%>
            <c:choose>
                <c:when test="${readOnlyEntity}">
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressLine1MaskedIfNecessary" attributeEntry="${personAttributes.addressLine1MaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressLine2MaskedIfNecessary" attributeEntry="${personAttributes.addressLine2MaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressLine3MaskedIfNecessary" attributeEntry="${personAttributes.addressLine3MaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressCityMaskedIfNecessary" attributeEntry="${personAttributes.addressCityMaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressStateProvinceCodeMaskedIfNecessary" attributeEntry="${personAttributes.addressStateProvinceCodeMaskedIfNecessary}" styleClass="fixed-size-200-select" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressPostalCodeMaskedIfNecessary" attributeEntry="${personAttributes.addressPostalCodeMaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressCountryCodeMaskedIfNecessary" attributeEntry="${personAttributes.addressCountryCodeMaskedIfNecessary}" styleClass="fixed-size-200-select" readOnly="true" />
                </c:when>
                <c:otherwise>
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressLine1" attributeEntry="${personAttributes.addressLine1}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressLine2" attributeEntry="${personAttributes.addressLine2}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressLine3" attributeEntry="${personAttributes.addressLine3}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressCity" attributeEntry="${personAttributes.addressCity}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressStateProvinceCode" attributeEntry="${personAttributes.addressStateProvinceCode}" styleClass="fixed-size-200-select" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressPostalCode" attributeEntry="${personAttributes.addressPostalCode}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.addressCountryCode" attributeEntry="${personAttributes.addressCountryCode}" styleClass="fixed-size-200-select" />
                </c:otherwise>
            </c:choose>
            <%-- End of FINP-9391 changes. --%>
        </tr>
    </table>
</kul:subtab>

<%-- CU Customization: Add new sub-tab for Alternate (Campus) Addresses. --%>
<c:set var="personExtensionAttributes" value="${DataDictionary.PersonExtension.attributes}" />

<kul:subtab width="${tableWidth}" subTabTitle="Alternate Address" noShowHideButton="true">
    <table class="standard side-margins">
        <tr>
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personExtensionAttributes.altAddressTypeCode}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personExtensionAttributes.altAddressLine1MaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personExtensionAttributes.altAddressLine2MaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personExtensionAttributes.altAddressLine3MaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personExtensionAttributes.altAddressCityMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personExtensionAttributes.altAddressStateProvinceCodeMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personExtensionAttributes.altAddressPostalCodeMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personExtensionAttributes.altAddressCountryCodeMaskedIfNecessary}" noColon="true" />
        </tr>
        <tr>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressTypeCode" attributeEntry="${personExtensionAttributes.altAddressTypeCode}" readOnly="${readOnlyEntity}" />
            <c:choose>
                <c:when test="${readOnlyEntity}">
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressLine1MaskedIfNecessary" attributeEntry="${personExtensionAttributes.altAddressLine1MaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressLine2MaskedIfNecessary" attributeEntry="${personExtensionAttributes.altAddressLine2MaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressLine3MaskedIfNecessary" attributeEntry="${personExtensionAttributes.altAddressLine3MaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressCityMaskedIfNecessary" attributeEntry="${personExtensionAttributes.altAddressCityMaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressStateProvinceCodeMaskedIfNecessary" attributeEntry="${personExtensionAttributes.altAddressStateProvinceCodeMaskedIfNecessary}" styleClass="fixed-size-200-select" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressPostalCodeMaskedIfNecessary" attributeEntry="${personExtensionAttributes.altAddressPostalCodeMaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressCountryCodeMaskedIfNecessary" attributeEntry="${personExtensionAttributes.altAddressCountryCodeMaskedIfNecessary}" styleClass="fixed-size-200-select" readOnly="true" />
                </c:when>
                <c:otherwise>
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressLine1" attributeEntry="${personExtensionAttributes.altAddressLine1}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressLine2" attributeEntry="${personExtensionAttributes.altAddressLine2}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressLine3" attributeEntry="${personExtensionAttributes.altAddressLine3}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressCity" attributeEntry="${personExtensionAttributes.altAddressCity}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressStateProvinceCode" attributeEntry="${personExtensionAttributes.altAddressStateProvinceCode}" styleClass="fixed-size-200-select" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressPostalCode" attributeEntry="${personExtensionAttributes.altAddressPostalCode}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.extension.altAddressCountryCode" attributeEntry="${personExtensionAttributes.altAddressCountryCode}" styleClass="fixed-size-200-select" />
                </c:otherwise>
            </c:choose>
        </tr>
    </table>
</kul:subtab>
<%-- End CU Customization --%>