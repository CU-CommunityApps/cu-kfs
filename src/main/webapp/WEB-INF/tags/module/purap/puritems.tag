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
<%@ tag import="org.kuali.kfs.sys.util.Guid" %>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="displayRequisitionFields" required="false" description="Boolean to indicate if REQ specific fields should be displayed"%>
<%@ attribute name="itemAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="accountingLineAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's accounting line fields."%>
<%@ attribute name="extraHiddenItemFields" required="false" description="A comma seperated list of names to be added to the list of normally hidden fields for the existing misc items." %>

<script language="JavaScript" type="text/javascript" src="dwr/interface/PurapCommodityCodeService.js"></script>
<script language="JavaScript" type="text/javascript" src="scripts/vnd/objectInfo.js"></script>
<script language="JavaScript" type="text/javascript" src="dwr/interface/ItemUnitOfMeasureService.js"></script>
<script language="JavaScript" type="text/javascript" src="scripts/module/purap/objectInfo.js"></script>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])}" />
<c:set var="amendmentEntry"	value="${(not empty KualiForm.editingMode['amendmentEntry'])}" />
<c:set var="lockB2BEntry" value="${(not empty KualiForm.editingMode['lockB2BEntry'])}" />
<c:set var="unorderedItemAccountEntry"	value="${(not empty KualiForm.editingMode['unorderedItemAccountEntry'])}" />
<c:set var="amendmentEntryWithUnpaidPreqOrCM" value="${(amendmentEntry && (KualiForm.document.containsUnpaidPaymentRequestsOrCreditMemos))}" />
<c:set var="lockTaxAmountEntry" value="${(not empty KualiForm.editingMode['lockTaxAmountEntry']) || !fullEntryMode}" />
<c:set var="purapTaxEnabled" value="${(not empty KualiForm.editingMode['purapTaxEnabled'])}" />
<c:set var="displayCommodityCodeFields" value="${KualiForm.editingMode['enableCommodityCode']}"/>

<c:set var="documentType" value="${KualiForm.document.documentHeader.workflowDocument.documentTypeName}" />
<c:set var="isATypeOfPODoc" value="${KualiForm.document.isATypeOfPODoc}" />
<c:set var="isPurchaseOrder" value="${KualiForm.document.isPODoc}" />
<c:set var="hasItems" value="${fn:length(KualiForm.document.items) > 0}" />
<c:set var="hasLineItems" value="${fn:length(KualiForm.document.items) > fn:length(KualiForm.document.belowTheLineTypes)}" />

<c:set var="tabindexOverrideBase" value="50" />

<c:set var="mainColumnCount" value="15"/>
<c:if test="${not purapTaxEnabled}">
    <c:set var="mainColumnCount" value="13"/>
</c:if>

<c:set var="accountColumnCount" value="${mainColumnCount - 1}"/>
<%-- if it is B2B then we will not display the "Restricted" or "Assigned to Trade In" columns
     so we need to reduce the width of the accounting lines --%>
<c:if test="${lockB2BEntry}">
    <%-- "Assigned to Trade In" always needs to be accounted for --%>
    <c:set var="accountColumnCount" value="${accountColumnCount - 1}"/>
    <c:if test="${displayRequisitionFields}">
        <%-- "Restricted" is a REQ specific column so we only need account for it on on REQS not POs --%>
        <c:set var="accountColumnCount" value="${accountColumnCount - 1}"/>
    </c:if>
</c:if>

<c:set var="colSpanItemType" value="6"/>
<c:set var="colSpanDescription" value="2"/>
<c:set var="colSpanExtendedPrice" value="1"/>

<c:set var="actionColSpan" value="1"/>

<c:choose>
	<c:when test="${displayRequisitionFields}">
		<c:set var="colSpanAmountPaid" value="0"/>
		<c:set var="itemRowSpan" value="3"/>
	</c:when>
	<c:otherwise>
		<c:set var="colSpanAmountPaid" value="1"/>
		<c:set var="itemRowSpan" value="3"/>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${displayCommodityCodeFields}">
		<c:set var="colSpanCatlogNumber" value="1"/>
	</c:when>
	<c:otherwise>
		<c:set var="colSpanCatlogNumber" value="2"/>
	</c:otherwise>
</c:choose>

