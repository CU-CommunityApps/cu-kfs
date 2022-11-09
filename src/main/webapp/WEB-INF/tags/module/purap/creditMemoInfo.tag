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

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>
              
<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="fullDocumentEntryCompleted" value="${not empty KualiForm.editingMode['fullDocumentEntryCompleted']}" />
<c:set var="purchaseOrderAttributes" value="${DataDictionary.PurchaseOrderDocument.attributes}" />

<kul:tab tabTitle="Credit Memo Info" defaultOpen="true" tabErrorKey="document.bankCode,document.paymentMethodCode">
   
    <div class="tab-container" align=center>
        <table class="standard" summary="Credit Memo Info Section">

            <tr>
                <th class="right" width="25%">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.creditMemoNumber}" />
                </th>
                <td class="datacell" width="25%">
                   <kul:htmlControlAttribute attributeEntry="${documentAttributes.creditMemoNumber}" property="document.creditMemoNumber" readOnly="true" /> 
                </td>
                <th class="right" width="25%">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.creditMemoType}" />
                </th>
                <td class="datacell" width="25%">
                   <bean:write name="KualiForm" property="document.creditMemoType" />
                </td>
            </tr>
            
            <tr>
                <th class="right">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.creditMemoDate}" />
                </th>
                <td class="datacell">
                   <kul:htmlControlAttribute attributeEntry="${documentAttributes.creditMemoDate}" property="document.creditMemoDate" readOnly="true" />
                </td>
                <th class="right">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorNumber}" />
                </th>
                <td class="datacell">
                   <kul:htmlControlAttribute attributeEntry="${documentAttributes.vendorNumber}" property="document.vendorNumber" readOnly="true" />
                </td>
             </tr>
             
             <c:if test="${not fullDocumentEntryCompleted}">
                  <tr>
                     <th class="right">
        	    		<kul:htmlAttributeLabel attributeEntry="${documentAttributes.creditMemoAmount}" useShortLabel="true" />
                     </th>
                     <td class="datacell">
                     	<kul:htmlControlAttribute attributeEntry="${documentAttributes.creditMemoAmount}" property="document.creditMemoAmount" readOnly="true" />
                     </td>
                     <th class="right">&nbsp;</th>
                     <td class="datacell">&nbsp;</td>
                  <tr>   
             </c:if>

             <tr>   
                <th class="right">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.purchaseOrderEndDate}" />
                </th>
                <td class="datacell">
                   <kul:htmlControlAttribute  attributeEntry="${documentAttributes.purchaseOrderEndDate}" property="document.purchaseOrder.purchaseOrderEndDate" readOnly="true" />
                </td>
                <th class="right">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.purchaseOrderIdentifier}" />
                </th>
                <td class="datacell">
                   <kul:htmlControlAttribute attributeEntry="${documentAttributes.purchaseOrderIdentifier}" property="document.purchaseOrderIdentifier" readOnly="true" />
                </td>
             </tr>
             
             <tr>   
                <th class="right">
                   <kul:htmlAttributeLabel  attributeEntry="${documentAttributes.purchaseOrderNotes}" />
                </th>
                <td class="datacell">
                   <bean:write name="KualiForm" property="document.purchaseOrderNotes" />
                </td>
                <th class="right">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.paymentRequestIdentifier}" />
                </th>
                <td class="datacell">
                   <kul:htmlControlAttribute attributeEntry="${documentAttributes.paymentRequestIdentifier}" property="document.paymentRequestIdentifier" readOnly="true" />
                </td>
            </tr>
			<tr>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.extractedTimestamp}" />
                </th>
                <td class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.extractedTimestamp}" property="document.extractedTimestamp" readOnly="${true}" />
                    <c:if test="${not empty KualiForm.document.extractedTimestamp}">
                        <purap:disbursementInfo sourceDocumentNumber="${KualiForm.document.documentNumber}" sourceDocumentType="${KualiForm.document.documentType}" />          
					</c:if>
                </td>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.accountsPayableApprovalTimestamp}" />
                </th>
                <td class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${documentAttributes.accountsPayableApprovalTimestamp}" property="document.accountsPayableApprovalTimestamp" readOnly="${not displayInitTab}" />
                </td>          
            </tr>

			<tr>
	            <sys:bankLabel align="right"/>
                <sys:bankControl property="document.bankCode" objectProperty="document.bank" readOnly="${not fullEntryMode}"/>
<%-- MOD-PA2000-01: Baseline Modification Start --%>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.paymentMethodCode}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.paymentMethodCode}" property="document.paymentMethodCode" 
                        	readOnly="${not fullEntryMode}"
                        	onchange="paymentMethodChanged( this.value );" />
                    </td>
<%-- MOD-PA2000-01: Baseline Modification End --%>
            </tr>   
		</table> 
    </div>
<%-- MOD-PA2000-01: Baseline Modification Start --%>
<c:if test="${fullEntryMode}">
	<script type="text/javascript" src="dwr/interface/CUPaymentMethodGeneralLedgerPendingEntryService.js"></script>
	<script type="text/javascript">
		function paymentMethodChanged(selectedMethod) {
		
			if ( selectedMethod != "" ) {
				var dwrReply = {
					callback:function(data) {
					if ( data != null && typeof data == 'object' ) {
							setRecipientValue( "document.bankCode", data.bankCode );
							setRecipientValue( "document.bank", data.bankName );
						} else {
							setRecipientValue( "document.bankCode", "" );
							setRecipientValue( "document.bank", "" );
						}
					},
					errorHandler:function( errorMessage ) { 
						window.status = errorMessage;
					}
				};
				CUPaymentMethodGeneralLedgerPendingEntryService.getBankForPaymentMethod( selectedMethod, dwrReply );
			}
		}	
	</script>
</c:if>
<%-- MOD-PA2000-01: Baseline Modification End --%>    
</kul:tab>
