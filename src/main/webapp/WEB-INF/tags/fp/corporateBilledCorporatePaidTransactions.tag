<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="editingMode" required="false" type="java.util.Map"%>
<%@ attribute name="editableAccounts" required="true" type="java.util.Map"
              description="Map of Accounts which this user is allowed to edit" %>
<%@ attribute name="editableFields" required="false" type="java.util.Map"
              description="Map of accounting line fields which this user is allowed to edit" %>

<c:set var="columnCountUntilAmount" value="8" />
<c:set var="columnCount" value="${columnCountUntilAmount + 1 + (KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] ? 1 : 0)}" />
<c:set var="accountingLineAttributes" value="${DataDictionary['TargetAccountingLine'].attributes}" />

<kul:tab tabTitle="Accounting Lines" defaultOpen="true" tabErrorKey="${KFSConstants.TARGET_ACCOUNTING_LINE_ERROR_PATTERN},document.transactionEntries*"
         helpUrl="${KualiForm.accountingLineImportInstructionsUrl}" helpLabel="Import Templates">
  <c:set var="transactionAttributes" value="${DataDictionary.CorporateBilledCorporatePaidTransactionDetail.attributes}" />
  <c:set var="vendorAttributes" value="${DataDictionary.CorporateBilledCorporatePaidCardVendor.attributes}" />
  <c:set var="cardAttributes" value="${DataDictionary.CorporateBilledCorporatePaidCardHolder.attributes}" />

  <div class="tab-container" align="center">
  <logic:iterate indexId="ctr" name="KualiForm" property="document.transactionEntries" id="currentTransaction">
    <table cellpadding="0" class="datatable" summary="Transaction Details">

       <fp:subheadingWithDetailToggleRow columnCount="4" subheading="Transaction #${currentTransaction.transactionReferenceNumber}"/>
	      <tr>
	        <th scope="row"><div align="right"><kul:htmlAttributeLabel attributeEntry="${cardAttributes.transactionCreditCardNumber}" readOnly="true"/></div></th>
	        <td>
	          <kul:inquiry boClassName="edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidCardHolder"
               keyValues="documentNumber=${currentTransaction.documentNumber}" render="true">
				<c:choose>
					<c:when test="${KualiForm.transactionCreditCardNumbersViewStatus[ctr]}">
						<bean:write name="KualiForm" property="document.procurementCardHolder.transactionCreditCardNumber" />
					</c:when>
					<c:otherwise>
						<kul:htmlControlAttribute attributeEntry="${cardAttributes.transactionCreditCardNumber}" property="document.procurementCardHolder.transactionCreditCardNumber"
						 readOnly="true" />
					</c:otherwise>
				</c:choose>
	          </kul:inquiry>
	        </td>
			<th>&nbsp;</th>
			<td>&nbsp;</td>
	      </tr>
	      <tr>
	        <th scope="row"><div align="right"><kul:htmlAttributeLabel attributeEntry="${cardAttributes.cardHolderName}" readOnly="true"/></div></th>
	        <td><kul:htmlControlAttribute attributeEntry="${cardAttributes.cardHolderName}" property="document.procurementCardHolder.cardHolderName" readOnly="true"/></td>
            <th> <div align="right"><kul:htmlAttributeLabel attributeEntry="${transactionAttributes.transactionTotalAmount}"/></div></th>
            <td valign=top><kul:htmlControlAttribute attributeEntry="${transactionAttributes.transactionTotalAmount}" property="document.transactionEntries[${ctr}].transactionTotalAmount" readOnly="true"/></td>
	     </tr>
       <tr>
          <th><div align="right"><kul:htmlAttributeLabel attributeEntry="${transactionAttributes.transactionDate}"/></div></th>
          <td valign=top><bean:write name="KualiForm" property="document.transactionEntries[${ctr}].transactionDate" /></td>
          <th> <div align="right"><kul:htmlAttributeLabel attributeEntry="${transactionAttributes.transactionReferenceNumber}"/></div></th>
          <td valign=top>
            <kul:inquiry boClassName="edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionDetail"
               keyValues="documentNumber=${currentTransaction.documentNumber}&financialDocumentTransactionLineNumber=${currentTransaction.financialDocumentTransactionLineNumber}"
               render="true">
				<bean:write name="KualiForm" property="document.transactionEntries[${ctr}].transactionReferenceNumber" />
            </kul:inquiry>
          </td>
       </tr>
       <tr>
          <th> <div align="right"><kul:htmlAttributeLabel attributeEntry="${vendorAttributes.vendorName}"/></div></th>
          <td valign=top>
            <kul:inquiry boClassName="edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidCardVendor"
               keyValues="documentNumber=${currentTransaction.documentNumber}&financialDocumentTransactionLineNumber=${currentTransaction.financialDocumentTransactionLineNumber}"
               render="true">
				<bean:write name="KualiForm" property="document.transactionEntries[${ctr}].procurementCardVendor.vendorName" />
            </kul:inquiry>
          </td>
          <th colspan="2"> <div align="left">
		  <c:choose>
			<c:when test="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}">
				<a href="${KualiForm.disputeURL}" target="_blank"><img src="${ConfigProperties.externalizable.images.url}buttonsmall_dispute.gif"/></a>
			</c:when>
			<c:otherwise>&nbsp;</c:otherwise>
		  </c:choose>
          </div></th>
       </tr>
    </table>

	<%-- For accounting lines to tab through the fields correctly, the sys-java:accountingLineGroup needs to be in a TAB tag, and in
	 	sys-java:accountingLines.  Nesting accountingLineGroup sets the tabindex element of the image tags very high, and the actual fields
	 	have no tabindex attribute at all.  Without nesting both the images and fields have no tabindex attribute.
	--%>
	<kul:subtab width="100%" noShowHideButton="true" useCurrentTabIndexAsKey="false">
		<sys-java:accountingLines>
			<sys-java:accountingLineGroup newLinePropertyName="newTargetLines[${ctr}]" collectionPropertyName="document.transactionEntries[${ctr}].targetAccountingLines" collectionItemPropertyName="document.transactionEntries[${ctr}].targetAccountingLines" attributeGroupName="target" />
		</sys-java:accountingLines>
	</kul:subtab>

    <br/>
   </logic:iterate>
  </div>
  <SCRIPT type="text/javascript">
    var kualiForm = document.forms['KualiForm'];
    var kualiElements = kualiForm.elements;
  </SCRIPT>
</kul:tab>
