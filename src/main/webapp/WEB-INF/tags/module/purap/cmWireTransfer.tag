<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="wireTransAttributes" value="${DataDictionary.CreditMemoWireTransfer.attributes}" />

<kul:tab tabTitle="Wire Transfer" defaultOpen="false" tabErrorKey="${KFSConstants.CM_WIRETRANSFER_TAB_ERRORS}">
    <div class="tab-container" align="center">
	    <table cellpadding=0 class="datatable standard" summary="Wire Transfer Section">
            <tbody>
                <tr>
                    <td colspan="4" align="left" valign="middle" class="datacell"><bean:write name="KualiForm" property="wireChargeMessage" /></td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmAutomatedClearingHouseProfileNumber}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmAutomatedClearingHouseProfileNumber}" property="document.cmWireTransfer.cmAutomatedClearingHouseProfileNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmWireTransferFeeWaiverIndicator}"/> </div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmWireTransferFeeWaiverIndicator}" property="document.cmWireTransfer.cmWireTransferFeeWaiverIndicator" readOnly="${!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmBankName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmBankName}" property="document.cmWireTransfer.cmBankName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmAdditionalWireText}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmAdditionalWireText}" property="document.cmWireTransfer.cmAdditionalWireText" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b">
                        <div align="right">
                            <kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmBankRoutingNumber}"/>
                            <c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}"><br/> *required for US bank</c:if>
                        </div>
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmBankRoutingNumber}" property="document.cmWireTransfer.cmBankRoutingNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmAttentionLineText}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmAttentionLineText}" property="document.cmWireTransfer.cmAttentionLineText" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <%-- can't make this field required in DD; so add '*' here --%>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmBankCityName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmBankCityName}" property="document.cmWireTransfer.cmBankCityName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmCurrencyTypeCode}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmCurrencyTypeCode}" property="document.cmWireTransfer.cmCurrencyTypeCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b">
                        <div align="right">
                            <kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmBankStateCode}"/>
                            <c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}"><br/> *required for US bank</c:if>
                        </div>
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmBankStateCode}" property="document.cmWireTransfer.cmBankStateCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmCurrencyTypeName}"/></div></th>
                    <td class="datacell" colspan="3">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmCurrencyTypeName}" property="document.cmWireTransfer.cmCurrencyTypeName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmBankCountryCode}"/></div></th>
                    <td class="datacell" colspan="3">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmBankCountryCode}" property="document.cmWireTransfer.cmBankCountryCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmPayeeAccountNumber}"/></div></th>
                    <td class="datacell" colspan="3">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmPayeeAccountNumber}" property="document.cmWireTransfer.cmPayeeAccountNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}" />
                    </td>
                </tr>
                <tr>
                    <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.cmPayeeAccountName}"/></div></th>
                    <td class="datacell">
                        <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.cmPayeeAccountName}" property="document.cmWireTransfer.cmPayeeAccountName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                    </td>
                    <td class="datacell" colspan="2">
                        <bean:message key="message.wiretransfer.fee"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</kul:tab>
