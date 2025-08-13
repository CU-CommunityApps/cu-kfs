<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2024 Kuali, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<%@ attribute name="readOnly" required="true" description="If document is in read only mode"%>

<c:set var="invoiceAccountDetailsAttributes" value="${DataDictionary.InvoiceAccountDetail.attributes}" />
<c:set var="invoiceGeneralDetailAttributes" value="${DataDictionary.InvoiceGeneralDetail.attributes}" />
<c:set var="documentAttributes" value="${DataDictionary.ContractsGrantsInvoiceDocument.attributes}" />
<c:set var="readOnlyForFinal" value="${readOnly or !KualiForm.document.finalizable}" />
<c:set var="readOnlyForBillignPeriodAdjustment" value="${readOnly or !KualiForm.document.billingPeriodAdjusted}" />
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
                    <kul:inquiry
                            boClassName="org.kuali.kfs.module.cg.businessobject.Award"
                            keyValues="proposalNumber=${KualiForm.document.invoiceGeneralDetail.proposalNumber}"
                            render="true">
                        <kul:htmlControlAttribute
                                attributeEntry="${invoiceGeneralDetailAttributes.proposalNumber}"
                                property="document.invoiceGeneralDetail.proposalNumber"
                                readOnly="true" />
                    </kul:inquiry>
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
                    <c:if test="${!empty KualiForm.document.invoiceGeneralDetail.proposalNumber
							&& KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]
							&& empty KualiForm.document.invoiceMilestones
							&& empty KualiForm.document.invoiceBills}">
                        <html:submit
                                styleClass="btn btn-default small"
                                property="methodToCall.recalculateTotalAmountBilledToDate"
                                title="relcalculate"
                                alt="recalculate"
                                value="Calculate"/>
                    </c:if>
                </div>
            </td>
        </tr>
        <c:choose>
            <c:when test="${KualiForm.document.invoiceGeneralDetail.billingFrequencyCode eq ArConstants.BillingFrequencyValues.MILESTONE.code or
					KualiForm.document.invoiceGeneralDetail.billingFrequencyCode eq ArConstants.BillingFrequencyValues.PREDETERMINED_BILLING.code}">
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
                                            readOnly="${readOnly}" /> <!-- CU Customization -->
                                </div>
                            </td>
                        </c:otherwise>
                    </c:choose>
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
                        <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.instrumentTypeDescription}" />
                    </th>
                    <td class="datacell" style="width: 25%;">
                        <div id="document.instrumentTypeDescription.div">
                            <kul:htmlControlAttribute
                                    attributeEntry="${invoiceGeneralDetailAttributes.instrumentTypeDescription}"
                                    property="document.invoiceGeneralDetail.instrumentTypeDescription"
                                    readOnly="true" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <th class="right" style="width: 25%;">
                        <kul:htmlAttributeLabel attributeEntry="${invoiceAccountDetailsAttributes.chartOfAccountsCode}" />
                    </th>
                    <td class="datacell" style="width: 25%;">
                        <div id="document.chartOfAccountsCode.div">
                            <kul:htmlControlAttribute
                                    attributeEntry="${invoiceAccountDetailsAttributes.chartOfAccountsCode}"
                                    property="document.accountDetails[0].chartOfAccountsCode"
                                    readOnly="true" />
                        </div>
                    </td>
                    <th class="right" style="width: 25%;">
                        <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.costShareAmount}" />
                    </th>
                    <td class="datacell" style="width: 25%;">
                        <div id="document.costShareAmount.div">
                            <kul:htmlControlAttribute
                                    attributeEntry="${invoiceGeneralDetailAttributes.costShareAmount}"
                                    property="document.invoiceGeneralDetail.costShareAmount"
                                    readOnly="${readOnly}" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <th class="right" style="width: 25%;">
                        <kul:htmlAttributeLabel attributeEntry="${invoiceAccountDetailsAttributes.accountNumber}" />
                    </th>
                    <td class="datacell" style="width: 25%;">
                        <div id="document.accountNumber.div">
                            <kul:htmlControlAttribute
                                    attributeEntry="${invoiceAccountDetailsAttributes.accountNumber}"
                                    property="document.accountDetails[0].accountNumber"
                                    readOnly="true" />
                        </div>
                    </td>
                    <th class="right" style="width: 25%;">
                        <kul:htmlAttributeLabel attributeEntry="${invoiceAccountDetailsAttributes['account.accountName']}" />
                    </th>
                    <td class="datacell" style="width: 25%;">
                        <div id="document.accountName.div">
                            <kul:htmlControlAttribute
                                    attributeEntry="${invoiceAccountDetailsAttributes['account.accountName']}"
                                    property="document.accountDetails[0].account.accountName"
                                    readOnly="true" />
                        </div>
                    </td>
                </tr>
            </c:when>
            <c:otherwise>
                <tr>
                    <th class="right" style="width: 25%;">
                        <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.billingPeriod}" />
                    </th>
                    <td class="datacell" style="width: 25%;">
                        <div id="document.billingPeriod.div">
                            <kul:htmlControlAttribute
                                    attributeEntry="${invoiceGeneralDetailAttributes.billingPeriod}"
                                    property="document.invoiceGeneralDetail.billingPeriod"
                                    readOnly="${readOnlyForBillignPeriodAdjustment}" />
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
                                            readOnly="${readOnly}" />  <!-- CU Customization -->
                                </div>
                            </td>
                        </c:otherwise>
                    </c:choose>
                    <th class="right" style="width: 25%;">
                        <kul:htmlAttributeLabel attributeEntry="${invoiceGeneralDetailAttributes.instrumentTypeDescription}" />
                    </th>
                    <td class="datacell" style="width: 25%;">
                        <div id="document.instrumentTypeDescription.div">
                            <kul:htmlControlAttribute
                                    attributeEntry="${invoiceGeneralDetailAttributes.instrumentTypeDescription}"
                                    property="document.invoiceGeneralDetail.instrumentTypeDescription"
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
                    <td class="datacell" style="width: 25%;">
                        <div id="document.costShareAmount.div">
                            <kul:htmlControlAttribute
                                    attributeEntry="${invoiceGeneralDetailAttributes.costShareAmount}"
                                    property="document.invoiceGeneralDetail.costShareAmount"
                                    readOnly="${readOnly}" />
                        </div>
                    </td>
                </tr>
            </c:otherwise>
        </c:choose>
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