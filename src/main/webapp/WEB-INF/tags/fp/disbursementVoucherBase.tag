<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="formAction" required="true" %>
<%@ attribute name="documentTypeName" required="true" %>
<%@ attribute name="dvAttributesType" required="true" type="java.lang.Object" %>
<%@ attribute name="displayGLPE" required="true" type="java.lang.Boolean"%>
<%@ attribute name="displayRecurringDetail" required="true" type="java.lang.Boolean"%>
<%@ attribute name="displayRecurringPDPDetail" required="true" type="java.lang.Boolean"%>

<kul:documentPage showDocumentInfo="true"
	htmlFormAction="${formAction}"
	documentTypeName="${documentTypeName}"
	renderMultipart="true" showTabButtons="true">
	
	<fp:dvPrintCoverSheet />
	   <script type="text/javascript">
        function clearSpecialHandlingTab() {
        var prefix = "document.dvPayeeDetail.";
        var ctrl;
        
        ctrl = kualiElements[prefix+"disbVchrSpecialHandlingPersonName"];
        ctrl.value = "";
        
        ctrl = kualiElements[prefix + "disbVchrSpecialHandlingCityName"]
        ctrl.value = "";
        
        ctrl = kualiElements[prefix + "disbVchrSpecialHandlingLine1Addr"];
        ctrl.value = "";
        
        ctrl = kualiElements[prefix + "disbVchrSpecialHandlingStateCode"];
        ctrl.value = "";
        
        ctrl = kualiElements[prefix + "disbVchrSpecialHandlingLine2Addr"];
        ctrl.value = "";
        
        ctrl = kualiElements[prefix + "disbVchrSpecialHandlingZipCode"];
        ctrl.value = "";
        
        ctrl = kualiElements[prefix + "disbVchrSpecialHandlingCountryCode"];
        ctrl.value = "";
       }
    </script>
    <sys:paymentMessages />
    <script type="text/javascript">
        const paymentMethodCodesRequiringAdditionalData = new Set();
        <c:forEach items="${KualiForm.paymentMethodCodesRequiringAdditionalData}" var="code">
            paymentMethodCodesRequiringAdditionalData.add('<e:forJavaScript value="${code}" />');
        </c:forEach>

        function onDvPaymentMethodChanged(input) {
            const selectedMethod = input.value;
            if (paymentMethodCodesRequiringAdditionalData.has(selectedMethod)) {
                paymentMethodMessages(selectedMethod);
            }

            input.form.submit();
        }
    </script>
    
	<fp:dvTripLink />
	<fp:dvIWantLink />

	
	<c:set var="canEdit" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}" scope="request" />
	<c:set var="fullEntryMode" value="${canEdit && KualiForm.editingMode['fullEntry']}" scope="request" />

	<c:set var="frnEntryMode" value="${canEdit && KualiForm.editingMode['frnEntry']}" scope="request" />
	<c:set var="travelEntryMode" value="${canEdit && KualiForm.editingMode['travelEntry']}" scope="request" />
	
	<c:set var="wireEntryMode" value="${canEdit && KualiForm.editingMode['wireEntry']}" scope="request" />
	<c:set var="taxEntryMode" value="${canEdit && KualiForm.editingMode['taxEntry']}" scope="request" />
	
	<c:set var="payeeEntryMode" value="${canEdit && KualiForm.editingMode['payeeEntry']}" scope="request" />
	
	<c:set var="paymentHandlingEntryMode" value="${canEdit && KualiForm.editingMode['paymentHandlingEntry']}" scope="request" />
	<c:set var="voucherDeadlineEntryMode" value="${canEdit && KualiForm.editingMode['voucherDeadlineEntry']}" scope="request" />
	<c:set var="specialHandlingChangingEntryMode" value="${canEdit && KualiForm.editingMode['specialHandlingChangingEntry']}" scope="request" />
	<c:set var="immediateDisbursementEntryMode" value="${canEdit && KualiForm.editingMode['immediateDisbursementEntryMode']}" scope="request"/>
	
	<sys:documentOverview editingMode="${KualiForm.editingMode}" includeBankCode="true"
	  bankProperty="document.disbVchrBankCode" 
	  bankObjectProperty="document.bank"
	  disbursementOnly="true" />
	  
	<fp:dvPayment dvAttributes="${dvAttributesType}" />

    <kul:tab tabTitle="Accounting Lines" defaultOpen="true" tabErrorKey="${KFSConstants.ACCOUNTING_LINE_ERRORS}">
        <sys-java:accountingLines>
            <sys-java:accountingLineGroup newLinePropertyName="newSourceLine" collectionPropertyName="document.sourceAccountingLines"
                                          collectionItemPropertyName="document.sourceAccountingLine" attributeGroupName="source"/>
        </sys-java:accountingLines>
    </kul:tab>
	
	<fp:dvContact />
    <fp:dvSpecialHandling />
	<fp:dvNonresidentTax/>
	<fp:wireTransfer />
	<fp:foreignDraft />
	<fp:dvNonEmployeeTravel />
	<fp:dvPrePaidTravel />
	
	<c:choose>
		<c:when test="${displayRecurringPDPDetail }">
			<fp:recurringDVPDPStatus />
		</c:when>
		<c:otherwise>
			<fp:dvPDPStatus />
		</c:otherwise>
	</c:choose>
    
    <c:if test="${displayGLPE}">
    	<gl:generalLedgerPendingEntries />
    </c:if>

    <c:if test="${displayRecurringDetail}">
    	<fp:recurringDVDetails />
    </c:if>
    
	
	<kul:notes attachmentTypesValuesFinder="${documentEntry.attachmentTypesValuesFinder}" />
	<kul:adHocRecipients />

	<kul:routeLog />

	<kul:superUserActions />

	<sys:documentControls transactionalDocument="${documentEntry.transactionalDocument}" extraButtons="${KualiForm.extraButtons}"/>
	<kul:modernLookupSupport />
	<script type="application/javascript">
      document.addEventListener('DOMContentLoaded', () => {
        wireReplaceInvalidCharacters();
      });
	</script>
</kul:documentPage>

