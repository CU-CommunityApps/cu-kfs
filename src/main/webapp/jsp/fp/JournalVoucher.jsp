<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2019 Kuali, Inc.

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
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="journalVoucherAttributes"
       value="${DataDictionary['JournalVoucherDocument'].attributes}"/>
<c:set var="readOnly"
       value="${!KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}"/>


<c:set var="documentTypeName" value="JournalVoucherDocument"/>
<c:set var="htmlFormAction" value="financialJournalVoucher"/>

<c:if test="${isYearEnd}">
    <c:set var="documentTypeName" value="YearEndJournalVoucherDocument"/>
    <c:set var="htmlFormAction" value="financialYearEndJournalVoucher"/>
</c:if>

<kul:documentPage showDocumentInfo="true"
                  documentTypeName="${documentTypeName}"
                  htmlFormAction="${htmlFormAction}" renderMultipart="true"
                  showTabButtons="true">
    <sys:hiddenDocumentFields/>
    <sys:documentOverview editingMode="${KualiForm.editingMode}"/>

    <kul:tab tabTitle="Journal Voucher Details" defaultOpen="true"
             tabErrorKey="${KFSConstants.EDIT_JOURNAL_VOUCHER_ERRORS}">
        <div class="tab-container" align=center>
            <table cellpadding=0 class="datatable" summary="Journal Voucher Details">
                <tbody>

                <tr>
                    <th width="35%" class="right">
                        <kul:htmlAttributeLabel
                                labelFor="selectedAccountingPeriod"
                                attributeEntry="${journalVoucherAttributes.accountingPeriod}"
                                useShortLabel="false"/>
                    </th>
                    <td class="datacell-nowrap">
                        <c:if test="${readOnly or isYearEnd}">
                            ${KualiForm.accountingPeriod.universityFiscalPeriodName}
                            <html:hidden property="selectedAccountingPeriod"/>
                        </c:if>
                        <c:if test="${!readOnly && !isYearEnd}">
                            <SCRIPT type="text/javascript">
                                <!--
                                function submitForChangedAccountingPeriod() {
                                    document.forms[0].submit();
                                }
                                //-->
                            </SCRIPT>
                            <html:select property="selectedAccountingPeriod"
                                         onchange="submitForChangedAccountingPeriod()">
                                <c:forEach items="${KualiForm.accountingPeriods}" var="accountingPeriod">
                                    <c:set var="accountingPeriodCompositeValue"
                                           value="${accountingPeriod.universityFiscalPeriodCode}${accountingPeriod.universityFiscalYear}"/>
                                    <html:option value="${accountingPeriodCompositeValue}"><c:out
                                            value="${accountingPeriod.universityFiscalPeriodName}"/></html:option>
                                </c:forEach>
                            </html:select>

                            <NOSCRIPT><html:submit value="refresh"
                                                   title="press this button to refresh the page after changing the accounting period"
                                                   alt="press this button to refresh the page after changing the accounting period"/>
                            </NOSCRIPT>
                        </c:if></td>
                </tr>
                <tr>
                    <th width="35%" class="right">
                        <kul:htmlAttributeLabel
                                labelFor="" attributeEntry="${journalVoucherAttributes.balanceTypeCode}"
                                useShortLabel="false"/>
                    </th>
                    <td class="datacell-nowrap">
                        <c:if test="${readOnly or isYearEnd}">
                            ${KualiForm.selectedBalanceType.financialBalanceTypeName}
                        </c:if>

                        <c:if test="${!readOnly && !isYearEnd}">
                            <SCRIPT type="text/javascript">
                                <!--
                                function submitForChangedBalanceType() {
                                    document.forms[0].submit();
                                }
                                //-->
                            </SCRIPT>
                            <html:select property="selectedBalanceType.code" onchange="submitForChangedBalanceType()">
                                <c:forEach items="${KualiForm.balanceTypes}" var="balanceType">
                                    <html:option value="${balanceType.code}">
                                        <c:out value="${balanceType.codeAndDescription}"/>
                                    </html:option>
                                </c:forEach>
                            </html:select>
                            <NOSCRIPT><html:submit value="refresh"
                                                   title="press this button to refresh the page after changing the balance type"
                                                   alt="press this button to refresh the page after changing the balance type"/>
                            </NOSCRIPT>
                            <kul:lookup
                                    boClassName="org.kuali.kfs.coa.businessobject.BalanceType"
                                    fieldConversions="code:selectedBalanceType.code"
                                    lookupParameters="selectedBalanceType.code:code"
                                    fieldLabel="${journalVoucherAttributes.balanceTypeCode.label}"/>
                        </c:if></td>
                </tr>
                <tr>
                    <kul:htmlAttributeHeaderCell
                            attributeEntry="${journalVoucherAttributes.reversalDate}"
                            horizontal="true"
                            width="35%"
                            addClass="right"/>
                    <td class="datacell-nowrap"><kul:htmlControlAttribute
                            attributeEntry="${journalVoucherAttributes.reversalDate}"
                            datePicker="true"
                            property="document.reversalDate"
                            readOnly="${readOnly}"
                            readOnlyAlternateDisplay="${fn:escapeXml(KualiForm.formattedReversalDate)}"
                    /></td>
                </tr>
                </tbody>
            </table>
        </div>
    </kul:tab>

    <c:set var="isEncumbrance" value="${KualiForm.isEncumbranceBalanceType}"/>
    <c:set var="isDebitCreditAmount" value="${KualiForm.selectedBalanceType.financialOffsetGenerationIndicator}"/>

    <c:choose>
        <c:when test="${isEncumbrance && isDebitCreditAmount}">
            <c:set var="attributeGroupName" value="source-withDebitCreditEncumbrance"/>
        </c:when>
        <c:when test="${!isEncumbrance && isDebitCreditAmount}">
            <c:set var="attributeGroupName" value="source-withDebitCredit"/>
        </c:when>
        <c:when test="${isEncumbrance && !isDebitCreditAmount}">
            <c:set var="attributeGroupName" value="source-withEncumbrance"/>
        </c:when>
        <c:otherwise>
            <c:set var="attributeGroupName" value="source"/>
        </c:otherwise>
    </c:choose>

    <kul:tab tabTitle="Accounting Lines" defaultOpen="true" tabErrorKey="${KFSConstants.NEW_SOURCE_LINE_ERRORS}"
             helpUrl="${KualiForm.accountingLineImportInstructionsUrl}" helpLabel="Import Templates">
        <sys-java:accountingLines>
            <sys-java:accountingLineGroup newLinePropertyName="newSourceLine"
                                          collectionPropertyName="document.sourceAccountingLines"
                                          collectionItemPropertyName="document.sourceAccountingLine"
                                          attributeGroupName="${attributeGroupName}"/>
        </sys-java:accountingLines>
    </kul:tab>

    <gl:generalLedgerPendingEntries/>
    <kul:notes/>
    <kul:adHocRecipients/>
    <kul:routeLog/>
    <kul:superUserActions/>
    <sys:documentControls transactionalDocument="true" extraButtons="${KualiForm.extraButtons}"/>
</kul:documentPage>
