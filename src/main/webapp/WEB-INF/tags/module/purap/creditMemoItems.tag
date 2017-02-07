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
<%@ tag import="org.kuali.kfs.sys.util.Guid" %>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="itemAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="accountingLineAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="mainColumnCount" required="true" %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="lockTaxAmountEntry" value="${(not empty KualiForm.editingMode['lockTaxAmountEntry'])}" />
<c:set var="clearAllTaxes" value="${(not empty KualiForm.editingMode['clearAllTaxes'])}" />
<c:set var="purapTaxEnabled" value="${(not empty KualiForm.editingMode['purapTaxEnabled'])}" />
<c:set var="editAmount" value="${(not empty KualiForm.editingMode['editAmount'])}" /> <!-- KFSPTS-1891, KFSPTS-2851 -->

<c:set var="colSpanDescription" value="4"/>
<c:if test="${purapTaxEnabled}">
	<c:set var="colSpanDescription" value="2"/>
</c:if>

<c:set var="usePO" value="true" />
<c:if test="${KualiForm.document.creditMemoType eq 'PREQ'}" >
  	<c:set var="usePO" value="false" />
</c:if>
<c:set var="tabindexOverrideBase" value="50" />

<c:if test="${KualiForm.countOfAboveTheLine>=1}">
    <tr class="header">
        <th></th>
		
		<c:if test="${usePO}" >
            <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.poInvoicedTotalQuantity}" addClass="right"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.poUnitPrice}" addClass="right"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.poTotalAmount}" addClass="right"/>
	    </c:if>
	    
		<c:if test="${!usePO}" >
            <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.preqInvoicedTotalQuantity}" addClass="right"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.preqUnitPrice}" addClass="right"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.preqTotalAmount}" addClass="right"/>
	    </c:if>
	    	
        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemQuantity}" addClass="right"/>
        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitPrice}" addClass="right"/>
        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.extendedPrice}" addClass="right"/>

		<c:if test="${purapTaxEnabled}">
            <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTaxAmount}" addClass="right"/>
            <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.totalAmount}" addClass="right"/>
		</c:if>

        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemCatalogNumber}"/>
        <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}" colspan="${colSpanDescription}"/>
        <kul:htmlAttributeHeaderCell literalLabel="Actions"/>
	</tr>
</c:if>

