<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="dvAttributes" value="${DataDictionary.DisbursementVoucherDocument.attributes}"/>
<c:set var="paymentDetailAttributes" value="${DataDictionary.PurchasingPaymentDetail.attributes}"/>

<c:set var="dvStatus" value="${KualiForm.generatedDvDocument.documentHeader.workflowDocument.status}"/>
<c:set var="dvPaymentDetails" value="${KualiForm.dvPaymentDetails}"/>
<c:set var="dvHasExactlyOnePayment" value="${fn:length(dvPaymentDetails) eq 1}"/>

<kul:tab tabTitle="Disbursement Voucher Payments" defaultOpen="true">
    <div class="tab-container">
        <h3>DV Payment Information</h3>
        <c:choose>
            <c:when test="${dvStatus eq 'PROCESSED' || dvStatus eq 'FINAL'}">
                <table class="datatable standard side-margins" summary="DV PDP Status">
                    <tr>
                        <th class="right" width="50%">
                            <kul:htmlAttributeLabel attributeEntry="${dvAttributes.disbursementVoucherPdpStatus}"/>
                        </th>
                        <td class="datacell" width="50%">
                            <kul:htmlControlAttribute
                                attributeEntry="${dvAttributes.disbursementVoucherPdpStatus}"
                                property="generatedDvDocument.disbursementVoucherPdpStatus"
                                readOnly="true"/>
                        </td>
                    </tr>
                </table>
                <table class="datatable standard side-margins" summary="Payments">
                    <tr class="header">
                        <kul:htmlAttributeHeaderCell literalLabel="&nbsp;" scope="col" align="left"/>
                        <kul:htmlAttributeHeaderCell
                            attributeEntry="${paymentDetailAttributes['paymentGroup.disbursementType.name']}"
                            hideRequiredAsterisk="true" useShortLabel="false" scope="col" align="left"/>
                        <kul:htmlAttributeHeaderCell
                            attributeEntry="${paymentDetailAttributes['paymentGroup.disbursementDate']}"
                            hideRequiredAsterisk="true" useShortLabel="false" scope="col" align="left"/>
                    </tr>
                    <c:forEach var="payment" items="${dvPaymentDetails}" varStatus="status">
                        <c:set var="lineNumber" value="${dvHasExactlyOnePayment ? '&nbsp;' : (status.index + 1)}"/>
                        <tr class="${status.index % 2 == 0 ? 'highlight' : ''}">
                            <td>${lineNumber}</td>
                            <td>
                                <kul:htmlControlAttribute
                                    attributeEntry="${paymentDetailAttributes['paymentGroup.disbursementType.name']}"
                                    property="dvPaymentDetails[${status.index}].paymentGroup.disbursementType.name"
                                    readOnly="true"/>
                            </td>
                            <td>
                                <kul:htmlControlAttribute
                                    attributeEntry="${paymentDetailAttributes['paymentGroup.disbursementDate']}"
                                    property="dvPaymentDetails[${status.index}].paymentGroup.disbursementDate"
                                    readOnly="true"/>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty dvPaymentDetails}">
                        <tr>
                            <td>&nbsp;</td>
                            <td colspan="2">No Payment Information Available</td>
                        </tr>
                    </c:if>
                </table>
            </c:when>
            <c:otherwise>
                <table class="datatable standard side-margins" summary="DV PDP Status">
                    <tr>
                        <th>No Payment Information Available</th>
                    </tr>
                </table>
            </c:otherwise>
        </c:choose>
        
    </div>
</kul:tab>
