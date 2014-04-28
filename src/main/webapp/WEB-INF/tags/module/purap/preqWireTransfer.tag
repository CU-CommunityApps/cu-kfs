<%--
 Copyright 2007-2009 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<c:set var="isOpen" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFiscalEntry'])
                               && KualiForm.document.paymentMethodCode == 'W'}" />

<kul:tab tabTitle="Wire Transfer" defaultOpen="${isOpen}" tabErrorKey="${KFSConstants.PREQ_WIRETRANSFER_TAB_ERRORS}">
	<c:set var="wireTransAttributes" value="${DataDictionary.PaymentRequestWireTransfer.attributes}" />
    <div class="tab-container" align=center > 
    <h3>Wire Transfer</h3>
	<table cellpadding=0 class="datatable" summary="Wire Transfer Section">
            
               <tr>
                <td colspan=4 align=left valign=middle class="datacell"><bean:write name="KualiForm" property="wireChargeMessage" /></td>
              </tr>
            
               <tr>
                <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqAutomatedClearingHouseProfileNumber}"/></div></th>
                <td class="datacell">
                  <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqAutomatedClearingHouseProfileNumber}" property="document.preqWireTransfer.preqAutomatedClearingHouseProfileNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                </td>
                <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqWireTransferFeeWaiverIndicator}"/> </div></th>
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
                <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankRoutingNumber}"/>
                    <c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}">
                        <br> *required for US bank
                    </c:if>
                </div></th>
                <td class="datacell">
                  <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankRoutingNumber}" property="document.preqWireTransfer.preqBankRoutingNumber" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                </td>
                <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqAttentionLineText}"/></div></th>
                <td class="datacell">
                  <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqAttentionLineText}" property="document.preqWireTransfer.preqAttentionLineText" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                </td>
              </tr>
              
              <tr>
              <%-- can't make this field required in DD; so add '*' here --%>
                <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankCityName}"/></div></th>
                <td class="datacell">
                  <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankCityName}" property="document.preqWireTransfer.preqBankCityName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                </td>
                <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqCurrencyTypeCode}"/></div></th>
                <td class="datacell">
                  <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqCurrencyTypeCode}" property="document.preqWireTransfer.preqCurrencyTypeCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                </td>
              </tr>
              
              <tr>
                <th scope=row class="bord-l-b"><div align="right"><kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqBankStateCode}"/>
                    <c:if test="${fullEntryMode||wireEntryMode||frnEntryMode}">
                        <br> *required for US bank
                    </c:if>
                </div></th>
                <td class="datacell">
                  <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqBankStateCode}" property="document.preqWireTransfer.preqBankStateCode" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
                </td>
                <th scope=row class="bord-l-b"><div align="right">*<kul:htmlAttributeLabel attributeEntry="${wireTransAttributes.preqCurrencyTypeName}"/></div></th>
                <td class="datacell" colspan="3">
                  <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqCurrencyTypeName}" property="document.preqWireTransfer.preqCurrencyTypeName" readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}"/>
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
                  <kul:htmlControlAttribute attributeEntry="${wireTransAttributes.preqPayeeAccountNumber}" property="document.preqWireTransfer.preqPayeeAccountNumber"
                   readOnly="${!fullEntryMode&&!wireEntryMode&&!frnEntryMode}" /> 
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
            </tbody>
          </table>
        </div>
</kul:tab>
