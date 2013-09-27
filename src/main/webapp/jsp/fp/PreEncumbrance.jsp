<%--
 Copyright 2005 The Kuali Foundation
 
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

<kul:documentPage showDocumentInfo="true"
	documentTypeName="PreEncumbranceDocument"
	htmlFormAction="financialPreEncumbrance" renderMultipart="true"
	showTabButtons="true">

	<c:set var="generalLedgerPendingEntriesList" value="${generalLedgerPendingEntries}" />
	<c:if test="${empty generalLedgerPendingEntries}">
		<c:set var="generalLedgerPendingEntriesList" value="${KualiForm.document.generalLedgerPendingEntries}" />
	</c:if>

	<sys:hiddenDocumentFields />

	<sys:documentOverview editingMode="${KualiForm.editingMode}" />
	<fp:preEncumbranceDetails editingMode="${KualiForm.editingMode}" />
	
	<kul:tab tabTitle="Accounting Lines" defaultOpen="true" tabErrorKey="${KFSConstants.ACCOUNTING_LINE_ERRORS}">
		<sys-java:accountingLines>		
			<sys-java:accountingLineGroup newLinePropertyName="newSourceLine" collectionPropertyName="document.sourceAccountingLines" collectionItemPropertyName="document.sourceAccountingLine" attributeGroupName="source" />
			<sys-java:accountingLineGroup newLinePropertyName="newTargetLine" collectionPropertyName="document.targetAccountingLines" collectionItemPropertyName="document.targetAccountingLine" attributeGroupName="target"/>
		</sys-java:accountingLines>
		<script type="text/javascript">		 		
							var i = 0;
							try {
								var image = document.getElementById('tab-EncumbranceAutomaticPartialDisEncumbrances-newSourceLine-imageToggle');
								if (document.forms['KualiForm'].elements['tabStates(EncumbranceAutomaticPartialDisEncumbrances-newSourceLine)'].value == "CLOSE") {
								image.click();
								}
							}
							catch (err) {
								//do nothing
							}
							do
							{
								try {
								 	image = document.getElementById('tab-EncumbranceAutomaticPartialDisEncumbrances-document-sourceAccountingLine('+i+')-imageToggle');
	    							 if (document.forms['KualiForm'].elements['tabStates(EncumbranceAutomaticPartialDisEncumbrances-document-sourceAccountingLine('+i+'))'].value == "CLOSE") {					
	   							 i = i + 1;
	   							 image.click();							 
	    								}	  							 																	
									}
								catch (err)
								{
									i = 4545;
								} 
							}
							while (i != 4545)											
		</script>
	</kul:tab>
		
	<fp:preEncumbranceGeneralLedgerPendingEntries />
	<c:if test="${!empty generalLedgerPendingEntriesList}"> 
			<script type="text/javascript">		 				
				try {	
						var image = document.getElementById('tab-GeneralLedgerPendingEntries-imageToggle');
						if (document.forms['KualiForm'].elements['tabStates(GeneralLedgerPendingEntries)'].value == "CLOSE") {
							image.click();
						}
				   	}
				catch (err){}
			</script>
	</c:if>
	<kul:notes />
	<kul:adHocRecipients />
	<kul:routeLog />
	<kul:superUserActions />
	<kul:panelFooter />
	<sys:documentControls
		transactionalDocument="${documentEntry.transactionalDocument}" extraButtons="${KualiForm.extraButtons}" />

</kul:documentPage>
