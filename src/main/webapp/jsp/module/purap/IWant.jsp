<%--
 Copyright 2006 The Kuali Foundation
 
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

<c:set var="headerTitle" value="${KualiForm.headerTitle}" />

<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="step" value="${KualiForm.step}" />
<c:set var="isAdHocApprover" value="${KualiForm.editingMode['completeOrder']}"/>
<%-- TODO: We have to use a hard-coded 'A' since constants from KewApiConstants can't be accessed when KEW is not in LOCAL mode. Should we fix this? --%>
<c:set var="canAdHocRouteForApprove" value="${KualiForm.adHocActionRequestCodes['A']}"/>	
<c:set var="isRegularStep" value="${ step eq 'regular' }"/>

<%-- Variable storing tab title message for use at various points. --%>
<c:set var="iwntTabTitle" value="IWNT"/>

<kul:documentPage showDocumentInfo="true"
		docTitle="${headerTitle}"
		documentTypeName="IWantDocument"
		htmlFormAction="purapIWant" renderMultipart="true"
		showTabButtons="${isRegularStep}">

	<SCRIPT type="text/javascript">
		var kualiForm = document.forms['KualiForm'];
		var kualiElements = kualiForm.elements;
	</SCRIPT>

	<%--<script type='text/javascript' src="dwr/interface/IWantAmountUtil.js"></script>--%>

	<%-- Display "Document Overview" tab, if at the regular or customer data steps. --%>
	<c:if test="${isRegularStep or (step eq 'customerDataStep')}">
		<kul:tabTop tabTitle="Document Overview" defaultOpen="true" tabErrorKey="${Constants.DOCUMENT_ERRORS}">
			<purap:iWantDocumentOverview editingMode="${KualiForm.editingMode}" readOnly="${not fullEntryMode}" />
			<purap:iWantCustomerData documentAttributes="${DataDictionary.IWantDocument.attributes}" />
		</kul:tabTop>
	</c:if>

	<%-- Display "Items" and "Accounting Info tabs, if at the regular or item+acct data steps. --%>
	<c:if test="${isRegularStep or (step eq 'itemAndAcctDataStep')}">
		<c:choose><c:when test="${isRegularStep}">
			<kul:tab tabTitle="Items" defaultOpen="true" tabErrorKey="${CUKFSConstants.I_WANT_DOC_ITEM_TAB_ERRORS}">
				<purap:iWantItems itemAttributes="${DataDictionary.IWantItem.attributes}" />
			</kul:tab>
			<c:set var="iwntTabTitle" value="Accounting Info"/>
		</c:when><c:otherwise>
			<kul:tabTop tabTitle="Items & Account Info" defaultOpen="true" tabErrorKey="${CUKFSConstants.I_WANT_DOC_ITEM_TAB_ERRORS}">
				<purap:iWantItems itemAttributes="${DataDictionary.IWantItem.attributes}"/>
			</kul:tabTop>
			<c:set var="iwntTabTitle" value="Account"/>
		</c:otherwise></c:choose>

		<kul:tab tabTitle="${iwntTabTitle}" defaultOpen="true" tabErrorKey="${CUKFSConstants.I_WANT_DOC_ACCOUNT_TAB_ERRORS}">	
			<purap:iWantAccountInfo documentAttributes="${DataDictionary.IWantDocument.attributes}" wizard="${!isRegularStep}" />
		</kul:tab>
	</c:if>

	<%-- Display vendor, misc info, and notes tabs, if at the regular or vendor steps. --%>
	<c:if test="${isRegularStep or (step eq 'vendorStep')}">
		<c:choose><c:when test="${isRegularStep}">
			<kul:tab tabTitle="Vendor" defaultOpen="true" tabErrorKey="${CUKFSConstants.I_WANT_DOC_VENDOR_TAB_ERRORS}">
				<purap:iWantVendor documentAttributes="${DataDictionary.IWantDocument.attributes}" />
			</kul:tab>
		</c:when><c:otherwise>
			<kul:tabTop tabTitle="Vendor" defaultOpen="true" tabErrorKey="${CUKFSConstants.I_WANT_DOC_VENDOR_TAB_ERRORS}">
				<purap:iWantVendor documentAttributes="${DataDictionary.IWantDocument.attributes}" wizard="true"/>
			</kul:tabTop>
		</c:otherwise></c:choose>

		<purap:iWantMisc documentAttributes="${DataDictionary.IWantDocument.attributes}" />

		<%-- Display related documents, if a req has been created from this doc. --%>
		<c:if test="${isRegularStep && !empty(KualiForm.document.reqsDocId)}">
			<purap:relatedDocuments documentAttributes="${DataDictionary.RelatedDocuments.attributes}" />
		</c:if>
		
		<c:if test="${!empty(KualiForm.document.dvDocId)}">
			<purap:iWantRelatedDocuments documentAttributes="${DataDictionary.RelatedDocuments.attributes}" />
		</c:if>     

		<purap:iWantNotes defaultOpen="true"/>
	</c:if>

	<%-- Display routing and submission tab, if at the regular or routing steps. --%>
	<c:choose><c:when test="${isRegularStep}">
		<c:if test="${canAdHocRouteForApprove != null}">
			<kul:tab tabTitle="Routing and Submission" defaultOpen="true">
				<purap:iWantAdHocRecipients />
			</kul:tab>
		</c:if>
	</c:when><c:when test="${(step eq 'routingStep')}">
		<kul:tabTop tabTitle="Routing and Submission" defaultOpen="true" tabErrorKey="${PurapConstants.VENDOR_ERRORS}">
			<c:if test="${canAdHocRouteForApprove != null}">
				<purap:iWantAdHocRecipients />
			</c:if>
		</kul:tabTop>
	</c:when><c:otherwise>
		<%-- Do not display this section if not at the proper step. --%>
	</c:otherwise></c:choose>

	<%-- Display the route log and order completed sections, if at the regular step. --%>
	<c:if test="${isRegularStep}">
		<kul:routeLog />

		<%-- Only ad hoc approvers should see this section. --%>
		<c:if test="${isAdHocApprover}">
			<kul:tab tabTitle="Order Completed (Required)" defaultOpen="true" tabErrorKey="${CUKFSConstants.I_WANT_DOC_ORDER_COMPLETED_TAB_ERRORS}">
				<div align="center" class="tab-container" >
					<table cellpadding="0" cellspacing="0" class="datatable" summary="Complete Information">
						<tr>
							<td class="subhead">Order Completed Information</td>
						</tr>
						<tr><td height="10" class="neutral"></td></tr>
						<tr align="center">
							<td align="center" class="neutral">
								<div align="center">
									<div align="center">
										<kul:htmlControlAttribute
												attributeEntry="${DataDictionary.IWantDocument.attributes.completeOption}"
												property="document.completeOption" readOnly="${not fullEntryMode}" />&nbsp;
									</div>
								</div>
							</td>
						</tr>
						<tr><td height="10" class="neutral"></td></tr>
					</table>
				</div>
			</kul:tab>
		</c:if>
	</c:if>

	<kul:panelFooter />

	<c:set var="extraButtons" value="${KualiForm.extraButtons}"/>

	<sys:documentControls transactionalDocument="true" extraButtons="${extraButtons}" suppressRoutingControls="${!isRegularStep and KualiForm.editingMode['wizard']}"/>

