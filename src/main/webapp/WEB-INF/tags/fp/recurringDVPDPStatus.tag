<%--
 Copyright 2007-2009 The Kuali Foundation
 
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

<c:set var="dvAttributes" value="${DataDictionary.DisbursementVoucherDocument.attributes}" />
<c:set var="recurringDVDetailAttributes" value="${DataDictionary.RecurringDisbursementVoucherDetail.attributes}" />
<c:set var="recurringDVAttributes" value="${DataDictionary.RecurringDisbursementVoucherDocument.attributes}" />
<kul:tab tabTitle="Pre-Disbursement Processor Status"
	defaultOpen="${KualiForm.preDisbursementProcessorTabDefaultOpen}"
	tabErrorKey="document.paymentCancelReason">
	<div class="tab-container" align=center>
		<table cellpadding=0 class="datatable"
			summary="Recurring DV PDP Statuses">
			<tr>
				<th>DV Document Number</th>
				<th>Due Date</th>
				<th>PDP Status</th>
				<th>Extract Date</th>
				<th>Paid Date</th>
				<th>Cancel Date</th>
			</tr>
			<logic:iterate indexId="ctr" name="KualiForm" property="pdpStatuses" id="currentDetail">
				<tr>
					<td class="datacell"><kul:htmlControlAttribute
							attributeEntry="${recurringDVDetailAttributes.dvDocumentNumber}"
							property="pdpStatuses[${ctr}].documentNumber" readOnly="true" />
					</td>
					<td class="datacell"><kul:htmlControlAttribute
							attributeEntry="${dvAttributes.disbursementVoucherDueDate}"
							property="pdpStatuses[${ctr}].dueDate" readOnly="true" /></td>
					<td class="datacell"><kul:htmlControlAttribute
							attributeEntry="${dvAttributes.disbursementVoucherPdpStatus}"
							property="pdpStatuses[${ctr}].pdpStatus" readOnly="true" /></td>
					<td class="datacell"><kul:htmlControlAttribute
							attributeEntry="${dvAttributes.extractDate}"
							property="pdpStatuses[${ctr}].extractDate" readOnly="true" /></td>
					<td class="datacell"><kul:htmlControlAttribute
							attributeEntry="${dvAttributes.paidDate}"
							property="pdpStatuses[${ctr}].paidDate" readOnly="true" /> <c:if
							test="${not empty currentDetail.extractDate}">
							<fp:dvDisbursementInfo
								sourceDocumentNumber="${currentDetail.documentNumber}"
								sourceDocumentType="${currentDetail.paymentDetailDocumentType}" />
						</c:if></td>
					<td class="datacell"><kul:htmlControlAttribute
							attributeEntry="${dvAttributes.cancelDate}"
							property="pdpStatuses[${ctr}].cancelDate" readOnly="true" /></td>
				</tr>
			</logic:iterate>
			<c:if test="${KualiForm.cancelPDPPaymentsActionAvailable}">
				<tr>
					<th colspan="5" align="center"><kul:htmlAttributeLabel
							attributeEntry="${recurringDVAttributes.paymentCancelReason}" />
						<br /> <kul:htmlControlAttribute
							attributeEntry="${recurringDVAttributes.paymentCancelReason}"
							property="document.paymentCancelReason" readOnly="false" /></th>
					<th align="center" valign="center"><html:submit
							styleClass="btn btn-default"
							property="methodToCall.confirmAndCancel"
							title="Cancel PDP Payments" alt="Cancel PDP Payments"
							value="Cancel PDP Payments" /></th>
				</tr>
			</c:if>
		</table>
	</div>
</kul:tab>