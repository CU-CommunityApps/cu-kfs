<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2022 Kuali, Inc.

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
<%@ attribute name="camsLocationAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="ctr" required="true" description="item count"%>
<%@ attribute name="ctr2" required="true" description="item count"%>
<%@ attribute name="camsAssetLocationProperty" required="true" description="String that represents the prefix of the property name to store into the document on the form."%>
<%@ attribute name="availability" required="true" description="Determines if this is a capture once tag or for each"%>
<%@ attribute name="poItemInactive" required="false" description="True if the PO item this is a part of is inactive"%>
<!--  KFSPTS-1792 : allow FO to edit REQ capital asset tab add 'enableCa' in purCams.tag-->

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])}" />

<c:if test="${empty availability}">
    <c:set var="availability" value="${PurapConstants.CapitalAssetAvailability.EACH}"/>
</c:if>

<c:set var="offCampus" value="false" />
<logic:equal name="KualiForm" property="${camsAssetLocationProperty}.offCampusIndicator" value="Yes">
    <c:set var="offCampus" value="true" />
</logic:equal>

<c:set var="readOnlyBuildingCode" value="true" />
<c:if test="${KualiForm.document.documentHeader.workflowDocument.initiated || KualiForm.document.documentHeader.workflowDocument.saved}">
    <c:set var="readOnlyBuildingCode" value="false" />
</c:if>

<c:set var="deleteLocationUrl" value="methodToCall.deleteCapitalAssetLocationByItem.(((${ctr}))).((#${ctr2}#))" />
<c:set var="refreshAssetLocationBuildingUrl" value="methodToCall.useOffCampusAssetLocationBuildingByItem.(((${ctr}))).((#${ctr2}#))" />
<c:if test="${PurapConstants.CapitalAssetAvailability.ONCE eq availability}">
    <c:set var="deleteLocationUrl" value="methodToCall.deleteCapitalAssetLocationByDocument.(((${ctr}))).((#${ctr2}#))" />
    <c:set var="refreshAssetLocationBuildingUrl" value="methodToCall.useOffCampusAssetLocationBuildingByDocument.(((${ctr}))).((#${ctr2}#))" />
</c:if>
<c:set var="tabindexOverrideBase" value="60" />

