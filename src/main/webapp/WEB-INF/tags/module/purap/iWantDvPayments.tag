<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="paymentDetailAttributes" value="${DataDictionary.PurchasingPaymentDetail.attributes}"/>
<c:set var="dvPaymentDetails" value="${KualiForm.document.dvPaymentDetails}"/>

<kul:tab tabTitle="Disbursement Voucher Payments" defaultOpen="false">
    <div class="tab-container">
        <h3>DV Payment Information</h3>
        <table class="datatable standard side-margins" summary="Payments">
            <c:choose>
                <c:when test="${!(empty dvPaymentDetails)}">
                    <tr class="header">
                        <kul:htmlAttributeHeaderCell literalLabel="&nbsp;" scope="col" align="left"/>
                        <kul:htmlAttributeHeaderCell attributeEntry="${paymentDetailAttributes.aaa}"
                                hideRequiredAsterisk="${true}" scope="col" align="left"/>
                    </tr>
                    <c:forEach var="payment" items="${dvPaymentDetails}" varStatus="status">
                        <tr class="${status.index % 2 == 0 ? 'highlight' : ''}">
                            <td>${status.index + 1}</td>
                            <td><c:out value="${payment.aaa}"/></td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr>
                        <th>No Payment Information Available</th>
                    </tr>
                </c:otherwise>
            </c:choose>
        </table>
    </div>
</kul:tab>
