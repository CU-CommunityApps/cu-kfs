<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2024 Kuali, Inc.

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

<c:set var="journalVoucherAttributes" value="${DataDictionary.LaborJournalVoucherDocument.attributes}"/>
<c:set var="readOnly" value="${empty KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>

<kul:documentPage showDocumentInfo="true"
                  documentTypeName="LaborJournalVoucherDocument"
                  htmlFormAction="laborLaborJournalVoucher" renderMultipart="true"
                  showTabButtons="true">

    <sys:documentOverview editingMode="${KualiForm.editingMode}"/>

    <!-- LABOR JOURNAL VOUCHER SPECIFIC FIELDS -->
    <kul:tab tabTitle="Labor Distribution Journal Voucher Details" defaultOpen="true"
             tabErrorKey="${KFSConstants.EDIT_JOURNAL_VOUCHER_ERRORS}">
        <div class="tab-container" align="center">

            <table cellpadding=0 class="standard" summary="Labor Distribution Journal Voucher Details">
                <tbody>
                <tr>
                    <th width="35%" class="bord-l-b">
                        <div align="right"><kul:htmlAttributeLabel
                                labelFor="selectedAccountingPeriod" attributeEntry="${journalVoucherAttributes.accountingPeriod}"
                                useShortLabel="false"/></div>
                    </th>
                    <td class="datacell-nowrap">
                        <c:if test="${readOnly}">
                            ${KualiForm.accountingPeriod.universityFiscalPeriodName}
                        </c:if>
                        <c:if test="${!readOnly}">
                            <SCRIPT type="text/javascript">
                                function submitForChangedAccountingPeriod() {
                                    document.forms[0].submit();
                                }
                            </SCRIPT>
                            <c:choose>
                                <c:when test="${KualiForm.selectedBalanceType.code eq KFSConstants.BALANCE_TYPE_A21}">
                                    <c:set var="selectedUniversityFiscalPeriodCode" value="${fn:substring(KualiForm.selectedAccountingPeriod, 0, 2)}" />
                                    <c:set var="selectedUniversityFiscalYear" value="${fn:substring(KualiForm.selectedAccountingPeriod, 2, 6)}" />
                                    <c:out value="${KualiForm.financialDocument.accountingPeriod.universityFiscalPeriodName}"/>
                                    <kul:lookup
                                            boClassName="org.kuali.kfs.coa.businessobject.AccountingPeriod"
                                            fieldConversions="universityFiscalPeriodCode:universityFiscalPeriodCode,universityFiscalYear:universityFiscalYear"
                                            lookupParameters="'${selectedUniversityFiscalPeriodCode}':universityFiscalPeriodCode,'${selectedUniversityFiscalYear}':universityFiscalYear"
                                            fieldLabel="${journalVoucherAttributes.accountingPeriod.label}"/>
                                </c:when>
                                <c:otherwise>
                                    <html:select property="selectedAccountingPeriod" onchange="submitForChangedAccountingPeriod()">
                                        <c:forEach items="${KualiForm.accountingPeriods}" var="accountingPeriod">
                                            <c:set var="accountingPeriodCompositeValue" value="${accountingPeriod.universityFiscalPeriodCode}${accountingPeriod.universityFiscalYear}"/>
                                            <html:option value="${accountingPeriodCompositeValue}">${accountingPeriod.universityFiscalPeriodName}</html:option>
                                        </c:forEach>
                                    </html:select>
                                </c:otherwise>
                            </c:choose>
                        </c:if></td>
                </tr>
                <tr>
                    <th width="35%" class="bord-l-b">
                        <div align="right"><kul:htmlAttributeLabel
                                labelFor="" attributeEntry="${journalVoucherAttributes.balanceTypeCode}"
                                useShortLabel="false"/></div>
                    </th>
                    <td class="datacell-nowrap">
                        <c:if test="${readOnly}">
                            ${KualiForm.selectedBalanceType.financialBalanceTypeName}
                        </c:if>

                        <c:if test="${!readOnly}">
                            <%-- CU Customization: Modify onchange handling for balance type to also set methodToCall. --%>
                            <input type="hidden" id="balanceTypeHelper" name="balanceTypeHelper" value="placeholder"/>
                            <SCRIPT type="text/javascript">
                                function submitForChangedBalanceType() {
                                    document.forms[0].balanceTypeHelper.value = "methodToCall.changeBalanceType";
                                    document.forms[0].balanceTypeHelper.name = "methodToCall.changeBalanceType";
                                    document.forms[0].submit();
                                }
                            </SCRIPT>
                            <%-- End CU Customization --%>
                            <html:select property="selectedBalanceType.code" onchange="submitForChangedBalanceType()">
                                <c:forEach items="${KualiForm.balanceTypes}" var="balanceType">
                                    <html:option value="${balanceType.code}">
                                        <c:out value="${balanceType.codeAndDescription}"/>
                                    </html:option>
                                </c:forEach>
                            </html:select>
                            <kul:lookup
                                    boClassName="org.kuali.kfs.coa.businessobject.BalanceType"
                                    fieldConversions="code:selectedBalanceType.code"
                                    lookupParameters="selectedBalanceType.code:code"
                                    fieldLabel="${journalVoucherAttributes.balanceTypeCode.label}"/>
                        </c:if></td>
                </tr>
                <tr>
                    <kul:htmlAttributeHeaderCell
                            attributeEntry="${journalVoucherAttributes.offsetTypeCode}"
                            horizontal="true" width="35%" addClass="right"/>
                    <td class="datacell-nowrap"><kul:htmlControlAttribute
                            attributeEntry="${journalVoucherAttributes.offsetTypeCode}"
                            property="document.offsetTypeCode"
                            readOnly="${readOnly}"/></td>
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
            <sys-java:accountingLineGroup newLinePropertyName="newSourceLine" collectionPropertyName="document.sourceAccountingLines" collectionItemPropertyName="document.sourceAccountingLine" attributeGroupName="${attributeGroupName}"/>
        </sys-java:accountingLines>
    </kul:tab>

    <ld:laborLedgerPendingEntries/>
    <kul:notes/>
    <kul:adHocRecipients/>
    <kul:routeLog/>
    <kul:superUserActions/>
    <sys:documentControls transactionalDocument="true" extraButtons="${KualiForm.extraButtons}"/>
</kul:documentPage>
