<%--
 Copyright 2005-2006 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="formAction" required="true" %>
<%@ attribute name="documentTypeName" required="true" %>
<%@ attribute name="dvAttributesType" required="true" type="java.lang.Object" %>

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
    
	<fp:dvTripLink />
	<fp:dvIWantLink />

	
	<c:set var="canEdit" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" scope="request" />
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
			<sys-java:accountingLineGroup newLinePropertyName="newSourceLine" collectionPropertyName="document.sourceAccountingLines" collectionItemPropertyName="document.sourceAccountingLine" attributeGroupName="source" />
		</sys-java:accountingLines>
	</kul:tab>
	
	<fp:dvContact />
    <fp:dvSpecialHandling />
	<fp:dvNRATax />
	<fp:wireTransfer />
	<fp:foreignDraft />
	<fp:dvNonEmployeeTravel />
	<fp:dvPrePaidTravel />
    <fp:dvPDPStatus />
	<gl:generalLedgerPendingEntries />
	<kul:notes attachmentTypesValuesFinderClass="${documentEntry.attachmentTypesValuesFinderClass}" />
	<kul:adHocRecipients />

	<kul:routeLog />

	<kul:superUserActions />

	<kul:panelFooter />
	
	<sys:documentControls transactionalDocument="${documentEntry.transactionalDocument}" extraButtons="${KualiForm.extraButtons}"/>
</kul:documentPage>

