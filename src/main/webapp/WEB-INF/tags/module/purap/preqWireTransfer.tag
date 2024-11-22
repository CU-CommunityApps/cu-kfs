<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<c:set var="isOpen" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])
                               && KualiForm.document.paymentMethodCode == 'W'}" />
<c:set var="wireTransAttributes" value="${DataDictionary.PaymentSourceWireTransfer.attributes}" />

<kul:tab tabTitle="Wire Transfer" defaultOpen="${isOpen}" tabErrorKey="${KFSConstants.PREQ_WIRETRANSFER_TAB_ERRORS}">
    <div class="tab-container" align=center >
	    <table cellpadding=0 class="datatable standard" summary="Wire Transfer Section">
            <tbody>
                <tr>
                    <td colspan="4" align="left" valign="middle" class="datacell"><bean:write name="KualiForm" property="wireChargeMessage" /></td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqAutomatedClearingHouseProfileNumber}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.automatedClearingHouseProfileNumber}" property="document.preqWireTransfer.preqAutomatedClearingHouseProfileNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.wireTransferFeeWaiverIndicator}"/> </div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqWireTransferFeeWaiverIndicator}" property="document.preqWireTransfer.preqWireTransferFeeWaiverIndicator" readOnly="${!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankName}" property="document.preqWireTransfer.preqBankName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqAdditionalWireText}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqAdditionalWireText}" property="document.preqWireTransfer.preqAdditionalWireText" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b">
                        <div align="right">
                            <kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankRoutingNumber}"/>
                            <c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}"><br/> *required for US bank</c:if>
                        </div>
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankRoutingNumber}" property="document.preqWireTransfer.preqBankRoutingNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqAttentionLineText}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqAttentionLineText}" property="document.preqWireTransfer.preqAttentionLineText" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b" >
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankStreetAddress}"/></div>
                    </th>
                    <td class="datacell" >
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankStreetAddress}" property="document.preqWireTransfer.preqBankStreetAddress" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <%-- can't make this field required in DD; so add '*' here --%>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankCityName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankCityName}" property="document.preqWireTransfer.preqBankCityName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqCurrencyTypeCode}"/></div></th>
                    <c:set var="amountHint">
                        <bean:message key="label.wireTransfer.amount.hint"/>
                    </c:set>
                    <td class="datacell">
                      <kul:htmlControlAttribute accessibilityHint="${amountHint}" attributeEntry="${wireTransAttributes.preqCurrencyTypeCode}" property="document.preqWireTransfer.preqCurrencyTypeCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b">
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankStateCode}"/>
                            <c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}"><br/> *required for US bank</c:if>
                        </div>
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankStateCode}" property="document.preqWireTransfer.preqBankStateCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqCurrencyTypeName}"/></div></th>
                    <td class="datacell" colspan="3">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqCurrencyTypeName}" property="document.preqWireTransfer.preqCurrencyTypeName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankProvince}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankProvince}" property="document.preqWireTransfer.preqBankProvince" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankCountryCode}"/></div></th>
                    <td class="datacell" colspan="3">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankCountryCode}" property="document.preqWireTransfer.preqBankCountryCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqPayeeAccountNumber}"/></div></th>
                    <td class="datacell" colspan="3">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqPayeeAccountNumber}" property="document.preqWireTransfer.preqPayeeAccountNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}" />
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqPayeeAccountName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqPayeeAccountName}" property="document.preqWireTransfer.preqPayeeAccountName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
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
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankIBAN}"/></div></th>
                    <c:set var="bankIBANHint">
        			    <bean:message key="label.wireTransfer.bankIBAN.hint"/>
                    </c:set>
                    <td class="datacell">
                        <kul:htmlControlAttribute accessibilityHint="${bankIBANHint}" attributeEntry="${wireTransAttributes.preqBankIBAN}" property="document.preqWireTransfer.preqBankIBAN" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqCorrespondentBankName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqCorrespondentBankName}" property="document.preqWireTransfer.preqCorrespondentBankName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankSwiftCode}"/></div></th>
                    <c:set var="bankSwiftCode">
        			    <bean:message key="label.wireTransfer.bankSwiftCode.hint"/>
                    </c:set>
                    <td class="datacell">
                        <kul:htmlControlAttribute accessibilityHint="${bankSwiftCode}" attributeEntry="${wireTransAttributes.preqBankSwiftCode}" property="document.preqWireTransfer.preqBankSwiftCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqCorrespondentBankAddress}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqCorrespondentBankAddress}" property="document.preqWireTransfer.preqCorrespondentBankAddress" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqSortOrTransitCode}"/></div></th>
                    <c:set var="sortOrTransitCodeHint">
        			    <bean:message key="label.wireTransfer.sortOrTransitCode.hint"/>
                    </c:set>
                    <td class="datacell">
                        <kul:htmlControlAttribute accessibilityHint="${sortOrTransitCodeHint}" attributeEntry="${wireTransAttributes.preqSortOrTransitCode}" property="document.preqWireTransfer.preqSortOrTransitCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqCorrespondentBankSwiftCode}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqCorrespondentBankSwiftCode}" property="document.preqWireTransfer.preqCorrespondentBankSwiftCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b" colspan="2"><div align="right">&nbsp;</div></th>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqCorrespondentBankRoutingNumber}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqCorrespondentBankRoutingNumber}" property="document.preqWireTransfer.preqCorrespondentBankRoutingNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <td class="datacell" colspan="2">
                        <b><bean:message key="message.wiretransfer.use.IBAN"/></b>
                    </td>
                    <th scope=row class="bord-l-b" ><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqCorrespondentBankAccountNumber}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqCorrespondentBankAccountNumber}" property="document.preqWireTransfer.preqCorrespondentBankAccountNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</kul:tab>
