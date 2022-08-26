<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2021 Kuali, Inc.

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

<%@ attribute name="itemAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="accountingLineAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="showAmount" required="false"
    type="java.lang.Boolean"
    description="show the amount if true else percent" %>
<%@ attribute name="mainColumnCount" required="true" %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])}" />
<c:set var="lockTaxAmountEntry" value="${(not empty KualiForm.editingMode['lockTaxAmountEntry'])}" />
<c:set var="clearAllTaxes" value="${(not empty KualiForm.editingMode['clearAllTaxes'])}" />
<c:set var="purapTaxEnabled" value="${(not empty KualiForm.editingMode['purapTaxEnabled'])}" />
<c:set var="tabindexOverrideBase" value="50" />
<c:set var="fullDocEntryCompleted" value="${(not empty KualiForm.editingMode['fullDocumentEntryCompleted'])}" />
<c:set var="editAmount" value="${(not empty KualiForm.editingMode['editAmount'])}" /> <!-- KFSPTS-1891 -->

<c:set var="colSpanDescription" value="3"/>
<c:if test="${purapTaxEnabled}">
	<c:set var="colSpanDescription" value="1"/>
</c:if>

<%-- temporary workaround due to removing discount item --%>
<c:if test="${KualiForm.countOfAboveTheLine>=1}">
	<tr class="header">
		<th></th>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.poOutstandingQuantity}"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitOfMeasureCode}"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.purchaseOrderItemUnitPrice}" addClass="right"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemQuantity}" addClass="right"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitPrice}" addClass="right"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.extendedPrice}" addClass="right"/>
		
		<c:if test="${purapTaxEnabled}">
			<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTaxAmount}" addClass="right"/>
			<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.totalAmount}" addClass="right"/>
		</c:if>

		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemCatalogNumber}"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemAssignedToTradeInIndicator}" addClass="center"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}" colspan="${colSpanDescription}"/>
		<kul:htmlAttributeHeaderCell literalLabel="Actions"/>
	</tr>
</c:if>