<kul:tab tabTitle="Items" defaultOpen="true" tabErrorKey="${PurapConstants.ITEM_TAB_ERRORS},accountDistributionnewSourceLine*,accountDistributionsourceAccountingLine*,document.favoriteAccountLineIdentifier">
	<div class="tab-container" style="overflow:auto;">
		<c:if test="${!KualiForm.document.inquiryRendered}">
			<div class="left">
				Object Code and Sub-Object Code inquiries and descriptions have been removed because this is a prior year document.
			</div>
		</c:if>

	
        <c:set var="itemCount" value="0"/>
        <logic:iterate indexId="ctr" name="KualiForm" property="document.items" id="itemLine">
            <c:if test="${itemLine.itemType.lineItemIndicator == true}">
                <c:set var="itemCount" value="${itemCount + 1}"/>
            </c:if>
        </logic:iterate>
        <table class="standard side-margins acct-lines ${itemCount > 99 ? 'large-seq' : ''}" summary="Items Section">
			<c:if test="${(fullEntryMode or amendmentEntry) and !lockB2BEntry}">
                <tr class="title">
                    <th style="visibility: hidden;"></th>
                    <td colspan="3">
                        <h2>
                            Add Item
                            <kul:help alternativeHelp="${KualiForm.lineItemImportInstructionsUrl}" alternativeHelpLabel="Import Templates"/>
                        </h2>
					</td>
                    <td colspan="${mainColumnCount - 4}" class="right nowrap">
						<SCRIPT type="text/javascript">
							<!--
							function hideImport() {
								document.getElementById("showLink").style.display="inline";
								document.getElementById("uploadDiv").style.display="none";
							}
							function showImport() {
								document.getElementById("showLink").style.display="none";
								document.getElementById("uploadDiv").style.display="inline";
							}
							document.write(
								'<a id="showLink" href="#" onclick="showImport();return false;">' +
										'<button title="import items from file" class="btn btn-default uppercase" alt="import items from file">' +
										'Import Lines' +
										'<\/button>' +
								'<\/a>' +
										'<div id="uploadDiv" style="display:none; float:right;" >' +
								'<html:file size="30" property="itemImportFile" />' +
										'<html:submit property="methodToCall.importItems" styleClass="btn btn-green" alt="add imported items" title="add imported items" value="Add"/>' +
										'<html:submit property="methodToCall.cancel" styleClass="btn btn-default" alt="cancel import" title="cancel import" onclick="hideImport();return false;" value="Cancel" />' +
								'<\/div>');
							//-->
	            		</SCRIPT>
						<NOSCRIPT>
							Import lines
							<html:file size="30" property="itemImportFile" style="font:10px;height:16px;" />
								<html:submit property="methodToCall.importItems" alt="add imported items" title="add imported items" styleClass="btn btn-green" value="Add" />
						</NOSCRIPT>
					</td>
				</tr>
            </c:if>

            <c:if test="${(fullEntryMode or amendmentEntry) and !lockB2BEntry}">
                <tr class="header top">
                    <th></th>
					<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTypeCode}" />
					<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemQuantity}"/>
                    <kul:htmlAttributeHeaderCell>
                        <kul:htmlAttributeLabel attributeEntry="${itemAttributes.itemUnitOfMeasureCode}" useShortLabel="true" noColon="true"/>
                    </kul:htmlAttributeHeaderCell>
					<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemCatalogNumber}" colspan="${colSpanCatlogNumber}" />
				
					<c:if test="${displayCommodityCodeFields}">
						<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.purchasingCommodityCode}" />
					</c:if>
				
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}"  colspan="${colSpanDescription}" forceRequired="true"/>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitPrice}" forceRequired="true" addClass="right"/>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.extendedPrice}" addClass="right" />

				
					<c:if test="${purapTaxEnabled}">
							<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTaxAmount}" addClass="right"/>
							<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.totalAmount}" addClass="right"/>
					</c:if>
				
					<c:if test="${displayRequisitionFields}">
							<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemRestrictedIndicator}" addClass="center" />
					</c:if>
				
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemAssignedToTradeInIndicator}" addClass="center" />

                    <c:if test="${isATypeOfPODoc}">
                        <kul:htmlAttributeHeaderCell literalLabel="" />
                    </c:if>
                    <kul:htmlAttributeHeaderCell literalLabel="Actions" colspan="${actionColSpan}"/>

				</tr>
                <tr class="top new">
					<td class="infoline">
						<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemLineNumber}" property="newPurchasingItemLine.itemLineNumber" readOnly="true"/>
					</td>
					<td class="infoline">
						<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemTypeCode}" property="newPurchasingItemLine.itemTypeCode" tabindexOverride="${tabindexOverrideBase + 0}"/>
					</td>
					<td class="infoline">
						<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemQuantity}" property="newPurchasingItemLine.itemQuantity" tabindexOverride="${tabindexOverrideBase + 0}"/>
					</td>
                    <td class="infoline nowrap">
						<c:set var="itemUnitOfMeasureCodeField"  value="newPurchasingItemLine.itemUnitOfMeasureCode" />
						<c:set var="itemUnitOfMeasureDescriptionField"  value="newPurchasingItemLine.itemUnitOfMeasure.itemUnitOfMeasureDescription" />
						<kul:htmlControlAttribute
								attributeEntry="${itemAttributes.itemUnitOfMeasureCode}"
								property="${itemUnitOfMeasureCodeField}"
								readOnly="${readOnly}"
								onblur="loadItemUnitOfMeasureInfo( '${itemUnitOfMeasureCodeField}', '${itemUnitOfMeasureDescriptionField}' );${onblur}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                        <!-- This hidden tag contains static values that can be picked up by new lookups to pre-populate fields -->
                        <input type="hidden" name="static.unitOfMeasureActive" value="Y" />
                        <kul:lookup
                                boClassName="org.kuali.kfs.sys.businessobject.UnitOfMeasure"
                                fieldConversions="active:static.unitOfMeasureActive"
                                lookupParameters="newPurchasingItemLine.itemUnitOfMeasureCode:itemUnitOfMeasureCode,newPurchasingItemLine.itemUnitOfMeasure.itemUnitOfMeasureDescription.div:itemUnitOfMeasureDescription"
                                fieldPropertyName="${itemUnitOfMeasureCodeField}"
                                newLookup="true"
                                addClass="embed"/>
						<div id="newPurchasingItemLine.itemUnitOfMeasure.itemUnitOfMeasureDescription.div" class="fineprint">
							<html:hidden write="true" property="${itemUnitOfMeasureDescriptionField}"/>
						</div>
					</td>
					<td class="infoline" colspan="${colSpanCatlogNumber}">
						<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemCatalogNumber}" property="newPurchasingItemLine.itemCatalogNumber" tabindexOverride="${tabindexOverrideBase + 0}"/>
					</td>
			    
				    <c:if test="${displayCommodityCodeFields}">
                        <td class="infoline nowrap">
							<c:set var="commodityCodeField"  value="newPurchasingItemLine.purchasingCommodityCode" />
							<c:set var="commodityDescriptionField"  value="newPurchasingItemLine.commodityCode.commodityDescription" />
							<kul:htmlControlAttribute
									attributeEntry="${itemAttributes.purchasingCommodityCode}"
									property="${commodityCodeField}"
									onblur="loadCommodityCodeDescription( '${commodityCodeField}', '${commodityDescriptionField}' );${onblur}" readOnly="${readOnly}" tabindexOverride="${tabindexOverrideBase + 0}"/>
							<kul:lookup
									boClassName="org.kuali.kfs.vnd.businessobject.CommodityCode"
									fieldConversions="purchasingCommodityCode:newPurchasingItemLine.purchasingCommodityCode"
									lookupParameters="'Y':active"
									addClass="embed"/>
							<div id="newPurchasingItemLine.commodityCode.commodityDescription.div" class="fineprint">
									<html:hidden write="true" property="${commodityDescriptionField}"/>
							</div>
						</td>
	                </c:if>
                    <td class="infoline relative wrap-break-word" colspan="${colSpanDescription}">
                       <kul:htmlControlAttribute
                               attributeEntry="${itemAttributes.itemDescription}"
                               property="newPurchasingItemLine.itemDescription"
                               tabindexOverride="${tabindexOverrideBase + 0}"
                               styleClass="fullwidth"/>
					</td>
                    <td class="infoline right">
				        <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemUnitPrice}" property="newPurchasingItemLine.itemUnitPrice" tabindexOverride="${tabindexOverrideBase + 0}"/>
					</td>
                    <td class="infoline right">
 				        <kul:htmlControlAttribute attributeEntry="${itemAttributes.extendedPrice}" property="newPurchasingItemLine.extendedPrice" readOnly="true" />
					</td>
					<c:if test="${purapTaxEnabled}">
                        <td class="infoline right">
 				        	<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemTaxAmount}" property="newPurchasingItemLine.itemTaxAmount" readOnly="true" />
						</td>
                        <td class="infoline right">
 				        	<kul:htmlControlAttribute attributeEntry="${itemAttributes.totalAmount}" property="newPurchasingItemLine.totalAmount" readOnly="true" />
						</td>
					</c:if>
				
					<c:if test="${displayRequisitionFields}">
                        <td class="infoline center">
  					        <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemRestrictedIndicator}" property="newPurchasingItemLine.itemRestrictedIndicator" tabindexOverride="${tabindexOverrideBase + 0}"/>
						</td>
					</c:if>
                    <td class="infoline center">
  		                <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemAssignedToTradeInIndicator}" property="newPurchasingItemLine.itemAssignedToTradeInIndicator" tabindexOverride="${tabindexOverrideBase + 0}"/>
					</td>
                    <c:if test="${isATypeOfPODoc}">
                        <td></td>
                    </c:if>
                    <td class="infoline" colspan="${actionColSpan}">
                        <div class="actions">
                            <html:html-button
                                    property="methodToCall.addItem"
                                    alt="Insert an Item"
                                    title="Add an Item"
                                    styleClass="btn btn-green skinny"
                                    tabindex="${tabindexOverrideBase + 0}"
                                    value="Add"
                                    innerHTML="<span class=\"fa fa-plus\"></span>"/>
				    	</div>
					</td>
				</tr>
			</c:if>
            <c:if test="${(fullEntryMode or amendmentEntry)}">
				<tr>
                    <th></th>
                    <th height=30 colspan="${accountColumnCount}" style="padding-bottom: 20px;">
			    		<purap:accountdistribution
								accountingLineAttributes="${accountingLineAttributes}"
			        			itemAttributes="${itemAttributes}"
								displayCommodityCodeFields="${displayCommodityCodeFields}" />
		    		</th>
				</tr>
            </c:if>

            <tr class="title">
                <th style="visibility: hidden;"></th>
                <td colspan="3" style="padding-top: 20px;">
                    <h2>Current Items</h2>
				</td>
			</tr>
		
        	<c:if test="${!lockB2BEntry and hasLineItems or lockB2BEntry and hasItems}">
                <tr class="header">
                    <th></th>
					<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTypeCode}" hideRequiredAsterisk="true" />
					<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemQuantity}" />
					<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitOfMeasureCode}" />
					<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemCatalogNumber}" colspan="${colSpanCatlogNumber}" />
				
					<c:if test="${displayCommodityCodeFields}">
						<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.purchasingCommodityCode}" />
					</c:if>
				
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}" colspan="2" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitPrice}" addClass="right" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.extendedPrice}" addClass="right" />

											
					<c:if test="${purapTaxEnabled}">
                        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTaxAmount}" addClass="right" />
                        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.totalAmount}" addClass="right" />
					</c:if>
				
					<c:if test="${displayRequisitionFields and !lockB2BEntry}">
                        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemRestrictedIndicator}" addClass="center" />
					</c:if>
				
					<c:if test="${!lockB2BEntry}">
                        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemAssignedToTradeInIndicator}" addClass="center" />
					</c:if>

					<c:if test="${isATypeOfPODoc}">
                        <kul:htmlAttributeHeaderCell literalLabel="Amount Paid" addClass="right" />
				    	<c:choose>
                        	<c:when test="${(documentType != 'PO' && !(fullEntryMode or amendmentEntry))}">
                                <kul:htmlAttributeHeaderCell literalLabel="Inactive"/>
                        	</c:when>
                        	<c:otherwise>
                                <kul:htmlAttributeHeaderCell literalLabel="Actions" colspan="${actionColSpan}"/>
                        	</c:otherwise>
                    	</c:choose>
                	</c:if>
                	<c:if test="${!isATypeOfPODoc}">
                        <kul:htmlAttributeHeaderCell literalLabel="Actions" colspan="${actionColSpan}"/>
                	</c:if>
				</tr>
			</c:if>

			<c:if test="${!lockB2BEntry and !hasLineItems or lockB2BEntry and !hasItems}">
				<tr>
					<th height=30 colspan="${mainColumnCount}">No items added to document</th>
				</tr>
			</c:if>

			<logic:iterate indexId="ctr" name="KualiForm" property="document.items" id="itemLine">
				<c:if test="${itemLine.itemType.lineItemIndicator == true}">
					<c:set var="currentTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
					<c:set var="topLevelTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />

					<c:choose>
						<c:when test="${itemLine.objectId == null}">
							<c:set var="newObjectId" value="<%= new Guid().toString()%>" />
							<c:set var="tabKey" value="Item-${newObjectId}" />
						</c:when>
						<c:when test="${itemLine.objectId != null}">
							<c:set var="tabKey" value="Item-${itemLine.objectId}" />
						</c:when>
					</c:choose>

					<c:set var="dummyIncrementer" value="${kfunc:incrementTabIndex(KualiForm, tabKey)}" />
					<c:set var="currentTab" value="${kfunc:getTabState(KualiForm, tabKey)}"/>

					<c:choose>
						<c:when test="${empty currentTab}">
							<c:set var="isOpen" value="true" />
						</c:when>
						<c:when test="${!empty currentTab}">
							<c:set var="isOpen" value="${currentTab == 'OPEN'}" />
						</c:when>
					</c:choose>

					<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
						<tbody style="display: none;" id="tab-${tabKey}-div">
					</c:if>

                    <tr class="top line">
                        <th class="infoline" nowrap="nowrap" rowspan="${itemRowSpan}" style="position: relative;">
                            <c:if test="${(fullEntryMode and !amendmentEntry) and !lockB2BEntry and ctr != 0}">
                                <html:html-button
                                        property="methodToCall.upItem.line${ctr}"
                                        alt="Move Item Up"
                                        title="Move Item Up"
                                        styleClass="btn clean move"
                                        value="Move Up"
                                        style="position:absolute; top:0; right:1px;"
                                        innerHTML="<span class=\"fa fa-angle-up\"></span>"/>
					    	</c:if>
                            <bean:write name="KualiForm" property="document.item[${ctr}].itemLineNumber"/>
                            <c:if test="${(fullEntryMode and !amendmentEntry) and !lockB2BEntry and ctr != itemCount - 1}">
                                <html:html-button
                                        property="methodToCall.downItem.line${ctr}"
                                        alt="Move Item Down"
                                        title="Move Item Down"
                                        styleClass="btn clean move"
                                        value="Move Down"
                                        style="position:absolute; bottom:0; right:1px;"
                                        innerHTML="<span class=\"fa fa-angle-down\"></span>"/>
                            </c:if>
                        </th>
						<td class="infoline">
							<kul:htmlControlAttribute
								attributeEntry="${itemAttributes.itemTypeCode}"
								property="document.item[${ctr}].itemTypeCode"
								extraReadOnlyProperty="document.item[${ctr}].itemType.itemTypeDescription"
								readOnly="${not ( (fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.versionNumber == null)) or itemLine.itemTypeCode == 'UNOR' or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 0}"/>
						</td>
						<td class="infoline">
							<kul:htmlControlAttribute
								attributeEntry="${itemAttributes.itemQuantity}"
								property="document.item[${ctr}].itemQuantity"
								readOnly="${not ((fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment))))}"
								tabindexOverride="${tabindexOverrideBase + 0}"/>
						</td>
                        <td class="infoline nowrap">
                        	<kul:htmlControlAttribute
								attributeEntry="${itemAttributes.itemUnitOfMeasureCode}"
                            	property="document.item[${ctr}].itemUnitOfMeasureCode"
                            	onblur="loadItemUnitOfMeasureInfo( 'document.item[${ctr}].itemUnitOfMeasureCode', 'document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription' );${onblur}"
                            	readOnly="${not ((fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment)))) or lockB2BEntry}"
                            	tabindexOverride="${tabindexOverrideBase + 0}"/>
                        	<c:if test="${!(not ((fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment)))) or lockB2BEntry)}">
                                <kul:lookup
                                        boClassName="org.kuali.kfs.sys.businessobject.UnitOfMeasure"
                                        fieldConversions="active:static.unitOfMeasureActive"
                                        lookupParameters="document.item[${ctr}].itemUnitOfMeasureCode:itemUnitOfMeasureCode,document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription.div:itemUnitOfMeasureDescription"
                                        fieldPropertyName="document.item[${ctr}].itemUnitOfMeasureCode"
                                        newLookup="true"
                                        addClass="embed"/>
                        	</c:if>
							<div id="document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription.div" class="fineprint">
								<html:hidden write="true" property="document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription"/>&nbsp;
							</div>
	                    </td>
						<td class="infoline" colspan="${colSpanCatlogNumber}" >
							<kul:htmlControlAttribute
								attributeEntry="${itemAttributes.itemCatalogNumber}"
								property="document.item[${ctr}].itemCatalogNumber"
								readOnly="${not ( (fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment)))) or lockB2BEntry}"
								tabindexOverride="${tabindexOverrideBase + 0}"/>
						</td>

					    <c:if test="${displayCommodityCodeFields}">
                            <td class="infoline nowrap">
            	                <kul:htmlControlAttribute
                	                	attributeEntry="${itemAttributes.purchasingCommodityCode}"
                    	            	property="document.item[${ctr}].purchasingCommodityCode"
                        	        	onblur="loadCommodityCodeDescription( 'document.item[${ctr}].purchasingCommodityCode', 'document.item[${ctr}].commodityCode.commodityDescription' );${onblur}"
                            	    	readOnly="${not ( (fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment))))}"
                                		tabindexOverride="${tabindexOverrideBase + 0}"/>
                            	<c:if test="${(fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment)))}">
                                	<kul:lookup boClassName="org.kuali.kfs.vnd.businessobject.CommodityCode"
                                    		fieldConversions="purchasingCommodityCode:document.item[${ctr}].purchasingCommodityCode"
                                            lookupParameters="'Y':active"
                                            addClass="embed"/>
                            	</c:if>
                            	<div id="document.item[${ctr}].commodityCode.commodityDescription.div" class="fineprint">
                                	<html:hidden write="true" property="document.item[${ctr}].commodityCode.commodityDescription"/>&nbsp;
                            	</div>
                        	</td>
                    	</c:if>

						<td class="infoline relative wrap-break-word" colspan="2">
							 <kul:htmlControlAttribute
									attributeEntry="${itemAttributes.itemDescription}"
						    		property="document.item[${ctr}].itemDescription"
						    		readOnly="${not ( (fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment)))) or lockB2BEntry}"
                                	tabindexOverride="${tabindexOverrideBase + 0}"
                                	styleClass="fullwidth"/>
						</td>
                    	<td class="infoline right">
					    	<kul:htmlControlAttribute
						    	    attributeEntry="${itemAttributes.itemUnitPrice}"
						        	property="document.item[${ctr}].itemUnitPrice"
						        	readOnly="${not ( (fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment)))) or lockB2BEntry}"
						        	tabindexOverride="${tabindexOverrideBase + 0}"/>
						</td>
						<td class="infoline right">
					    	<kul:htmlControlAttribute
						    	    attributeEntry="${itemAttributes.extendedPrice}"
						        	property="document.item[${ctr}].extendedPrice" readOnly="${true}"/>
						</td>
					
						<c:if test="${purapTaxEnabled}">
                        	<td class="infoline right">
					        	<kul:htmlControlAttribute
						        		attributeEntry="${itemAttributes.itemTaxAmount}"
						        		property="document.item[${ctr}].itemTaxAmount"
						        		readOnly="${(lockTaxAmountEntry or (not ( (fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment)))) or lockB2BEntry))}"
						        		tabindexOverride="${tabindexOverrideBase + 0}"/>
							</td>
                        	<td class="infoline right">
					        	<kul:htmlControlAttribute
						        		attributeEntry="${itemAttributes.totalAmount}"
						        		property="document.item[${ctr}].totalAmount" readOnly="${true}"/>
							</td>
						</c:if>
					
						<c:if test="${displayRequisitionFields and !lockB2BEntry}">
                        	<td class="infoline center">
						    	<kul:htmlControlAttribute
							    		attributeEntry="${itemAttributes.itemRestrictedIndicator}"
							    		property="document.item[${ctr}].itemRestrictedIndicator"
							    		readOnly="${not ( (fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment))))}"
							    		tabindexOverride="${tabindexOverrideBase + 0}"/>
							</td>
						</c:if>
						
						<c:if test="${!lockB2BEntry}">
                        	<td class="infoline center">
						    	<kul:htmlControlAttribute
							    		attributeEntry="${itemAttributes.itemAssignedToTradeInIndicator}"
							    		property="document.item[${ctr}].itemAssignedToTradeInIndicator"
							    		readOnly="${not ( (fullEntryMode and !amendmentEntry) or (amendmentEntry and itemLine.itemActiveIndicator and (not (amendmentEntryWithUnpaidPreqOrCM and itemLine.itemInvoicedTotalAmount != null and itemLine.itemInvoicedTotalAmount != 0.00 and !itemLine.newItemForAmendment))))}"
							    		tabindexOverride="${tabindexOverrideBase + 0}"/>
							</td>
						</c:if>

						<c:if test="${isATypeOfPODoc}">
							<td class="infoline right" rowspan="3">
								<kul:htmlControlAttribute
										attributeEntry="${itemAttributes.itemInvoicedTotalAmount}"
										property="document.item[${ctr}].itemInvoicedTotalAmount" readOnly="${true}"/>
							</td>
						</c:if>

						<td class="infoline" rowspan="${itemRowSpan}" colspan="${actionColSpan}">
							<div class="actions">
								<c:choose>
								<%-- ==== CU Customization: Updated first c:when condition to not show the "delete" button for not-new-item lines with invoiced totals of zero. ==== --%>
								<c:when test="${(fullEntryMode and !amendmentEntry) or (amendmentEntry and (itemLine.itemInvoicedTotalAmount == null or itemLine.newItemForAmendment))}">
									<html:html-button
											property="methodToCall.deleteItem.line${ctr}"
											alt="Delete Item ${ctr+1}"
											title="Delete Item ${ctr+1}"
											styleClass="btn clean"
											value="Delete"
											innerHTML="<span class=\"fa fa-trash\"></span>"/>
								</c:when>
								<c:when test="${amendmentEntry and itemLine.canInactivateItem and itemLine.itemInvoicedTotalAmount != null}">
									<html:submit
											property="methodToCall.inactivateItem.line${ctr}"
											alt="Inactivate Item ${ctr+1}"
											title="Inactivate Item ${ctr+1}"
											styleClass="btn btn-default"
											value="Inactivate"/>
								</c:when>
								<c:when test="${isATypeOfPODoc and !itemLine.itemActiveIndicator}">
									Inactive
								</c:when>
								<c:otherwise>
									&nbsp;
								</c:otherwise>
								</c:choose>
								<c:set var="toggleTabIndex" value="${KualiForm.currentTabIndex}"/>
								<purap:accountingLinesToggle currentTabIndex="${toggleTabIndex}" accountPrefix="document.item[${ctr}]."/>
							</div>
						</td>
					</tr>

					<!-- KFSPTS-2257 -->
					<c:set var="eshopFlagColumnCount" value="${mainColumnCount - colSpanAction - 2}"/>
					<c:if test="${isATypeOfPODoc}">
						<c:set var="eshopFlagColumnCount" value="${eshopFlagColumnCount - 1}"/>
					</c:if>

					<tr>
					   <th align="right">
						  e-Shop Flags
					   </th>
					   <td class="infoline" colspan="${eshopFlagColumnCount}">
						  <kul:htmlControlAttribute
									attributeEntry="${itemAttributes.totalAmount}"
									property="document.item[${ctr}].eshopFlags" readOnly="true"/>
					   </td>
					</tr>
					<c:set var="accountColumnCount" value="${mainColumnCount - colSpanAction - 1}"/>
					<c:if test="${isATypeOfPODoc}">
						<c:set var="accountColumnCount" value="${accountColumnCount - 1}"/>
					</c:if>

					<c:set var="rowStyle" value="border-bottom:1px solid #BBBBBB;"/>
					<c:choose>
                		<c:when test="${amendmentEntry}">
                    		<c:choose>
								<c:when test="${itemLine.itemActiveIndicator and (!amendmentEntryWithUnpaidPreqOrCM or itemLine.itemInvoicedTotalAmount == null or itemLine.newItemForAmendment)}">
									<c:set target="${KualiForm.accountingLineEditingMode}" property="fullEntry" value="true" />
									<purap:purapGeneralAccounting
											accountPrefix="document.item[${ctr}]."
											itemColSpan="${accountColumnCount}"
											rowStyle="${rowStyle}"
											currentTabIndex="${toggleTabIndex}"
											showToggle="false"/>
								</c:when>
								<c:otherwise>
									<c:set target="${KualiForm.editingMode}" property="viewOnly" value="true" />
									<purap:purapGeneralAccounting
											accountPrefix="document.item[${ctr}]."
											itemColSpan="${accountColumnCount}"
											rowStyle="${rowStyle}"
											currentTabIndex="${toggleTabIndex}"
											showToggle="false"/>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:when test="${unorderedItemAccountEntry and itemLine.newUnorderedItem}">
							<c:set target="${KualiForm.accountingLineEditingMode}" property="fullEntry" value="true" />
							<purap:purapGeneralAccounting
									accountPrefix="document.item[${ctr}]."
									itemColSpan="${accountColumnCount}"
									rowStyle="${rowStyle}"
									currentTabIndex="${toggleTabIndex}"
									showToggle="false"/>
						</c:when>
						<c:when test="${(!amendmentEntry)}">
							<c:if test="${!empty KualiForm.editingMode['allowItemEntry'] && (KualiForm.editingMode['allowItemEntry'] == itemLine.itemIdentifier)}" >
								<c:set target="${KualiForm.editingMode}" property="expenseEntry" value="true" />
							</c:if>
							<purap:purapGeneralAccounting
									accountPrefix="document.item[${ctr}]."
									itemColSpan="${accountColumnCount}"
									rowStyle="${rowStyle}"
									currentTabIndex="${toggleTabIndex}"
									showToggle="false"/>
						</c:when>
					</c:choose>
				
					<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
						</tbody>
					</c:if>
				</c:if>
			</logic:iterate>
        </table>
    </div>

	<c:if test="${!lockB2BEntry && fn:length(KualiForm.document.belowTheLineTypes) > 0}">
		<purap:miscitems
				itemAttributes="${itemAttributes}"
				accountingLineAttributes="${accountingLineAttributes}" 
    	        descriptionFirst="${KualiForm.document.isATypeOfPurDoc}"/>
	</c:if>
		
    <div class="tab-container">
        <h3>Totals</h3>
        <table class="standard" summary="Items Section">
			<c:set var="colSpanTotalLabel" value="${colSpanItemType+colSpanDescription}"/>
			<c:set var="colSpanTotalAmount" value="${colSpanExtendedPrice}"/>
			<c:set var="colSpanTotalBlank" value="${mainColumnCount-colSpanTotalLabel-colSpanTotalAmount}"/>

			<c:if test="${purapTaxEnabled}">
				<c:set var="colSpanTotalBlank" value="${colSpanTotalBlank-2}"/>
				<c:set var="colSpanTotalAmount" value="1"/>
				<c:set var="colSpanTotalLabel" value="${mainColumnCount-colSpanTotalBlank-colSpanTotalAmount}"/>
		
				<tr>
                    <th class="right" width='62%' colspan="${colSpanTotalLabel}" scope="row">
				        <kul:htmlAttributeLabel attributeEntry="${DataDictionary.RequisitionDocument.attributes.totalPreTaxDollarAmount}" />
					</th>
                    <td valign=middle class="datacell right heavy" colspan="${colSpanTotalAmount}" width="150px">
                    	<kul:htmlControlAttribute
								attributeEntry="${DataDictionary.RequisitionDocument.attributes.totalPreTaxDollarAmount}"
                        		property="document.totalPreTaxDollarAmount"
                                readOnly="true" />
					</td>
					<td class="datacell" colspan="${colSpanTotalBlank}">&nbsp;</td>
				</tr>

				<tr>
                    <th class="right" colspan="${colSpanTotalLabel}" scope="row">
			        	<kul:htmlAttributeLabel attributeEntry="${DataDictionary.RequisitionDocument.attributes.totalTaxAmount}" />
					</th>
                    <td valign=middle class="datacell right heavy" colspan="${colSpanTotalAmount}">
                    	<kul:htmlControlAttribute
								attributeEntry="${DataDictionary.RequisitionDocument.attributes.totalTaxAmount}"
                        		property="document.totalTaxAmount"
                                readOnly="true" />
					</td>
					<td class="datacell" colspan="${colSpanTotalBlank}">&nbsp;</td>
				</tr>
			</c:if>

			<tr>
                <th class="right" colspan="${colSpanTotalLabel}" scope="row">
			        <kul:htmlAttributeLabel attributeEntry="${DataDictionary.RequisitionDocument.attributes.totalDollarAmount}" />
				</th>
                <td valign=middle class="datacell right heavy"  colspan="${colSpanTotalAmount}">
                    <kul:htmlControlAttribute
							attributeEntry="${DataDictionary.RequisitionDocument.attributes.totalDollarAmount}"
                        	property="document.totalDollarAmount"
                            readOnly="true" />
				</td>
				<td class="datacell" colspan="${colSpanTotalBlank}">&nbsp;</td>
			</tr>

			<tr>
                <th class="right" colspan="${colSpanTotalLabel}" scope="row">
			    	<c:if test="${displayRequisitionFields}">
				        <kul:htmlAttributeLabel attributeEntry="${DataDictionary.RequisitionDocument.attributes.organizationAutomaticPurchaseOrderLimit}" />
			    	</c:if>
			    	<c:if test="${!displayRequisitionFields}">
                        <kul:htmlAttributeLabel attributeEntry="${DataDictionary.PurchaseOrderDocument.attributes.internalPurchasingLimit}" />
                	</c:if>
            	</th>
				<td valign=middle class="datacell right" colspan="${colSpanTotalAmount}">
			    	<c:if test="${displayRequisitionFields}">
				        <kul:htmlControlAttribute
					        attributeEntry="${DataDictionary.RequisitionDocument.attributes.organizationAutomaticPurchaseOrderLimit}"
					        property="document.organizationAutomaticPurchaseOrderLimit"
                            readOnly="true" />
			    	</c:if>
			    	<c:if test="${!displayRequisitionFields}">
                        <kul:htmlControlAttribute
                            attributeEntry="${DataDictionary.PurchaseOrderDocument.attributes.internalPurchasingLimit}"
                            property="document.internalPurchasingLimit"
                            readOnly="true" />
			    	</c:if>
				</td>
				<td class="datacell" colspan="${colSpanTotalBlank}">&nbsp;</td>
			</tr>
		</table>
	</div>
</kul:tab>
