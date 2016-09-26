<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:tab tabTitle="Recurring Details" defaultOpen="${KualiForm.recurringDVDetailsDefaultOpen}">
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