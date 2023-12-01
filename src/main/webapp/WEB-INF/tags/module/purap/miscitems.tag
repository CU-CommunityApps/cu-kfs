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
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="overrideTitle" required="false"
	description="The title to be used for this section." %>
<%@ attribute name="documentAttributes" required="false" type="java.util.Map" 
	description="The DataDictionary entry containing attributes for this row's fields." %>
<%@ attribute name="itemAttributes" required="true" type="java.util.Map"
	description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="accountingLineAttributes" required="true"
	type="java.util.Map"
	description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="showAmount" required="false"
    type="java.lang.Boolean"
    description="show the amount if true else percent" %>
<%@ attribute name="showInvoiced" required="false"
    type="java.lang.Boolean"
    description="post the unitPrice into the extendedPrice field" %>
<%@ attribute name="specialItemTotalType" required="false" %>
<%@ attribute name="specialItemTotalOverride" required="false" fragment="true"
    description="Fragment of code to specify special item total line" %>
<%@ attribute name="descriptionFirst" required="false" type="java.lang.Boolean"
    description="Whether or not to show item description before extended price." %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])}" />
<c:set var="lockTaxAmountEntry" value="${(not empty KualiForm.editingMode['lockTaxAmountEntry']) || !fullEntryMode}" />
<c:set var="tabindexOverrideBase" value="50" />
<c:set var="isATypeOfPODoc" value="${KualiForm.document.isATypeOfPODoc}" />

<c:if test="${empty overrideTitle}">
	<c:set var="overrideTitle" value="Additional Charges"/>
</c:if>

<c:set var="amendmentEntry"
	value="${(!empty KualiForm.editingMode['amendmentEntry'])}" />
	
<c:set var="amountEditable"
	value="${(!empty KualiForm.editingMode['addtnlChargeAmountEditable'])}" />

<c:set var="documentType" value="${KualiForm.document.documentHeader.workflowDocument.documentTypeName}" />
<c:set var="isATypeOfPODoc" value="${KualiForm.document.isATypeOfPODoc}" />
  
<c:set var="currentTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
<c:set var="topLevelTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
<c:set var="tabKey" value="${kfunc:generateTabKey(overrideTitle)}" />
<c:set var="dummyIncrementer" value="${kfunc:incrementTabIndex(KualiForm, tabKey)}" />
<c:set var="currentTab" value="${kfunc:getTabState(KualiForm, tabKey)}" />
<c:set var="purapTaxEnabled" value="${(not empty KualiForm.editingMode['purapTaxEnabled'])}" />

<c:choose>
	<c:when test="${empty currentTab}">
		<c:set var="isOpen" value="true" />
	</c:when>
	<c:when test="${!empty currentTab}">
		<c:set var="isOpen" value="${currentTab == 'OPEN'}" />
	</c:when>
</c:choose>
	
<html:hidden property="tabStates(${tabKey})" value="${(isOpen ? 'OPEN' : 'CLOSE')}" />

