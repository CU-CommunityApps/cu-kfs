<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="wireTransAttributes" value="${DataDictionary.PaymentSourceWireTransfer.attributes}" />

<kul:tab tabTitle="Foreign Draft" defaultOpen="false" tabErrorKey="${CUKFSConstants.PREQ_FOREIGNDRAFTS_TAB_ERRORS}">
    <div class="tab-container" align=center>
        <table class="datatable standard side-margins" summary="Foreign Draft Section" cellpadding="0">
            <tbody>
                <c:if test="${!fullEntryMode&&!frnEntryMode}">
                    <tr>
                        <td>
                            <c:if test="${KualiForm.document.wireTransfer.foreignCurrencyTypeCode=='C'}">
                                PREQ amount is stated in U.S. dollars; convert to foreign currency
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <c:if test="${KualiForm.document.wireTransfer.foreignCurrencyTypeCode=='F'}">
                                PREQ amount is stated in foreign currency
                            </c:if>
                        </td>
                    </tr>
                </c:if>
                <c:if test="${fullEntryMode||frnEntryMode}">
                    <tr>
                        <td>
                            <html:radio
                                    styleId="us-currency"
                                    property="document.wireTransfer.foreignCurrencyTypeCode"
                                    value="C"/>

                            <label for="us-currency">
                                PREQ amount is stated in U.S. dollars; convert to foreign currency
                            </label>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <html:radio
                                    styleId="foreign-currency"
                                    property="document.wireTransfer.foreignCurrencyTypeCode"
                                    value="F"/>
                            <label for="foreign-currency">
                                PREQ amount is stated in foreign currency
                            </label>
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <td>
                        *<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.currencyTypeName}"/>&nbsp;
                        <kul:htmlControlAttribute
                                attributeEntry="${wireTransAttributes.currencyTypeName}"
                                property="document.wireTransfer.foreignCurrencyTypeName"
                                readOnly="${!fullEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</kul:tab>
