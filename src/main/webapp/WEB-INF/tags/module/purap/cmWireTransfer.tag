<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="wireTransAttributes" value="${DataDictionary.PaymentSourceWireTransfer.attributes}" />

<kul:tab tabTitle="Wire Transfer" defaultOpen="false" tabErrorKey="${KFSConstants.CM_WIRETRANSFER_TAB_ERRORS}">
    <div class="tab-container" align="center">
	    <table cellpadding=0 class="datatable standard" summary="Wire Transfer Section">
            <tbody>
                <tr>
                    <td colspan="4" align="left" valign="middle" class="datacell"><bean:write name="KualiForm" property="wireChargeMessage" /></td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.automatedClearingHouseProfileNumber}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.automatedClearingHouseProfileNumber}" property="document.wireTransfer.automatedClearingHouseProfileNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.wireTransferFeeWaiverIndicator}"/> </div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.wireTransferFeeWaiverIndicator}" property="document.wireTransfer.wireTransferFeeWaiverIndicator" readOnly="${!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankName}" property="document.wireTransfer.bankName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.additionalWireText}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.additionalWireText}" property="document.wireTransfer.additionalWireText" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b">
                        <div align="right">
                            <kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankRoutingNumber}"/>
                            <c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}"><br/> *required for US bank</c:if>
                        </div>
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankRoutingNumber}" property="document.wireTransfer.bankRoutingNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.attentionLineText}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.attentionLineText}" property="document.wireTransfer.attentionLineText" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <%-- can't make this field required in DD; so add '*' here --%>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankCityName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankCityName}" property="document.wireTransfer.bankCityName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.currencyTypeCode}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.currencyTypeCode}" property="document.wireTransfer.currencyTypeCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b">
                        <div align="right">
                            <kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankStateCode}"/>
                            <c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}"><br/> *required for US bank</c:if>
                        </div>
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankStateCode}" property="document.wireTransfer.bankStateCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.currencyTypeName}"/></div></th>
                    <td class="datacell" colspan="3">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.currencyTypeName}" property="document.wireTransfer.currencyTypeName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankCountryCode}"/></div></th>
                    <td class="datacell" colspan="3">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankCountryCode}" property="document.wireTransfer.bankCountryCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.payeeAccountNumber}"/></div></th>
                    <td class="datacell" colspan="3">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.payeeAccountNumber}" property="document.wireTransfer.payeeAccountNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}" />
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.payeeAccountName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.payeeAccountName}" property="document.wireTransfer.payeeAccountName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <td class="datacell" colspan="2">
                        <bean:message key="message.wiretransfer.fee"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</kul:tab>