<div class="tab-container">
    <table class="standard side-margins acct-lines" summary="Items Section">
        <tr class="title">
            <th></th>
            <td colspan="3">
                <h2>
                    <c:out value="${overrideTitle}" />&nbsp;&nbsp;
					<c:if test="${isOpen == 'true' || isOpen == 'TRUE'}">
                        <html:submit
                                property="methodToCall.toggleTab.tab${tabKey}"
                                alt="hide"
                                title="toggle"
                                styleClass="btn btn-default"
                                styleId="tab-${tabKey}-imageToggle"
                                onclick="javascript: return true; "
                                value="Hide"/>
					</c:if>
					<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
                        <html:submit
                                property="methodToCall.toggleTab.tab${tabKey}"
                                alt="show"
                                title="toggle"
                                styleClass="btn btn-default"
                                styleId="tab-${tabKey}-imageToggle"
                                onclick="javascript: return true; "
                                value="Show"/>
					</c:if>
                </h2>
			</td>
		</tr>
		<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
			<tbody style="display: none;" id="tab-${tabKey}-div">
		</c:if>

        <c:set var="accountingLineWidth" value="5"/>

        <tr class="header">
            <th></th>
            <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTypeCode}" colspan="2"/>
	
			<c:if test="${showInvoiced}">
				<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.originalAmountfromPO}" addClass="right" />
				<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.poOutstandingAmount}" addClass="right" />
				<c:set var="accountingLineWidth" value="${accountingLineWidth + 2}"/>
			</c:if>

			<c:choose>
				<c:when test="${descriptionFirst}">
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.extendedPrice}" addClass="right" />
                    <c:set var="accountingLineWidth" value="${accountingLineWidth + 2}"/>
					<c:if test="${purapTaxEnabled}">
                        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTaxAmount}" addClass="right" />
                        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.totalAmount}" addClass="right" />
                        <c:set var="accountingLineWidth" value="${accountingLineWidth + 2}"/>
					</c:if>
					<c:if test="${isATypeOfPODoc}">
                        <kul:htmlAttributeHeaderCell literalLabel="Amount Paid" addClass="right"/>
                        <c:set var="accountingLineWidth" value="${accountingLineWidth + 1}"/>
            		</c:if>
				</c:when>
	    		<c:otherwise>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.extendedPrice}" />
					<c:if test="${purapTaxEnabled}">
                        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTaxAmount}" addClass="right" />
                        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.totalAmount}" addClass="right" />
                        <c:set var="accountingLineWidth" value="${accountingLineWidth + 2}"/>
					</c:if>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}" />
                    <c:set var="accountingLineWidth" value="${accountingLineWidth + 2}"/>
				</c:otherwise>
			</c:choose>
            <td></td>
            <kul:htmlAttributeHeaderCell literalLabel="Actions"/>
		</tr>

		<logic:iterate indexId="ctr" name="KualiForm" property="document.items" id="itemLine">
			<c:if test="${itemLine.itemType.additionalChargeIndicator && !itemLine.itemType.isTaxCharge}">
				<c:if test="${not empty specialItemTotalType and itemLine.itemTypeCode == specialItemTotalType }">
					  <c:if test="${!empty specialItemTotalOverride}">
						<jsp:invoke fragment="specialItemTotalOverride"/>
					  </c:if>
				</c:if>
		
                <tr class="top line">
                    <td></td>
                    <td class="infoline heavy" colspan="2">
                        <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemTypeCode}" property="document.item[${ctr}].itemType.itemTypeDescription" readOnly="${true}" />
					</td>
			
					<c:if test="${showInvoiced}">
                        <td class="infoline right">
							<kul:htmlControlAttribute
								attributeEntry="${itemAttributes.purchaseOrderItemUnitPrice}"
								property="document.item[${ctr}].purchaseOrderItemUnitPrice"
								readOnly="true" />
						</td>
                        <td class="infoline right">
							<kul:htmlControlAttribute
								attributeEntry="${itemAttributes.poOutstandingAmount}"
								property="document.item[${ctr}].poOutstandingAmount"
								readOnly="true" />
						</td>
					</c:if>
			
					<c:choose>
						<c:when test="${descriptionFirst}">
							<td class="infoline">
								<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemDescription}" property="document.item[${ctr}].itemDescription" readOnly="${not (fullEntryMode or amendmentEntry)}" tabindexOverride="${tabindexOverrideBase + 0}" styleClass="fullwidth"/>
							</td>
							<td class="infoline right">
								<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemUnitPrice}" property="document.item[${ctr}].itemUnitPrice" readOnly="${not (fullEntryMode or amendmentEntry)}" styleClass="amount" tabindexOverride="${tabindexOverrideBase + 0}"/>
							</td>

							<c:if test="${purapTaxEnabled and itemLine.itemType.taxableIndicator}">
								<td class="infoline right">
									<kul:htmlControlAttribute
										attributeEntry="${itemAttributes.itemTaxAmount}"
										property="document.item[${ctr}].itemTaxAmount" readOnly="${lockTaxAmountEntry}"
										tabindexOverride="${tabindexOverrideBase + 0}"/>
								</td>
								<td class="infoline right">
									<kul:htmlControlAttribute
										attributeEntry="${itemAttributes.totalAmount}"
										property="document.item[${ctr}].totalAmount" readOnly="true"
										tabindexOverride="${tabindexOverrideBase + 0}"/>
								</td>
							</c:if>

							<c:if test="${isATypeOfPODoc}">
								<td class="infoline right">
									<kul:htmlControlAttribute
										attributeEntry="${itemAttributes.itemInvoicedTotalAmount}"
										property="document.item[${ctr}].itemInvoicedTotalAmount" readOnly="${true}"/>
								</td>
							</c:if>
						</c:when>
			    		<c:otherwise>
                            <td class="infoline">
                                <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemUnitPrice}" property="document.item[${ctr}].itemUnitPrice" readOnly="${not (fullEntryMode or amendmentEntry or amountEditable)}" styleClass="amount" tabindexOverride="${tabindexOverrideBase + 0}"/>
							</td>

							<c:if test="${purapTaxEnabled}">
								<c:choose>
									<c:when test="${itemLine.itemType.taxableIndicator}">
										<td class="infoline">
											<kul:htmlControlAttribute
												attributeEntry="${itemAttributes.itemTaxAmount}"
												property="document.item[${ctr}].itemTaxAmount" readOnly="${lockTaxAmountEntry}"
												tabindexOverride="${tabindexOverrideBase + 0}"/>
										</td>
										<td class="infoline right">
											<kul:htmlControlAttribute
												attributeEntry="${itemAttributes.totalAmount}"
												property="document.item[${ctr}].totalAmount" readOnly="true"
												tabindexOverride="${tabindexOverrideBase + 0}"/>
										</td>
									</c:when>
									<c:otherwise>
										<td class="infoline">
											&nbsp;
										</td>
										<td class="infoline">
											&nbsp;
										</td>
									</c:otherwise>
								</c:choose>
							</c:if>
					
                            <td class="infoline">
								<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemDescription}" property="document.item[${ctr}].itemDescription" readOnly="${not (fullEntryMode or amendmentEntry)}" tabindexOverride="${tabindexOverrideBase + 0}"/>
							</td>
						</c:otherwise>
					</c:choose>

                    <td></td>
                    <td class="infoline">
                        <div class="actions">
                            <c:set var="toggleTabIndex" value="${KualiForm.currentTabIndex}"/>
                            <purap:accountingLinesToggle currentTabIndex="${toggleTabIndex}" accountPrefix="document.item[${ctr}]."/>
                        </div>
                    </td>
				</tr>

				<c:if test="${amendmentEntry}">
					<purap:purapGeneralAccounting
							accountPrefix="document.item[${ctr}]."
							itemColSpan="${accountingLineWidth}"
							currentTabIndex="${toggleTabIndex}"
							showToggle="false"/>
				</c:if>
				
				<c:if test="${!empty KualiForm.editingMode['allowItemEntry'] && !empty itemLine.itemUnitPrice || empty KualiForm.editingMode['allowItemEntry']}">
		    		<c:if test="${!amendmentEntry && KualiForm.document.applicationDocumentStatus!='Awaiting Fiscal Officer Approval' || KualiForm.document.applicationDocumentStatus =='Awaiting Fiscal Officer Approval' && !empty KualiForm.document.items[ctr].itemUnitPrice}">
			    		<purap:purapGeneralAccounting
				    			accountPrefix="document.item[${ctr}]."
                                itemColSpan="${accountingLineWidth}"
                                currentTabIndex="${toggleTabIndex}"
                                showToggle="false"/>
		    		</c:if>
				</c:if>
			</c:if>
		</logic:iterate>

        <tr class="line"><td></td></tr>

		<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
			</tbody>
		</c:if>
    </table>
</div>
