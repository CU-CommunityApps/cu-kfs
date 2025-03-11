<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2023 Kuali, Inc.

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

<%-- Cornell Customization. KualiCo 2023-04-19 sys/innerWireTransfer.tag used.--%> 

<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="wireTransAttributes" value="${DataDictionary.PaymentSourceWireTransfer.attributes}" />
<%-- Cornell Customization --%>
<c:set var="wireTransExtendedAttributes" value="${DataDictionary.PaymentSourceWireTransferExtendedAttribute.attributes}" />

<table cellpadding=0 class="datatable standard" summary="Wire Transfer Section">
	<tbody>
		<tr>
			<td colspan="4" align="left" valign="middle" class="datacell">
                <c:if test="${not isPaymentRequestDocument}">
                    <bean:write name="KualiForm" property="wireChargeMessage" />
                </c:if>
            </td>
		</tr>
		<tr>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.automatedClearingHouseProfileNumber}"/></div></th>
			<td class="datacell">
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.automatedClearingHouseProfileNumber}" property="document.wireTransfer.automatedClearingHouseProfileNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
            <c:if test="${not isPaymentRequestDocument}">
                <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.wireTransferFeeWaiverIndicator}"/> </div></th>
                <td class="datacell">
                    <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.wireTransferFeeWaiverIndicator}" property="document.wireTransfer.wireTransferFeeWaiverIndicator" readOnly="${!wireEntryMode&&!frnEntryMode}"/>
                </td>
            </c:if>
		</tr>
		<tr>
            <%-- Cornell Customization: Added * due to inability to make field required. --%>
            <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankName}" forceRequired="true"/></div></th>
			<td class="datacell">
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankName}" forceRequired="true" property="document.wireTransfer.bankName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.additionalWireText}"/></div></th>
			<td class="datacell">
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.additionalWireText}" property="document.wireTransfer.additionalWireText" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
		</tr>
		<tr>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankRoutingNumber}"/>
				<c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}"><br/> *required for US bank</c:if>
			</th>
			<td class="datacell">
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankRoutingNumber}" property="document.wireTransfer.bankRoutingNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.attentionLineText}"/></div></th>
			<td class="datacell">
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.attentionLineText}" property="document.wireTransfer.attentionLineText" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
		</tr>
        <%-- Cornell Customization: Start : Added extended attribute --%>
        <tr> 
            <th scope=row class="bord-l-b" >
                <div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.bankStreetAddress}"/></div>
            </th>
            <td class="datacell" >
                <kul:htmlControlAttribute attributeEntry="${wireTransExtendedAttributes.bankStreetAddress}" property="document.wireTransfer.extension.bankStreetAddress" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
            </td>
        </tr>
        <%-- Cornell Customization: End : Added extended attribute --%>
		<tr>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankCityName}" forceRequired="true"/></div></th>
			<td class="datacell">
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankCityName}" forceRequired="true" property="document.wireTransfer.bankCityName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.currencyTypeCode}" forceRequired="true"/></div></th>
                <%-- Cornell Customization : Start : Added hint --%>
                <c:set var="amountHint">
                    <bean:message key="label.wireTransfer.amount.hint"/>
                </c:set>
            <td class="datacell">
                <kul:htmlControlAttribute accessibilityHint="${amountHint}" attributeEntry="${wireTransAttributes.currencyTypeCode}" forceRequired="true" property="document.wireTransfer.currencyTypeCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                <%-- Cornell Customization : End : Added hint --%>
			</td>
		</tr>
		<tr>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankStateCode}"/>
				<c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}"><br/> *required for US bank</c:if>
			</th>
			<td class="datacell">
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankStateCode}" property="document.wireTransfer.bankStateCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
            <%-- Cornell Customization : Added "rowspan=2" to both heading and data currencyTypeName tags --%>
            <th scope=row class="bord-l-b" rowspan=2><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.currencyTypeName}" forceRequired="true"/></div></th>
            <td class="datacell" colspan="3" rowspan=2>
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.currencyTypeName}" forceRequired="true" property="document.wireTransfer.currencyTypeName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
		</tr>
        <%-- Cornell Customization : Start : Added extended attribute --%>
        <tr>
            <th scope=row class="bord-l-b">
             	<div align="right">
               		<kul:htmlAttributeLabel attributeEntry="${wireTransExtendedAttributes.bankProvince}"/>
               	</div>
            </th>
            <td class="datacell">
              <kul:htmlControlAttribute attributeEntry="${wireTransExtendedAttributes.bankProvince}" property="document.wireTransfer.extension.bankProvince" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
            </td>
        </tr>
        <%-- Cornell Customization : End : Added extended attribute --%>
		<tr>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.bankCountryCode}" forceRequired="true"/></div></th>
			<td class="datacell" colspan="3">
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.bankCountryCode}" forceRequired="true" property="document.wireTransfer.bankCountryCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
		</tr>
		<tr>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.payeeAccountNumber}" forceRequired="true"/></div></th>
			<td class="datacell" colspan="3">
                <c:set var="accountNumberReadOnly"
                       value="${
                    not fullEntryMode
                    and not wireEntryMode
                    and not frnEntryMode
                }"/>
                <c:set var="mask"
                       value="${
                    accountNumberReadOnly
                    or (
                        not KualiForm.document.documentHeader.workflowDocument.initiated
                        and not KualiForm.document.documentHeader.workflowDocument.saved
                        and not wireEntryMode
                    )
                }"/>
				<kul:htmlControlAttribute mask="${mask}"
                                          attributeEntry="${wireTransAttributes.payeeAccountNumber}"
                                          forceRequired="true" property="document.wireTransfer.payeeAccountNumber"
                                          readOnly="${accountNumberReadOnly}" />
			</td>
		</tr>
		<tr>
			<th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.payeeAccountName}" forceRequired="true"/></div></th>
			<td class="datacell">
				<kul:htmlControlAttribute attributeEntry="${wireTransAttributes.payeeAccountName}" forceRequired="true" property="document.wireTransfer.payeeAccountName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
			</td>
			<td class="datacell" colspan="2"><bean:message key="message.wiretransfer.fee"/></td>
		</tr>
        <%-- Cornell Customization - Start : Added extended attributes --%>
        <tr>
            <td colspan=4 valign=middle class="tab-subhead"><b><bean:message key="message.wiretransfer.foreign.wires.additional.data"/></b></td>
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
        <%-- Cornell Customization - End : Added extended attributes --%>
	</tbody>
</table>