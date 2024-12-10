<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<c:set var="isOpen" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])
                               && KualiForm.document.paymentMethodCode == 'W'}" />
<c:set var="wireTransAttributes" value="${DataDictionary.PaymentSourceWireTransfer.attributes}" />
<c:set var="wireTransExtendedAttributes" value="${DataDictionary.PaymentSourceWireTransferExtendedAttribute.attributes}" />

<kul:tab tabTitle="Wire Transfer" defaultOpen="${isOpen}" tabErrorKey="${CUKFSConstants.PREQ_WIRETRANSFER_TAB_ERRORS}">
    <div class="tab-container" align=center >
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
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqWireTransferFeeWaiverIndicator}" property="document.wireTransfer.wireTransferFeeWaiverIndicator" readOnly="${!wireEntryMode&&!frnEntryMode}"/>
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
                    <th scope=row class="bord-l-b" >
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.bankStreetAddress}"/></div>
                    </th>
                    <td class="datacell" >
                        <kul:htmlControlAttribute attributeEntry="${wireTransExtendedAttributes.bankStreetAddress}" property="document.wireTransfer.extension.bankStreetAddress" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <%-- can't make this field required in DD; so add '*' here --%>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankCityName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankCityName}" property="document.wireTransfer.bankCityName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.currencyTypeCode}"/></div></th>
                    <c:set var="amountHint">
                        <bean:message key="label.wireTransfer.amount.hint"/>
                    </c:set>
                    <td class="datacell">
                      <kul:htmlControlAttribute accessibilityHint="${amountHint}" attributeEntry="${wireTransAttributes.currencyTypeCode}" property="document.wireTransfer.currencyTypeCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b">
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankStateCode}"/>
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
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.extension.bankProvince}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransExtendedAttributes.bankProvince}" property="document.wireTransfer.extension.bankProvince" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
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
                <tr>
                    <td colspan="4" align="center" valign="middle" class="tab-subhead"><b><bean:message key="message.wiretransfer.foreign.wires.additional.data"/></b></td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b" colspan="2"><div align="right">&nbsp;</div></th>
                    <td class="datacell" colspan="2">
                        <b><bean:message key="message.wiretransfer.correspondent.bank"/></b>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.bankIBAN}"/></div></th>
                    <c:set var="bankIBANHint">
        			    <bean:message key="label.wireTransfer.bankIBAN.hint"/>
                    </c:set>
                    <td class="datacell">
                        <kul:htmlControlAttribute accessibilityHint="${bankIBANHint}" attributeEntry="${wireTransExtendedAttributes.bankIBAN}" property="document.wireTransfer.extension.bankIBAN" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.correspondentBankName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransExtendedAttributes.correspondentBankName}" property="document.wireTransfer.extension.correspondentBankName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.bankSwiftCode}"/></div></th>
                    <c:set var="bankSwiftCode">
        			    <bean:message key="label.wireTransfer.bankSwiftCode.hint"/>
                    </c:set>
                    <td class="datacell">
                        <kul:htmlControlAttribute accessibilityHint="${bankSwiftCode}" attributeEntry="${wireTransExtendedAttributes.bankSwiftCode}" property="document.wireTransfer.extension.bankSwiftCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.correspondentBankAddress}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransExtendedAttributes.correspondentBankAddress}" property="document.wireTransfer.extension.correspondentBankAddress" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.sortOrTransitCode}"/></div></th>
                    <c:set var="sortOrTransitCodeHint">
        			    <bean:message key="label.wireTransfer.sortOrTransitCode.hint"/>
                    </c:set>
                    <td class="datacell">
                        <kul:htmlControlAttribute accessibilityHint="${sortOrTransitCodeHint}" attributeEntry="${wireTransExtendedAttributes.sortOrTransitCode}" property="document.wireTransfer.extension.sortOrTransitCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.correspondentBankSwiftCode}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransExtendedAttributes.correspondentBankSwiftCode}" property="document.wireTransfer.extension.correspondentBankSwiftCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b" colspan="2"><div align="right">&nbsp;</div></th>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.correspondentBankRoutingNumber}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransExtendedAttributes.correspondentBankRoutingNumber}" property="document.wireTransfer.extension.correspondentBankRoutingNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <td class="datacell" colspan="2">
                        <b><bean:message key="message.wiretransfer.use.IBAN"/></b>
                    </td>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.correspondentBankAccountNumber}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransExtendedAttributes.correspondentBankAccountNumber}" property="document.wireTransfer.extension.correspondentBankAccountNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</kul:tab>
