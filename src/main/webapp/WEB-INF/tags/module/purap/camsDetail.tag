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
<%@ attribute name="camsItemIndex" required="true" description="cams item index"%>
<%@ attribute name="camsSystemAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="camsAssetAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="camsLocationAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="ctr" required="true" description="item count"%>
<%@ attribute name="camsAssetSystemProperty" required="true" description="String that represents the prefix of the property name to store into the document on the form."%>
<%@ attribute name="availability" required="true" description="Determines if this is a capture once or each tag or for each"%>
<%@ attribute name="isRequisition" required="false" description="Determines if this is a requisition document"%>
<%@ attribute name="isPurchaseOrder" required="false" description="Determines if this is a requisition document"%>
<%@ attribute name="poItemInactive" required="false" description="True if the item this is part of is inactive."%>
<!--  KFSPTS-1792 : allow FO to edit REQ capital asset tab add 'enableCa' in purCams.tag-->


<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])}" />
<c:set var="addItemAssetUrl" value="methodToCall.addItemCapitalAssetByItem.line${ctr}" />
<c:set var="deleteItemAssetUrl" value="methodToCall.deleteItemCapitalAssetByItem.line${camsItemIndex}.(((${ctr})))" />
<c:set var="setManufacturerFromVendorUrl" value="methodToCall.setManufacturerFromVendorByItem.line${ctr}" />
<c:set var="selectNotCurrentYearUrl" value="methodToCall.selectNotCurrentYearByItem.line${ctr}" />
<c:set var="clearNotCurrentYearUrl" value="methodToCall.clearNotCurrentYearByItem.line${ctr}" />
<c:if test="${PurapConstants.CapitalAssetAvailability.ONCE eq availability}">
	<c:set var="addItemAssetUrl" value="methodToCall.addItemCapitalAssetByDocument.line${ctr}" />
	<c:set var="deleteItemAssetUrl" value="methodToCall.deleteItemCapitalAssetByDocument.line${camsItemIndex}.(((${ctr})))" />
	<c:set var="setManufacturerFromVendorUrl" value="methodToCall.setManufacturerFromVendorByDocument.line${ctr}" />	
    <c:set var="selectNotCurrentYearUrl" value="methodToCall.selectNotCurrentYearByDocument.line${ctr}" />    
    <c:set var="clearNotCurrentYearUrl" value="methodToCall.clearNotCurrentYearByDocument.line${ctr}" />    
</c:if>
<c:set var="tabindexOverrideBase" value="60" />

<c:if test="${KualiForm.purchasingItemCapitalAssetAvailability eq availability}">
	<tr>
    	<th class="datacell right">Add Asset Number:</th>
        <td class="datacell" colspan="3">
        	<kul:htmlControlAttribute attributeEntry="${camsAssetAttributes.capitalAssetNumber}"
                                      property="document.purchasingCapitalAssetItems[${camsItemIndex}].newPurchasingItemCapitalAssetLine.capitalAssetNumber"
                                      readOnly="${!(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                                      tabindexOverride="${tabindexOverrideBase + 0}"/>
            <c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive}">
            	<kul:lookup boClassName="org.kuali.kfs.integration.cam.CapitalAssetManagementAsset" fieldConversions="capitalAssetNumber:document.purchasingCapitalAssetItems[${camsItemIndex}].newPurchasingItemCapitalAssetLine.capitalAssetNumber" lookupParameters="document.purchasingCapitalAssetItems[${camsItemIndex}].newPurchasingItemCapitalAssetLine.capitalAssetNumber:capitalAssetNumber"/>
            </c:if>
					
	      	&nbsp;
	      	<c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive}">
				<html:submit
						property="${addItemAssetUrl}"
						alt="Insert an Item Capital Asset"
						title="Add an Item Capital Asset"
						styleClass="btn btn-green"
						value="Add"/>
			</c:if>
		</td>
	</tr>
	<tr>
        <th class="right top">
            <kul:htmlAttributeLabel attributeEntry="${camsAssetAttributes.capitalAssetNumber}"/>
        </th>
        <td class="datacell">
	    	<logic:iterate indexId="idx" name="KualiForm" property="${camsAssetSystemProperty}.itemCapitalAssets" id="asset">
            	<kul:htmlControlAttribute
                        attributeEntry="${camsAssetAttributes.capitalAssetNumber}"
                        property="${camsAssetSystemProperty}.itemCapitalAssets[${idx}].capitalAssetNumber"
                        readOnly="true"/>
                <c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive}">
                    <html:html-button
                            property="${deleteItemAssetUrl}.((#${idx}#))"
                            alt="Delete an Asset Number"
                            title="Delete an Asset Number"
                            styleClass="btn clean"
                            value="Delete"
                            innerHTML="<span class=\"fa fa-trash\"></span>"/>
	            </c:if>
                <br/>
	        </logic:iterate>
		</td>
	</tr>
