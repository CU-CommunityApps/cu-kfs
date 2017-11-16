<%--
   - The Kuali Financial System, a comprehensive financial management system for higher education.
   -
   - Copyright 2005-2017 Kuali, Inc.
   -
   - This program is free software: you can redistribute it and/or modify
   - it under the terms of the GNU Affero General Public License as
   - published by the Free Software Foundation, either version 3 of the
   - License, or (at your option) any later version.
   -
   - This program is distributed in the hope that it will be useful,
   - but WITHOUT ANY WARRANTY; without even the implied warranty of
   - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   - GNU Affero General Public License for more details.
   -
   - You should have received a copy of the GNU Affero General Public License
   - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ tag description="render the given field in the capital asset info object"%>

<%@ attribute name="capitalAssetInfoDetails" required="true" type="java.lang.Object"
	description="The capital asset info object containing the data being displayed"%>
<%@ attribute name="capitalAssetInfoDetailsName" required="true" description="The name of the capital asset info object"%>	
<%@ attribute name="readOnly" required="false" description="Whether the capital asset information should be read only" %>	
<%@ attribute name="capitalAssetInfoIndex" required="true" description="Gives the capital asset information index" %>	
	
<c:set var="attributes" value="${DataDictionary.CapitalAssetInformationDetail.attributes}" />		
<c:set var="attributes2" value="${DataDictionary.CapitalAssetInformationDetailExtendedAttribute.attributes}" />		
<c:set var="dataCellCssClass" value="datacell"/>

<c:if test="${not empty capitalAssetInfoDetails}">
	<table style="border-top: 1px dashed #c3c3c3;" cellpadding="0" cellspacing="0" class="datatable" summary="Capital Asset Information Details">
        <tr class="header">
	   		<kul:htmlAttributeHeaderCell literalLabel=""/>
	   	    <kul:htmlAttributeHeaderCell attributeEntry="${attributes.capitalAssetTagNumber}" labelFor="${capitalAssetInfoDetailsName}.capitalAssetTagNumber"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${attributes.capitalAssetSerialNumber}" labelFor="${capitalAssetInfoDetailsName}.capitalAssetSerialNumber"/>                      
		    <kul:htmlAttributeHeaderCell attributeEntry="${attributes.campusCode}" labelFor="${capitalAssetInfoDetailsName}.campusCode"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${attributes.buildingCode}" labelFor="${capitalAssetInfoDetailsName}.buildingCode"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${attributes.buildingRoomNumber}" labelFor="${capitalAssetInfoDetailsName}.buildingRoomNumber"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${attributes.buildingSubRoomNumber}" labelFor="${capitalAssetInfoDetailsName}.buildingSubRoomNumber"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${attributes2.assetLocationStreetAddress}" labelFor="${capitalAssetInfoDetailsName}.assetLocationStreetAddress"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${attributes2.assetLocationCityName}" labelFor="${capitalAssetInfoDetailsName}.assetLocationCityName"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${attributes2.assetLocationStateCode}" labelFor="${capitalAssetInfoDetailsName}.assetLocationStateCode"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${attributes2.assetLocationCountryCode}" labelFor="${capitalAssetInfoDetailsName}.assetLocationCountryCode"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${attributes2.assetLocationZipCode}" labelFor="${capitalAssetInfoDetailsName}.assetLocationZipCode"/>
			<c:if test="${!readOnly}">
				<kul:htmlAttributeHeaderCell literalLabel="Action"/>
			</c:if>
	   </tr>
	   
   	   <c:forEach items="${capitalAssetInfoDetails}" var="detailLine" varStatus="status">
            <tr class="${status.index % 2 == 0 ? "highlight" : ""}">
	   		<c:set var="lineNumber" value="${status.index + 1}"/>
			<kul:htmlAttributeHeaderCell literalLabel="${lineNumber}"/>	
	   		
            <fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="capitalAssetTagNumber" lookup="false" inquiry="false"/>	
				   
            <fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="capitalAssetSerialNumber" lookup="false" inquiry="false"/>
				
            <fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="campusCode" lookup="false" inquiry="true"
                    boClassSimpleName="Campus" boPackageName="org.kuali.rice.location.api.campus"
				lookupOrInquiryKeys="campusCode"
				businessObjectValuesMap="${capitalAssetInfoDetail.valuesMap}"/>	
			
            <fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="buildingCode" lookup="true" inquiry="true"
				boClassSimpleName="Building" boPackageName="org.kuali.kfs.sys.businessobject"
				lookupOrInquiryKeys="campusCode,buildingCode"
				businessObjectValuesMap="${capitalAssetInfoDetail.valuesMap}"/>
			
            <fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="buildingRoomNumber" lookup="true" inquiry="true"
				boClassSimpleName="Room" boPackageName="org.kuali.kfs.sys.businessobject"
				lookupOrInquiryKeys="campusCode,buildingCode,buildingRoomNumber"
				businessObjectValuesMap="${capitalAssetInfoDetail.valuesMap}"/>	
			
            <fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="buildingSubRoomNumber" lookup="false" inquiry="false"/>

			<fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="extension.assetLocationStreetAddress" lookup="false" inquiry="false"/>
			
			<fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="extension.assetLocationCityName" lookup="false" inquiry="false"/>
			
			<fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="extension.assetLocationStateCode" lookup="false" inquiry="false"/>
			
			<fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="extension.assetLocationCountryCode" lookup="false" inquiry="false"/>
				
			<fp:dataCell dataCellCssClass="${dataCellCssClass} left"
				businessObjectFormName="${capitalAssetInfoDetailsName}[${status.index}]" attributes="${attributes}" readOnly="${readOnly}"
				field="extension.assetLocationZipCode" lookup="false" inquiry="false"/>
			
			<c:if test="${!readOnly}">
                    <td class="infoline left">
                        <html:submit
                                property="methodToCall.deleteCapitalAssetInfoDetailLine.line${lineNumber}.Anchor"
							title="delete the capital Asset Information Detail line ${lineNumber}"
                                alt="delete the capital Asset Information Detail line ${lineNumber}"
                                styleClass="btn btn-red"
                                value="Delete"/>
				</td>
			</c:if>																									 
	   </tr>
	   </c:forEach>
	</table>
</c:if>
