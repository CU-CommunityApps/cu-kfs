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
                      Also backported the FINP-9357 and FINP-9391 changes into this file.
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
        </tr>
    </table>
</kul:subtab>

<kul:subtab width="${tableWidth}" subTabTitle="Alternate Address" noShowHideButton="true">
    <table class="standard side-margins">
        <tr>
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.altAddressTypeCode}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.altAddressLine1MaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.altAddressLine2MaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.altAddressLine3MaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.altAddressCityMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.altAddressStateProvinceCodeMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.altAddressPostalCodeMaskedIfNecessary}" noColon="true" />
            <kim:cell isLabel="true" textAlign="left" attributeEntry="${personAttributes.altAddressCountryCodeMaskedIfNecessary}" noColon="true" />
        </tr>
        <tr>
            <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressTypeCode" attributeEntry="${personAttributes.altAddressTypeCode}" readOnly="${readOnlyEntity}" />
            <c:choose>
                <c:when test="${readOnlyEntity}">
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressLine1MaskedIfNecessary" attributeEntry="${personAttributes.altAddressLine1MaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressLine2MaskedIfNecessary" attributeEntry="${personAttributes.altAddressLine2MaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressLine3MaskedIfNecessary" attributeEntry="${personAttributes.altAddressLine3MaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressCityMaskedIfNecessary" attributeEntry="${personAttributes.altAddressCityMaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressStateProvinceCodeMaskedIfNecessary" attributeEntry="${personAttributes.altAddressStateProvinceCodeMaskedIfNecessary}" styleClass="fixed-size-200-select" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressPostalCodeMaskedIfNecessary" attributeEntry="${personAttributes.altAddressPostalCodeMaskedIfNecessary}" readOnly="true" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressCountryCodeMaskedIfNecessary" attributeEntry="${personAttributes.altAddressCountryCodeMaskedIfNecessary}" styleClass="fixed-size-200-select" readOnly="true" />
                </c:when>
                <c:otherwise>
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressLine1" attributeEntry="${personAttributes.altAddressLine1}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressLine2" attributeEntry="${personAttributes.altAddressLine2}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressLine3" attributeEntry="${personAttributes.altAddressLine3}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressCity" attributeEntry="${personAttributes.altAddressCity}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressStateProvinceCode" attributeEntry="${personAttributes.altAddressStateProvinceCode}" styleClass="fixed-size-200-select" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressPostalCode" attributeEntry="${personAttributes.altAddressPostalCode}" />
                    <kim:cell valign="middle" cellClass="infoline" textAlign="left" property="document.altAddressCountryCode" attributeEntry="${personAttributes.altAddressCountryCode}" styleClass="fixed-size-200-select" />
                </c:otherwise>
            </c:choose>
        </tr>
    </table>
</kul:subtab>
