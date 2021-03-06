<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="wireTransAttributes" value="${DataDictionary.CreditMemoWireTransfer.attributes}" />

<kul:tab tabTitle="Foreign Draft" defaultOpen="false" tabErrorKey="${CUKFSConstants.CM_FOREIGNDRAFTS_TAB_ERRORS}">
    <div class="tab-container" align=center>
        <table class="datatable standard side-margins" summary="Foreign Draft Section" cellpadding="0">
            <tbody>
                <c:if test="${!fullEntryMode&&!frnEntryMode}">
                    <tr>
                        <td>
                            <c:if test="${KualiForm.document.cmWireTransfer.cmForeignCurrencyTypeCode=='C'}">
                                CM amount is stated in U.S. dollars; convert to foreign currency
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <c:if test="${KualiForm.document.cmWireTransfer.cmForeignCurrencyTypeCode=='F'}">
                                CM amount is stated in foreign currency
                            </c:if>
                        </td>
                    </tr>
                </c:if>
                <c:if test="${fullEntryMode||frnEntryMode}">
                    <tr>
                        <td>
                            <html:radio
                                    styleId="us-currency"
                                    property="document.cmWireTransfer.cmForeignCurrencyTypeCode"
                                    value="C"/>

                            <label for="us-currency">
                                CM amount is stated in U.S. dollars; convert to foreign currency
                            </label>

                        </td>
                    </tr>
                    <tr>
                        <td>
                            <html:radio
                                    styleId="foreign-currency"
                                    property="document.cmWireTransfer.cmForeignCurrencyTypeCode"
                                    value="F"/>
                            <label for="foreign-currency">
                                CM amount is stated in foreign currency
                            </label>
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <td>
                        *<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmCurrencyTypeName}"/>
                        <kul:htmlControlAttribute
                                attributeEntry="${wireTransAttributes.cmCurrencyTypeName}"
                                property="document.cmWireTransfer.cmForeignCurrencyTypeName"
                                readOnly="${!fullEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</kul:tab>
