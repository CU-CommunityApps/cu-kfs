<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<%@ attribute name="readOnly" required="true" description="If document is in read only mode"%>

<c:set var="invoiceGeneralDetailAttributes" value="${DataDictionary.InvoiceGeneralDetail.attributes}" />
<c:set var="documentAttributes" value="${DataDictionary.ContractsGrantsInvoiceDocument.attributes}" />
<c:set var="arDocHeaderAttributes" value="${DataDictionary.AccountsReceivableDocumentHeader.attributes}" />

<kul:tab tabTitle="General" defaultOpen="true" tabErrorKey="${KFSConstants.CUSTOMER_INVOICE_DOCUMENT_GENERAL_ERRORS}">
    <div class="tab-container" align=center>
    <h3>Billing Summary</h3>
    <table cellpadding="0" cellspacing="0" class="datatable standard" summary="Invoice Section">
        <tr>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel
                        attributeEntry="${invoiceGeneralDetailAttributes.proposalNumber}"
                        labelFor="document.invoiceGeneralDetail.proposalNumber"
                        useShortLabel="false" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.proposalNumber.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.proposalNumber}"
                            property="document.invoiceGeneralDetail.proposalNumber"
                            readOnly="true" />
                </div>
            </td>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.awardTotal}" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.awardTotal.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.awardTotal}"
                            property="document.invoiceGeneralDetail.awardTotal"
                            readOnly="true" />
                </div>
            </td>
        </tr>
        <tr>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel
                        attributeEntry="${invoiceGeneralDetailAttributes.awardDateRange}"
                        useShortLabel="false" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.awardDateRange.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.awardDateRange}"
                            property="document.invoiceGeneralDetail.awardDateRange"
                            readOnly="true" />
                </div>
            </td>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.totalPreviouslyBilled}" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.totalPreviouslyBilled.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.totalPreviouslyBilled}"
                            property="document.invoiceGeneralDetail.totalPreviouslyBilled"
                            readOnly="true" />
                </div>
            </td>
        </tr>
        <tr>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel
                        attributeEntry="${invoiceGeneralDetailAttributes.billingFrequencyCode}"
                        useShortLabel="false" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.billingFrequencyCode.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.billingFrequencyCode}"
                            property="document.invoiceGeneralDetail.billingFrequencyCode"
                            readOnly="true" />
                </div>
            </td>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.totalAmountBilledToDate}" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.totalAmountBilledToDate.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.totalAmountBilledToDate}"
                            property="document.invoiceGeneralDetail.totalAmountBilledToDate"
                            readOnly="true" />
                    &nbsp;&nbsp;&nbsp;
                    <c:if test="${!empty KualiForm.document.invoiceGeneralDetail.proposalNumber && KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}">
                        <c:if test="${empty KualiForm.document.invoiceMilestones}">
                            <c:if test="${empty KualiForm.document.invoiceBills}">
                                <html:submit
                                        styleClass="btn btn-default small"
                                        property="methodToCall.recalculateTotalAmountBilledToDate"
                                        title="relcalculate"
                                        alt="recalculate"
                                        value="Calculate"/>
                            </c:if>
                        </c:if>
                    </c:if>
                </div>
            </td>
        </tr>
        <tr>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.billingPeriod}" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.billingPeriod.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.billingPeriod}"
                            property="document.invoiceGeneralDetail.billingPeriod"
                            readOnly="${readOnly}" />
                </div>
            </td>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.amountRemainingToBill}" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.amountRemainingToBill.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.amountRemainingToBill}"
                            property="document.invoiceGeneralDetail.amountRemainingToBill"
                            readOnly="true" />
                </div>
            </td>
        </tr>
        <tr>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.finalBillIndicator}" />
            </th>
            <c:choose>
                <c:when test="${KualiForm.document.invoiceReversal}">
                    <td class="datacell" style="width: 25%;"></td>
                </c:when>
                <c:otherwise>
                    <td class="datacell" style="width: 25%;">
                        <div id="document.finalBill.div">
                            <kul:htmlControlAttribute
                                    attributeEntry="${invoiceGeneralDetailAttributes.finalBillIndicator}"
                                    property="document.invoiceGeneralDetail.finalBillIndicator"
                                    readOnly="${readOnly}"/>
                        </div>
                    </td>
                </c:otherwise>
            </c:choose>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.instrumentTypeCode}" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.instrumentTypeCode.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.instrumentTypeCode}"
                            property="document.invoiceGeneralDetail.instrumentTypeCode"
                            readOnly="true" />
                </div>
            </td>
        </tr>
        <tr>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.lastBilledDate}" />
            </th>
            <td class="datacell" style="width: 25%;">
                <div id="document.lastBilledDate.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.lastBilledDate}"
                            property="document.invoiceGeneralDetail.lastBilledDate"
                            readOnly="true" />
                </div>
            </td>
            <th class="right" style="width: 25%;">
                <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.costShareAmount}" />
            </th>
            <td class="datacell" colspan="3">
                <div id="document.costShareAmount.div">
                    <kul:htmlControlAttribute
                            attributeEntry="${invoiceGeneralDetailAttributes.costShareAmount}"
                            property="document.invoiceGeneralDetail.costShareAmount"
                            readOnly="${readOnly}" />
                </div>
            </td>
        </tr>
    </table>

    <c:if test="${!empty KualiForm.document.invoiceGeneralDetail.proposalNumber}">
        <h3>Customer Information</h3>
        <table cellpadding="0" cellspacing="0" class="datatable standard" summary="Invoice Section">
            <tr>
                <th class="right" style="width: 25%;">
                        <kul:htmlAttributeLabel
                                attributeEntry="${arDocHeaderAttributes.customerNumber}"
                                labelFor="document.accountsReceivableDocumentHeader.customerNumber" />
        </div>
        </th>
        <td class="top">
            <c:if test="${not empty KualiForm.document.accountsReceivableDocumentHeader.customerNumber}">
                <kul:inquiry
                        boClassName="org.kuali.kfs.module.ar.businessobject.Customer"
                        keyValues="customerNumber=${KualiForm.document.accountsReceivableDocumentHeader.customerNumber}"
                        render="true">

                    <kul:htmlControlAttribute
                            attributeEntry="${arDocHeaderAttributes.customerNumber}"
                            property="document.accountsReceivableDocumentHeader.customer.customerNumber"
                            readOnly="true"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </kul:inquiry>
            </c:if>
        </td>
        <th class="right" style="width: 25%;">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.customerName}" />
        </th>
        <td class="datacell" style="width: 25%;">
            <div id="document.accountsReceivableDocumentHeader.customer.customerName.div">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.customerName}"
                        property="document.accountsReceivableDocumentHeader.customer.customerName"
                        readOnly="true" />
            </div>
        </td>

        </tr>
        </table>
    </c:if>
    </div>
</kul:tab>