</c:if>
	
<c:if test="${(KualiForm.purchasingCapitalAssetSystemCommentsAvailability eq availability) or (KualiForm.purchasingCapitalAssetSystemDescriptionAvailability eq availability)}">
    <tr>
		<c:if test="${KualiForm.purchasingCapitalAssetSystemCommentsAvailability eq availability}">
	    	<bean:define id="capitalAssetNoteTextValue" property="${camsAssetSystemProperty}.capitalAssetNoteText" name="KualiForm" />
            <th class="right top">
                <kul:htmlAttributeLabel attributeEntry="${camsSystemAttributes.capitalAssetNoteText}"/>
  	        </th>
		    <td class="datacell">
			    <kul:htmlControlAttribute
			   	        attributeEntry="${camsSystemAttributes.capitalAssetNoteText}" 
			   	        property="${camsAssetSystemProperty}.capitalAssetNoteText" 
			   	        readOnly="${!(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}" 
			   	        readOnlyAlternateDisplay="${capitalAssetNoteTextValue}"		 
			   	        tabindexOverride="${tabindexOverrideBase + 0}"/>
			</td>
		</c:if>
	    <c:if test="${!(KualiForm.purchasingCapitalAssetSystemCommentsAvailability eq availability)}">
			<th>&nbsp;</th>
			<td class="datacell">&nbsp;</td>
	    </c:if>
		<c:if test="${KualiForm.purchasingCapitalAssetSystemDescriptionAvailability eq availability}">
			<bean:define id="capitalAssetSystemDescriptionValue" property="${camsAssetSystemProperty}.capitalAssetSystemDescription" name="KualiForm" />
            <th class="right top">
                <kul:htmlAttributeLabel attributeEntry="${camsSystemAttributes.capitalAssetSystemDescription}"/>
            </th>
	        <td class="datacell">
		    	<kul:htmlControlAttribute
		    		    attributeEntry="${camsSystemAttributes.capitalAssetSystemDescription}" 
		    		    property="${camsAssetSystemProperty}.capitalAssetSystemDescription" 
		    		    readOnly="${!(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}" 
		    		    readOnlyAlternateDisplay="${capitalAssetSystemDescriptionValue}"		    		    
		    		    tabindexOverride="${tabindexOverrideBase + 3}"/>
			</td>
		</c:if>
	    <c:if test="${!(KualiForm.purchasingCapitalAssetSystemDescriptionAvailability eq availability)}">
	        <th>&nbsp;</th>
	        <td class="datacell">&nbsp;</td>
	    </c:if>
    </tr>
</c:if>