<table class="standard">
    <tr>
        <td colspan="4" class="subhead">
            <c:choose>
                <c:when test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive and !(ctr2 eq 'new')}">
                    <div class="center">
                        <html:submit
                                property="${deleteLocationUrl}"
                                alt="Delete a Asset Location"
                                title="Delete a Asset Location"
                                styleClass="btn btn-red"
                                value="Delete"/>
                    </div>
                </c:when>
                <c:otherwise><h3>Location</h3></c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <th width="25%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${camsLocationAttributes.itemQuantity}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute attributeEntry="${camsLocationAttributes.itemQuantity}"
                                      property="${camsAssetLocationProperty}.itemQuantity"
                                      readOnly="${!(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                                      tabindexOverride="${tabindexOverrideBase + 5}"/>
        </td>
        <th width="25%">&nbsp;</th>
        <td class="datacell" width="25%">&nbsp;</td>
    </tr>
    <tr>
        <th width="25%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${camsLocationAttributes.campusCode}"/>
        </th>
        <td class="datacell" width="25%">
        	<html:hidden property="${camsAssetLocationProperty}.campusCode" />
            <kul:htmlControlAttribute attributeEntry="${camsLocationAttributes.campusCode}"
                                      property="${camsAssetLocationProperty}.campusCode" readOnly="true"/>&nbsp;
            <c:if test="${(fullEntryMode or amendmentEntry or enableCa) && !poItemInactive}">
                <kul:lookup boClassName="org.kuali.kfs.vnd.businessobject.CampusParameter" fieldConversions="campusCode:${camsAssetLocationProperty}.campusCode"/>
            </c:if>
        </td>
        <th width="25%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${camsLocationAttributes.capitalAssetCityName}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute attributeEntry="${camsLocationAttributes.capitalAssetCityName}"
                                      property="${camsAssetLocationProperty}.capitalAssetCityName"
                                      readOnly="${!offCampus or !(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                                      tabindexOverride="${tabindexOverrideBase + 7}"/>
        </td>
    </tr>
    <tr>
        <th width="25%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${camsLocationAttributes.buildingCode}"/>
        </th>
        <td class="datacell" width="25%">
        	<html:submit value="Find Building" style="display:none;" property="methodToCall.populateBuilding" styleId="populate-building-item${ctr}-location${ctr2}-button"/>
            <kul:htmlControlAttribute attributeEntry="${camsLocationAttributes.buildingCode}"
                                      property="${camsAssetLocationProperty}.buildingCode"
                                      onblur="updateAssetLocation('${ctr}', '${ctr2}')" readOnly="${offCampus || readOnlyBuildingCode}"/>&nbsp;
            <c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive}">
                <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.Building"
                            lookupParameters="${camsAssetLocationProperty}.campusCode:campusCode"
                            fieldConversions="buildingCode:${camsAssetLocationProperty}.buildingCode,campusCode:${camsAssetLocationProperty}.campusCode"
                            anchor="${currentTabIndex}"/>&nbsp;&nbsp;
                <html:submit property="${refreshAssetLocationBuildingUrl}"
                             alt="building not found"
                             styleClass="btn btn-default small"
                             value="Off Campus"/>
            </c:if>
        </td>
        <th width="25%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${camsLocationAttributes.capitalAssetStateCode}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute attributeEntry="${camsLocationAttributes.capitalAssetStateCode}"
                                      property="${camsAssetLocationProperty}.capitalAssetStateCode"
                                      readOnly="${!offCampus or !(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                                      tabindexOverride="${tabindexOverrideBase + 7}"/>
        </td>
    </tr>
    <tr>
        <th width="25%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${camsLocationAttributes.capitalAssetLine1Address}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute attributeEntry="${camsLocationAttributes.capitalAssetLine1Address}"
                                      property="${camsAssetLocationProperty}.capitalAssetLine1Address"
                                      readOnly="${!offCampus or !(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                                      tabindexOverride="${tabindexOverrideBase + 5}"/>
        </td>
        <th width="25%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${camsLocationAttributes.capitalAssetPostalCode}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute attributeEntry="${camsLocationAttributes.capitalAssetPostalCode}"
                                      property="${camsAssetLocationProperty}.capitalAssetPostalCode"
                                      readOnly="${!offCampus or !(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                                      tabindexOverride="${tabindexOverrideBase + 7}"/>
        </td>
    </tr>
    <tr>
        <logic:notEmpty name="KualiForm" property="${camsAssetLocationProperty}.buildingCode">
            <c:set var="buildingSelected" value="true" />
        </logic:notEmpty>
        <th width="25%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${camsLocationAttributes.buildingRoomNumber}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute attributeEntry="${camsLocationAttributes.buildingRoomNumber}"
                                      property="${camsAssetLocationProperty}.buildingRoomNumber"
                                      readOnly="${!(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                                      tabindexOverride="${tabindexOverrideBase + 5}"/>&nbsp;
            <c:if test="${(fullEntryMode or amendmentEntry or enableCa) && !poItemInactive && !offCampus && buildingSelected}">
                <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.Room"
                            readOnlyFields="buildingCode,campusCode"
                            lookupParameters="'Y':active,${camsAssetLocationProperty}.campusCode:campusCode,${camsAssetLocationProperty}.buildingCode:buildingCode"
                            fieldConversions="buildingRoomNumber:${camsAssetLocationProperty}.buildingRoomNumber"/>
            </c:if>
        </td>
        <th width="25%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${camsLocationAttributes.capitalAssetCountryCode}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute attributeEntry="${camsLocationAttributes.capitalAssetCountryCode}"
                                      property="${camsAssetLocationProperty}.capitalAssetCountryCode"
                                      readOnly="${!offCampus or !(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                                      tabindexOverride="${tabindexOverrideBase + 7}"/>
        </td>
    </tr>
</table>
