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

<kul:tab tabTitle="Recurring Details" defaultOpen="false">
	<c:set var="recurringDVAttributes" value="${DataDictionary.RecurringDisbursementVoucherDetail.attributes}" />
	<div class="tab-container" align=center > 
		<table cellpadding=0 class="datatable" summary="Recurring DV Details">
			<tr>
				<kul:htmlAttributeHeaderCell attributeEntry="${recurringDVAttributes.dvCheckDate}" hideRequiredAsterisk="true" scope="col"/>
				<kul:htmlAttributeHeaderCell attributeEntry="${recurringDVAttributes.dvCheckAmount}" hideRequiredAsterisk="true" scope="col"/>
				<kul:htmlAttributeHeaderCell attributeEntry="${recurringDVAttributes.dvCheckStub}" scope="col"/>
				<kul:htmlAttributeHeaderCell attributeEntry="${recurringDVAttributes.dvDocumentNumber}" hideRequiredAsterisk="true" scope="col"/>
			</tr>
			<logic:iterate indexId="ctr" name="KualiForm" property="document.recurringDisbursementVoucherDetails" id="currentDetail">
				<tr>
					<td>
						<kul:htmlControlAttribute attributeEntry="${recurringDVAttributes.dvCheckDate}" 
							property="document.recurringDisbursementVoucherDetails[${ctr}].dvCheckDate" readOnly="true"/>
					</td>
					<td>
						<kul:htmlControlAttribute attributeEntry="${recurringDVAttributes.dvCheckAmount}" 
							property="document.recurringDisbursementVoucherDetails[${ctr}].dvCheckAmount" readOnly="true"/>
					</td>
					<td>
						<kul:htmlControlAttribute attributeEntry="${recurringDVAttributes.dvCheckStub}" readOnly="${!canEdit }" 
							property="document.recurringDisbursementVoucherDetails[${ctr}].dvCheckStub"/>
					</td>
					<td>
						<kul:htmlControlAttribute attributeEntry="${recurringDVAttributes.dvDocumentNumber}" 
							property="document.recurringDisbursementVoucherDetails[${ctr}].dvDocumentNumber" readOnly="true"/>
					</td>
				</tr>
			</logic:iterate>
		</table>
	</div>
</kul:tab>