<c:if test="${KualiForm.purchasingCapitalAssetSystemAvailability eq availability}">
    <tr>
        <th class="right" width="25%">
            <kul:htmlAttributeLabel attributeEntry="${camsSystemAttributes.capitalAssetNotReceivedCurrentFiscalYearIndicator}"/>
        </th>
        <td class="datacell" width="25%">
			<kul:htmlControlAttribute attributeEntry="${camsSystemAttributes.capitalAssetNotReceivedCurrentFiscalYearIndicator}" property="${camsAssetSystemProperty}.capitalAssetNotReceivedCurrentFiscalYearIndicator" readOnly="true"/>&nbsp;
            <c:set var="notCurrentYear" value="false" />
            <logic:equal name="KualiForm" property="${camsAssetSystemProperty}.capitalAssetNotReceivedCurrentFiscalYearIndicator" value="Yes">
                <c:set var="notCurrentYear" value="true" />
            </logic:equal>
            <c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive and !notCurrentYear}">
                <html:submit
                        property="${selectNotCurrentYearUrl}"
                        alt="Select"
                        styleClass="btn btn-default small"
                        value="Select"/>
            </c:if>
            <c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive and notCurrentYear}">
                <html:submit
                        property="${clearNotCurrentYearUrl}"
                        alt="Clear Selection"
                        styleClass="btn btn-default small"
                        value="Clear"/>
            </c:if>
		</td>
        <th class="right top" width="25%">
            <kul:htmlAttributeLabel attributeEntry="${camsSystemAttributes.capitalAssetManufacturerName}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute
                    attributeEntry="${camsSystemAttributes.capitalAssetManufacturerName}"
                    property="${camsAssetSystemProperty}.capitalAssetManufacturerName"
                    readOnly="${!(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                    tabindexOverride="${tabindexOverrideBase + 3}"/>
            <c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive}">
                <html:submit
                        property="${setManufacturerFromVendorUrl}"
                        alt="Manufacturer Same as Vendor"
                        title="Manufacturer Same as Vendor"
                        styleClass="btn btn-default small"
                        value="Same as Vendor"/>
            </c:if>
        </td>
    </tr>
    <tr>
        <th class="right" width="25%">
            <kul:htmlAttributeLabel attributeEntry="${camsSystemAttributes.capitalAssetTypeCode}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute attributeEntry="${camsSystemAttributes.capitalAssetTypeCode}"
                                      property="${camsAssetSystemProperty}.capitalAssetTypeCode"
                                      readOnly="${!(fullEntryMode or amendmentEntry or enableCa) or poItemInactive or notCurrentYear}"
                                      tabindexOverride="${tabindexOverrideBase + 0}"/>
            <c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive and !notCurrentYear}">
                  <kul:lookup boClassName="org.kuali.kfs.integration.cam.CapitalAssetManagementAssetType" fieldConversions="capitalAssetTypeCode:${camsAssetSystemProperty}.capitalAssetTypeCode" lookupParameters="${camsAssetSystemProperty}.capitalAssetTypeCode:capitalAssetTypeCode"/>
            </c:if>
        </td>
        <th class="right" width="25%">
            <kul:htmlAttributeLabel attributeEntry="${camsSystemAttributes.capitalAssetModelDescription}"/>
        </th>
        <td class="datacell" width="25%">
            <kul:htmlControlAttribute
                    attributeEntry="${camsSystemAttributes.capitalAssetModelDescription}"
                    property="${camsAssetSystemProperty}.capitalAssetModelDescription"
                    readOnly="${!(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                    tabindexOverride="${tabindexOverrideBase + 3}"/>
		</td>
    </tr>
    <c:if test="${KualiForm.purchasingCapitalAssetCountAssetNumberAvailability eq availability}">
	    <tr>
            <th class="right" width="25%">
                <kul:htmlAttributeLabel attributeEntry="${camsSystemAttributes.capitalAssetCountAssetNumber}"/>
            </th>
            <td class="datacell" width="25%">
                <kul:htmlControlAttribute
                        attributeEntry="${camsSystemAttributes.capitalAssetCountAssetNumber}"
                        property="${camsAssetSystemProperty}.capitalAssetCountAssetNumber"
                        readOnly="${!(fullEntryMode or amendmentEntry or enableCa) or poItemInactive}"
                        tabindexOverride="${tabindexOverrideBase + 0}"/>
	        </td>
            <th width="25%">&nbsp;</th>
            <td class="datacell" width="25%">&nbsp;</td>
		</tr>
	</c:if>

    <tr>
        <td colspan="4">
			<c:set var="locationPrefix" value=""/>
			<c:set var="addCapitalAssetLocationUrl" value="methodToCall.addCapitalAssetLocationByDocument.line${ctr}"/>			
			<c:if test="${availability eq PurapConstants.CapitalAssetAvailability.EACH}">
				<c:set var="locationPrefix" value="${camsAssetSystemProperty}."/>
				<c:set var="addCapitalAssetLocationUrl" value="methodToCall.addCapitalAssetLocationByItem.line${ctr}"/>
			</c:if>

			<!-- Cams Location Entry -->
            <c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive}">
                <purap:camsLocation camsLocationAttributes="${camsLocationAttributes}" ctr="${ctr}"
                                    ctr2="new"
                                    camsAssetLocationProperty="${locationPrefix}newPurchasingCapitalAssetLocationLine"
                                    availability="${availability}" poItemInactive="${poItemInactive}"/>
    	    </c:if>
			
            <table class="standard">
                <c:if test="${(fullEntryMode or amendmentEntry or enableCa) and !poItemInactive}">
	            	<tr>
                    	<td colspan="4" class="center">
                            <html:submit
                                    property="${addCapitalAssetLocationUrl}"
                                    alt="Add a Asset Location"
                                    title="Add a Asset Location"
                                    styleClass="btn btn-green"
                                    value="Add"/>
						</td>
	            	</tr>
	        	</c:if>

				<logic:iterate indexId="ctr2" name="KualiForm" property="${camsAssetSystemProperty}.capitalAssetLocations" id="location">
								
					<!-- Cams Locations -->
					<c:set var="currentTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
					<c:set var="topLevelTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
					<c:set var="tabTitle" value="AccountingLines-${currentTabIndex}" />
					<c:set var="tabKey" value="${kfunc:generateTabKey(tabTitle)}"/>
					<!--  hit form method to increment tab index -->
					<c:set var="dummyIncrementer" value="${kfunc:incrementTabIndex(KualiForm, tabKey)}" />
					<c:set var="currentTab" value="${kfunc:getTabState(KualiForm, tabKey)}"/>

					<%-- default to closed --%>
					<c:choose>
						<c:when test="${empty currentTab}">
							<c:set var="isOpen" value="true" />
						</c:when>
						<c:when test="${!empty currentTab}">
							<c:set var="isOpen" value="${currentTab == 'OPEN'}" />
						</c:when>
					</c:choose>

					<html:hidden property="tabStates(${tabKey})" value="${(isOpen ? 'OPEN' : 'CLOSE')}" />
			
					<tr>
                        <td class="infoline" colspan="4">
                            <table class="standard">
								<tr>
                                    <th colspan="10" >
                                        <h3>
                                            Address ${ctr2+1}
                                            &nbsp;
						  	    			<c:if test="${isOpen == 'true' || isOpen == 'TRUE'}">
                                                <html:submit
                                                        property="methodToCall.toggleTab.tab${tabKey}"
                                                        alt="hide" title="toggle"
                                                        styleClass="btn btn-default small"
                                                        styleId="tab-${tabKey}-imageToggle"
                                                        onclick="return toggleTab(document, 'kualiFormModal', '${tabKey}');"
                                                        value="Hide"/>
						     				</c:if>
						     				<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
                                                <html:submit
                                                        property="methodToCall.toggleTab.tab${tabKey}"
                                                        alt="show" title="toggle"
                                                        styleClass="btn btn-default small"
                                                        styleId="tab-${tabKey}-imageToggle"
                                                        onclick="return toggleTab(document, 'kualiFormModal', '${tabKey}');"
                                                        value="Show"/>
						     					</c:if>
                                        </h3>
									</th>
								</tr>
				
                                <c:choose>
                                    <c:when test="${isOpen != 'true' && isOpen != 'TRUE'}">
										<tr style="display: none;"  id="tab-${tabKey}-div">
                                    </c:when>
                                    <c:otherwise><tr id="tab-${tabKey}-div"></c:otherwise>
                                </c:choose>
                                    <th colspan="10">
										<!-- Cams Location List -->
                                        <purap:camsLocation
                                                camsLocationAttributes="${camsLocationAttributes}"
                                                ctr="${ctr}" ctr2="${ctr2}"
                                                camsAssetLocationProperty="${camsAssetSystemProperty}.capitalAssetLocations[${ctr2}]"
                                                availability="${availability}"
                                                poItemInactive="${poItemInactive}"/>
				        			</th>
								</tr>
							</table>
						</td>
					</tr>
				</logic:iterate>
			</table>
		</td>
    </tr>
</c:if>
