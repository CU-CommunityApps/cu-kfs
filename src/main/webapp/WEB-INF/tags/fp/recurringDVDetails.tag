<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:tab tabTitle="Recurring Details (${KualiForm.recurringDVDetailsSize })" defaultOpen="${KualiForm.recurringDVDetailsDefaultOpen}">
	<c:set var="recurringDVAttributes" value="${DataDictionary.RecurringDisbursementVoucherDetail.attributes}" />
	<div class="tab-container"> 
		<table class="standard side-margins" summary="Recurring DV Details">
			<tr class="header">
				<kul:htmlAttributeHeaderCell attributeEntry="${recurringDVAttributes.dvCheckDate}" hideRequiredAsterisk="true" scope="col"/>
				<kul:htmlAttributeHeaderCell attributeEntry="${recurringDVAttributes.dvCheckAmount}" hideRequiredAsterisk="true" scope="col"/>
				<kul:htmlAttributeHeaderCell attributeEntry="${recurringDVAttributes.dvCheckStub}" scope="col"/>
				<kul:htmlAttributeHeaderCell attributeEntry="${recurringDVAttributes.dvDocumentNumber}" hideRequiredAsterisk="true" scope="col"/>
			</tr>
			<logic:iterate indexId="ctr" name="KualiForm" property="document.recurringDisbursementVoucherDetails" id="currentDetail">
				<tr class="${ctr % 2 == 0 ? "highlight" : ""}">
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${recurringDVAttributes.dvCheckDate}" 
							property="document.recurringDisbursementVoucherDetails[${ctr}].dvCheckDate" readOnly="true"/>
					</td>
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${recurringDVAttributes.dvCheckAmount}" 
							property="document.recurringDisbursementVoucherDetails[${ctr}].dvCheckAmount" readOnly="true"/>
					</td>
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${recurringDVAttributes.dvCheckStub}" readOnly="${!canEdit }" 
							property="document.recurringDisbursementVoucherDetails[${ctr}].dvCheckStub"/>
					</td>
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${recurringDVAttributes.dvDocumentNumber}" 
							property="document.recurringDisbursementVoucherDetails[${ctr}].dvDocumentNumber" readOnly="true"/>
					</td>
				</tr>
			</logic:iterate>
		</table>
	</div>
</kul:tab>