<c:if test="${KualiForm.countOfAboveTheLine<1}">
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
                <c:set var="newObjectId" value="<%= (new Guid()).toString()%>" />
                <c:set var="tabKey" value="Item-${newObjectId}" />
            </c:when>
            <c:when test="${itemLine.objectId != null}">
                <c:set var="tabKey" value="Item-${itemLine.objectId}" />
            </c:when>
        </c:choose>
        
        <%-- hit form method to increment tab index --%>
        <c:set var="dummyIncrementer" value="${kfunc:incrementTabIndex(KualiForm, tabKey)}" />
        <c:set var="currentTab" value="${kfunc:getTabState(KualiForm, tabKey)}"/>

		<c:choose>
		<c:when test="${empty currentTab}">
			<c:set var="isOpen" value="false" />
		</c:when>
		<c:when test="${!empty currentTab}">
			<c:set var="isOpen" value="${(isOpen ? 'OPEN' : 'CLOSE')}" />
		</c:when>
		</c:choose>

        <tr class="line top">
            <th class="infoline" rowspan="2">
                <bean:write name="KualiForm" property="document.item[${ctr}].itemLineNumber"/>
            </th>
			
	    	<c:if test="${usePO}" >
                <td class="infoline right">
		    	<c:if test="${itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
			       <kul:htmlControlAttribute
				    	attributeEntry="${itemAttributes.poInvoicedTotalQuantity}"
				    	property="document.item[${ctr}].poInvoicedTotalQuantity"
				    	readOnly="true" styleClass="infoline" />
				</c:if>  
		    	<c:if test="${!itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
		    		&nbsp;
		    	</c:if>  				  
		    	</td>
                <td class="infoline right">
		    	    <kul:htmlControlAttribute
				    	attributeEntry="${itemAttributes.poUnitPrice}"
				    	property="document.item[${ctr}].poUnitPrice"
				    	readOnly="true" styleClass="infoline" />
	    	    </td>
                <td class="infoline right">
		     	    <kul:htmlControlAttribute
				    	attributeEntry="${itemAttributes.poTotalAmount}"
				    	property="document.item[${ctr}].poTotalAmount"
				    	readOnly="true" styleClass="infoline" />
	    	    </td>		
	    	</c:if>
	    	
	    	<c:if test="${!usePO}" >
                <td class="infoline right">
		    	   	<c:if test="${itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
			         	<kul:htmlControlAttribute
				     		attributeEntry="${itemAttributes.preqInvoicedTotalQuantity}"
				      		property="document.item[${ctr}].preqInvoicedTotalQuantity"
				    		readOnly="true" styleClass="infoline" />
				   	</c:if>
		    	   	<c:if test="${!itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
		    	   		&nbsp;
		    	   	</c:if>
		    	</td>
                <td class="infoline right">
		    	    <kul:htmlControlAttribute
				    	attributeEntry="${itemAttributes.preqUnitPrice}"
				    	property="document.item[${ctr}].preqUnitPrice"
				    	readOnly="true" styleClass="infoline" />
	    	    </td>
                <td class="infoline right">
		     	    <kul:htmlControlAttribute
				    	attributeEntry="${itemAttributes.preqTotalAmount}"
				    	property="document.item[${ctr}].preqTotalAmount"
				    	readOnly="true" styleClass="infoline" />
	    	    </td>		
	    	</c:if>
	    	
            <td class="infoline right">
				<c:if test="${itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
					<kul:htmlControlAttribute
							attributeEntry="${itemAttributes.itemQuantity}"
				          	property="document.item[${ctr}].itemQuantity"
				          	readOnly="${not (fullEntryMode) and not (editAmount)}" styleClass="amount"
				          	tabindexOverride="${tabindexOverrideBase + 0}"/>
				</c:if>
		    	<c:if test="${!itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
		    		&nbsp;
		    	</c:if>
			</td>
            <td class="infoline right">
				<c:if test="${itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
	            	<kul:htmlControlAttribute
	                        attributeEntry="${itemAttributes.itemUnitPrice}"
	                        property="document.item[${ctr}].itemUnitPrice"
	                        readOnly="${not (fullEntryMode) and not (editAmount)}" styleClass="amount" 
	                        tabindexOverride="${tabindexOverrideBase + 0}"/>
				</c:if>
                <c:if test="${!itemLine.itemType.quantityBasedGeneralLedgerIndicator}" >
                	&nbsp;
				</c:if>
			</td>
            <td class="infoline right">
				<kul:htmlControlAttribute
						attributeEntry="${itemAttributes.extendedPrice}"
				        property="document.item[${ctr}].extendedPrice"
				        readOnly="${not fullEntryMode and not (editAmount)}" styleClass="amount" 
				        tabindexOverride="${tabindexOverrideBase + 0}"/>
			</td>

			<c:if test="${purapTaxEnabled}">
                <td class="infoline right">
			        <kul:htmlControlAttribute
				        attributeEntry="${itemAttributes.itemTaxAmount}"
				        property="document.item[${ctr}].itemTaxAmount"
				        readOnly="${not fullEntryMode or lockTaxAmountEntry}" styleClass="amount" 
				        tabindexOverride="${tabindexOverrideBase + 0}"/>
				</td>
                <td class="infoline right">
			        <kul:htmlControlAttribute
				        attributeEntry="${itemAttributes.totalAmount}"
				        property="document.item[${ctr}].totalAmount"
				        readOnly="true" styleClass="amount"/>
				</td>
			</c:if>

			<td class="infoline">
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.itemCatalogNumber}"
				    property="document.item[${ctr}].itemCatalogNumber"
				    readOnly="true" />
		    </td>		    
            <td class="infoline" colspan="${colSpanDescription}">
			    <kul:htmlControlAttribute
				    attributeEntry="${itemAttributes.itemDescription}"
				    property="document.item[${ctr}].itemDescription"
				    readOnly="true" />
			</td>			
            <td class="infoline">
                <div class="actions">
                    <c:set var="toggleTabIndex" value="${KualiForm.currentTabIndex}"/>
                    <purap:accountingLinesToggle currentTabIndex="${toggleTabIndex}" accountPrefix="document.item[${ctr}]."/>
                </div>
            </td>
		</tr>

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
			 </div>
	 	</th>
	 </tr>
</c:if>