<c:if test="${KualiForm.countOfAboveTheLine<1}">
	<tr>
		<th height=30 colspan="${mainColumnCount}">No items Payable</th>
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
			<c:set var="isOpen" value="false" />
		</c:when>
		<c:when test="${!empty currentTab}">
			<c:set var="isOpen" value="${currentTab == 'OPEN'}" />
		</c:when>
		</c:choose>

		<tr class="top line">
			<td class="infoline" nowrap="nowrap" rowspan="2" style="position: relative;"> <!-- KFSPTS-1719 -->
				<bean:write name="KualiForm" property="document.item[${ctr}].extension.lineNumber"/>
			</td>
			<td class="infoline">
				<c:choose>
				<c:when test="${KualiForm.document.items[ctr].itemType.quantityBasedGeneralLedgerIndicator}">
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.poOutstandingQuantity}"
				    property="document.item[${ctr}].poOutstandingQuantity"
				    readOnly="true" />
				</c:when>
				<c:otherwise>
					&nbsp;
				</c:otherwise>
				</c:choose>
			</td>
			<td class="infoline">
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.itemUnitOfMeasureCode}"
				    property="document.item[${ctr}].itemUnitOfMeasureCode"
				    readOnly="true" />
		    </td>
			<td class="infoline right">
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.purchaseOrderItemUnitPrice}"
				    property="document.item[${ctr}].purchaseOrderItemUnitPrice"
				    readOnly="true" />
		    </td>				    
			<td class="infoline right">
				<kul:htmlControlAttribute
						attributeEntry="${itemAttributes.itemQuantity}"
				        property="document.item[${ctr}].itemQuantity"
				        readOnly="${ (not (fullEntryMode) or (fullDocEntryCompleted)) or (KualiForm.document.items[ctr].itemType.amountBasedGeneralLedgerIndicator) }" 
				        tabindexOverride="${tabindexOverrideBase + 0}" />
			</td>
			<td class="infoline right">
				<c:if test="${KualiForm.document.items[ctr].itemType.quantityBasedGeneralLedgerIndicator}">
					<!--  KFSPTS-1891 : added 'editAmount' check -->
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemUnitPrice}"
                            property="document.item[${ctr}].itemUnitPrice"
                            readOnly="${(not (fullEntryMode) or (fullDocEntryCompleted))  and not (editAmount)}" 
                            tabindexOverride="${tabindexOverrideBase + 0}" />
				</c:if>
				<c:if test="${KualiForm.document.items[ctr].itemType.amountBasedGeneralLedgerIndicator}">
                    <!-- KFSPTS-1719 -->
					<kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemUnitPrice}"
                            property="document.item[${ctr}].extension.poOutstandingAmountForDisplay" 
                            readOnly="true" />
				</c:if>
			</td>
			<td class="infoline right">
				<!-- KFSPTS-1891 : added 'editAmount' check -->
				<kul:htmlControlAttribute
				        attributeEntry="${itemAttributes.extendedPrice}"
				        property="document.item[${ctr}].extendedPrice" 
				        readOnly="${(not (fullEntryMode) or (fullDocEntryCompleted)) and not (editAmount)}"
				        tabindexOverride="${tabindexOverrideBase + 0}" />
			</td>

			<c:if test="${purapTaxEnabled}">
				<td class="infoline right">
			        <kul:htmlControlAttribute
				        attributeEntry="${itemAttributes.itemTaxAmount}"
				        property="document.item[${ctr}].itemTaxAmount" 
				        readOnly="${not (fullEntryMode) or lockTaxAmountEntry}" 
				        tabindexOverride="${tabindexOverrideBase + 0}" />
			</td>			
				<td class="infoline right">
			        <kul:htmlControlAttribute
				        attributeEntry="${itemAttributes.totalAmount}"
				        property="document.item[${ctr}].totalAmount" 
				        readOnly="true" />
			</td>
			</c:if>

			<td class="infoline">
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.itemCatalogNumber}"
				    property="document.item[${ctr}].itemCatalogNumber"
				    readOnly="true" />
		    </td>
			<td class="infoline center">
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.itemAssignedToTradeInIndicator}"
				    property="document.item[${ctr}].itemAssignedToTradeInIndicator"
				    readOnly="true" />
			</td>			    
			<td  class="infoline" colspan="${colSpanDescription}">
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.itemDescription}"
				    property="document.item[${ctr}].itemDescription"
				    readOnly="true" />
			</td>	
			
			<td class="infoline">
				<div class="actions">
					<c:if test="${fullEntryMode}">
						<html:html-button
								property="methodToCall.recalculateItemAccountsAmounts.line${ctr}.Anchor"
								alt="Recalculate Item's accounts amounts distributions"
								title="Recalculate Item's accounts amounts distributions"
								styleClass="btn clean"
								value="Calculate"
								innerHTML="<img src='${ConfigProperties.externalizable.images.url}calculator.png' height='18px'/>"/>
						<html:html-button
								property="methodToCall.restoreItemAccountsAmounts.line${ctr}.Anchor"
								alt="Restore Item's accounts percents/amounts from Purchase Order"
								title="Restore Item's accounts percents/amounts from Purchase Order"
								styleClass="btn clean"
								value="Restore"
								innerHTML="<span class=\"fa fa-undo\"></span>"/>
					</c:if>
					<c:set var="toggleTabIndex" value="${KualiForm.currentTabIndex}"/>
					<purap:accountingLinesToggle currentTabIndex="${toggleTabIndex}" accountPrefix="document.item[${ctr}]."/>
				</div>
			</td>
		</tr>
		
		<c:set var="hideFields" value="amount" />
		<c:if test="${showAmount}">
			<c:set var="hideFields" value="" />
		</c:if>		

		<c:set var="accountColumnCount" value="${mainColumnCount - 1}"/>
		<c:set var="rowStyle" value="border-bottom:1px solid #BBBBBB;"/>
		<purap:purapGeneralAccounting
				accountPrefix="document.item[${ctr}]."
				itemColSpan="${mainColumnCount-1}"
				rowStyle="${rowStyle}"
				currentTabIndex="${toggleTabIndex}"
				showToggle="false"/>
		<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
			</tbody>
		</c:if>
	</c:if>
</logic:iterate>

<c:if test="${(fullEntryMode) and (clearAllTaxes) and (purapTaxEnabled)}">
	<tr>
		<th></th>
		<th height=30 colspan="${mainColumnCount - 1}">
            <html:submit
			    	property="methodToCall.clearAllTaxes"
			    	alt="Clear all tax"
                    title="Clear all tax"
                    styleClass="btn btn-default"
                    value="Clear All Tax"/>
	 	</th>
	 </tr>
</c:if>	