</kul:documentPage>
<%--<kul:page showDocumentInfo="true"
		htmlFormAction="purapIWant" renderMultipart="true"
		docTitle="${headerTitle}"
		transactionalDocument="true">

	<SCRIPT type="text/javascript">
		var kualiForm = document.forms['KualiForm'];
		var kualiElements = kualiForm.elements;
	</SCRIPT>

	<script type='text/javascript' src="dwr/interface/IWantAmountUtil.js"></script>

	<c:if test="${(step eq 'customerDataStep')}">

		<kul:tabTop tabTitle="Document Overview" defaultOpen="true" tabErrorKey="${Constants.DOCUMENT_ERRORS}">

			<purap:iWantDocumentOverview editingMode="${KualiForm.editingMode}" readOnly="false" />

			<purap:iWantCustomerData documentAttributes="${DataDictionary.IWantDocument.attributes}" />

		</kul:tabTop>

	</c:if>

	<c:if test="${(step eq 'itemAndAcctDataStep')}">
		<kul:tabTop tabTitle="Items & Account Info" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_ITEM_TAB_ERRORS}">
			<purap:iWantItems itemAttributes="${DataDictionary.IWantItem.attributes}"/>
		</kul:tabTop>

		<kul:tab tabTitle="Account" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_ACCOUNT_TAB_ERRORS}" >
			<purap:iWantAccountInfo documentAttributes="${DataDictionary.IWantDocument.attributes}" wizard="true" />
		</kul:tab>

	</c:if>
	<c:if test="${(step eq 'vendorStep')}">
		<kul:tabTop tabTitle="Vendor" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_VENDOR_TAB_ERRORS}">
			<purap:iWantVendor documentAttributes="${DataDictionary.IWantDocument.attributes}" wizard="true"/>
		</kul:tabTop>

		<purap:iWantMisc documentAttributes="${DataDictionary.IWantDocument.attributes}" />

		<purap:iWantNotes defaultOpen="true"/>

	</c:if>

	<c:if test="${(step eq 'routingStep')}">
		<kul:tabTop tabTitle="Routing and Submission" defaultOpen="true" tabErrorKey="${PurapConstants.VENDOR_ERRORS}">
			<c:if test="${canAdHocRouteForApprove != null}">
				<purap:iWantAdHocRecipients />
			</c:if>
		</kul:tabTop>
	</c:if>

	<kul:panelFooter />

	<c:set var="extraButtons" value="${KualiForm.extraButtons}"/>

	<sys:documentControls transactionalDocument="true" extraButtons="${extraButtons}" suppressRoutingControls="${KualiForm.editingMode['wizard']}"/>

</kul:page>--%>
