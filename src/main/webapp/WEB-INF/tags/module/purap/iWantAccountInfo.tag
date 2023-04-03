<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for cash control document fields." %>
<%@ attribute name="wizard" required="false" %>

<c:set var="hasAccounts" value="${fn:length(KualiForm.document.accounts) > 0}"/>
<c:set var="accountAttributes" value="${DataDictionary.IWantAccount.attributes}"/>
<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>
<c:set var="accountsNbr" value="${fn:length(KualiForm.document.accounts)}"/>
<c:set var="nbrOfItems" value="${fn:length(KualiForm.document.items)}"/>
<c:set var="mainColumnCount" value="10"/>

<div class="tab-container" style="overflow:auto;">
    <h3>Account Information</h3>
    <table class="standard side-margins acct-lines" summary="Account Information">
        <tr>
            <td height="30" colspan="${mainColumnCount}" class="neutral"
                style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic">
                <b>NOTE:</b> If you do not know the account number, in College/Unit Routing and Approval (Step 4) enter the NetID 
                of the person in your college or department who can provide the account number and authorize the transaction.
            </td>
        </tr>
        <tr>
            <td colspan="${mainColumnCount}" class="center">
                <table border="0">
                    <tr>
                        <th class="right" width="50%">
                            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.accountDescriptionTxt}"/>
                        </th>
                        <td align="left" valign="middle" width="50%" class="neutral">
                            <kul:htmlControlAttribute
                                    attributeEntry="${documentAttributes.accountDescriptionTxt}"
                                    property="document.accountDescriptionTxt"
                                    readOnly="${not fullEntryMode}"
                                    tabindexOverride="${tabindexOverrideBase + 0}"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <th colspan="${mainColumnCount}" class="center">or</th>
        </tr>
        <c:if test="${fullEntryMode}">
            <%-- Render Favorite Accounts drop-down if the user has more than just the empty row in the values list. --%>
            <c:set var="favoriteAccountsFinder"
                   value="${fn:replace(documentAttributes.favoriteAccountLineIdentifier.control.valuesFinder, '.', '|')}"/>
            <c:if test="${not empty favoriteAccountsFinder}">
                <%-- Use the ActionFormUtilMap's custom method-invoking feature to retrieve the user's favorite accounts. --%>
                <c:set var="optionsMapMethodCallString"
                       value="getOptionsMap${KRADConstants.ACTION_FORM_UTIL_MAP_METHOD_PARM_DELIMITER}${favoriteAccountsFinder}"/>
                <c:set var="favoriteAccountsValues" value="${KualiForm.actionFormUtilMap[optionsMapMethodCallString]}"/>
                <c:if test="${not empty favoriteAccountsValues && fn:length(favoriteAccountsValues) > 1}">
                    <tr>
                        <td colspan="${mainColumnCount}" class="neutral">
                            <table border="0">
                                <tr>
                                    <th class="right" width="50%">
                                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.favoriteAccountLineIdentifier}"/>
                                    </th>
                                    <td align="left" valign="middle" width="50%" class="neutral">
                                        <kul:htmlControlAttribute
                                                attributeEntry="${documentAttributes.favoriteAccountLineIdentifier}"
                                                property="document.favoriteAccountLineIdentifier"
                                                tabindexOverride="${tabindexOverrideBase + 0}"/>
                                        <html:html-button
                                                property="methodToCall.addFavoriteAccount"
                                                alt="Add Favorite Account"
                                                title="Add Favorite Account"
                                                styleClass="btn btn-green skinny"
                                                tabindex="${tabindexOverrideBase + 0}"
                                                value="Add"
                                                innerHTML="<span class=\"fa fa-plus\"></span>"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <th colspan="${mainColumnCount}" class="center">or</th>
                    </tr>
                </c:if>
            </c:if>
            <tr class="header top">
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.chartOfAccountsCode}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.accountNumber}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.subAccountNumber}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.financialObjectCode}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.financialSubObjectCode}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.projectCode}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.organizationReferenceId}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.useAmountOrPercent}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.amountOrPercent}"/>
                <kul:htmlAttributeHeaderCell literalLabel="Action"/>
            </tr>
            <tr>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.chartOfAccountsCode}"
                            onchange="loadAccountName('newSourceLine.accountNumber', 'newSourceLine.chartOfAccountsCode', 'document.newSourceLine.accountNumber.name.div');"
                            property="newSourceLine.chartOfAccountsCode"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 1}"/>
                </td>
                <td valign="top" class="infoline nowrap">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.accountNumber}"
                            property="newSourceLine.accountNumber"
                            onblur="loadAccountName('newSourceLine.accountNumber', 'newSourceLine.chartOfAccountsCode', 'document.newSourceLine.accountNumber.name.div');"
                            readOnly="${not fullEntryMode}"/>
                    <c:if test="${ fullEntryMode && (not empty KualiForm.editingMode['iwntUseLookups'])}">
                        <kul:lookup
                                boClassName="org.kuali.kfs.coa.businessobject.Account"
                                fieldConversions="accountNumber:newSourceLine.accountNumber,chartOfAccountsCode:newSourceLine.chartOfAccountsCode"
                                lookupParameters="newSourceLine.accountNumber:accountNumber,newSourceLine.chartOfAccountsCode:chartOfAccountsCode"
                                addClass="embed"/>
                        <div id="document.newSourceLine.accountNumber.name.div" class="fineprint">
                            <kul:htmlControlAttribute
                                    attributeEntry="${accountAttributes.accountNumber}"
                                    property="newSourceLine.account.accountName"
                                    readOnly="true"/>
                        </div>
                    </c:if>
                </td>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.subAccountNumber}"
                            property="newSourceLine.subAccountNumber"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td valign="top" class="infoline nowrap">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.financialObjectCode}"
                            property="newSourceLine.financialObjectCode"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                    <c:if test="${ fullEntryMode && (not empty KualiForm.editingMode['iwntUseLookups'])}">
                        <kul:lookup
                                boClassName="org.kuali.kfs.coa.businessobject.ObjectCode"
                                fieldConversions="chartOfAccountsCode:newSourceLine.chartOfAccountsCode,financialObjectCode:newSourceLine.financialObjectCode"
                                lookupParameters="newSourceLine.chartOfAccountsCode:chartOfAccountsCode,newSourceLine.financialObjectCode:financialObjectCode"
                                addClass="embed"/>
                    </c:if>
                </td>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.financialSubObjectCode}"
                            property="newSourceLine.financialSubObjectCode"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.projectCode}"
                            property="newSourceLine.projectCode"
                            readOnly="false"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.organizationReferenceId}"
                            property="newSourceLine.organizationReferenceId"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.useAmountOrPercent}"
                            property="newSourceLine.useAmountOrPercent"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.amountOrPercent}"
                            property="newSourceLine.amountOrPercent"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td valign="top" class="infoline">
                    <html:html-button
                            property="methodToCall.addAccountingLine"
                            alt="Add an Account"
                            title="Add an Account"
                            styleClass="btn btn-green skinny"
                            tabindex="${tabindexOverrideBase + 0}"
                            value="Add"
                            innerHTML="<span class=\"fa fa-plus\"></span>"/>
                </td>
            </tr>
        </c:if>
        <tr class="title">
            <td colspan="${mainColumnCount}" style="padding-top: 20px;">
                <h3>Current Accounts</h3>
            </td>
        </tr>
        <c:if test="${hasAccounts}">
            <tr class="header top">
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.chartOfAccountsCode}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.accountNumber}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.subAccountNumber}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.financialObjectCode}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.financialSubObjectCode}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.projectCode}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.organizationReferenceId}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.useAmountOrPercent}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${accountAttributes.amountOrPercent}"/>
                <c:choose>
                    <c:when test="${fullEntryMode }">
                        <kul:htmlAttributeHeaderCell literalLabel="Action"/>
                    </c:when>
                    <c:otherwise>
                        <kul:htmlAttributeHeaderCell literalLabel="&nbsp;"/>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:if>
        <c:if test="${!hasAccounts}">
            <tr>
                <th height="30" colspan="${mainColumnCount}" class="neutral">No accounts added</th>
            </tr>
        </c:if>
        <logic:iterate indexId="ctr" name="KualiForm" property="document.accounts" id="accountLine">
            <tr>
                <td valign="top" class="infoline">
                    <div align="top">
                        <kul:htmlControlAttribute
                                attributeEntry="${accountAttributes.chartOfAccountsCode}"
                                property="document.account[${ctr}].chartOfAccountsCode"
                                onchange="loadAccountName('document.account[${ctr}].accountNumber', 'document.account[${ctr}].chartOfAccountsCode', 'document.account[${ctr}].accountNumber.name.div');"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}"/>
                    </div>
                </td>
                <td valign="top" class="infoline nowrap">
                    <div align="left">
                        <kul:htmlControlAttribute
                                attributeEntry="${accountAttributes.accountNumber}"
                                property="document.account[${ctr}].accountNumber"
                                onblur="loadAccountName('document.account[${ctr}].accountNumber', 'document.account[${ctr}].chartOfAccountsCode', 'document.account[${ctr}].accountNumber.name.div');"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}"/>
                        <c:if test="${fullEntryMode && (not empty KualiForm.editingMode['iwntUseLookups'])}">
                            <kul:lookup
                                    boClassName="org.kuali.kfs.coa.businessobject.Account"
                                    fieldConversions="accountNumber:document.account[${ctr}].accountNumber,chartOfAccountsCode:document.account[${ctr}].chartOfAccountsCode"
                                    lookupParameters="document.account[${ctr}].accountNumber:accountNumber,document.account[${ctr}].chartOfAccountsCode:chartOfAccountsCode"
                                    addClass="embed"/>
                            <div id="document.account[${ctr}].accountNumber.name.div" class="fineprint">
                                <kul:htmlControlAttribute
                                        attributeEntry="${accountAttributes.accountNumber}"
                                        property="document.account[${ctr}].account.accountName"
                                        readOnly="true"/>
                            </div>
                        </c:if>
                    </div>
                </td>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.subAccountNumber}"
                            property="document.account[${ctr}].subAccountNumber"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.financialObjectCode}"
                            property="document.account[${ctr}].financialObjectCode"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                    <c:if test="${fullEntryMode && (not empty KualiForm.editingMode['iwntUseLookups'])}">
                        <kul:lookup boClassName="org.kuali.kfs.coa.businessobject.ObjectCode"
                                    fieldConversions="chartOfAccountsCode:document.account[${ctr}].chartOfAccountsCode,financialObjectCode:document.account[${ctr}].financialObjectCode"
                                    lookupParameters="document.account[${ctr}].chartOfAccountsCode:chartOfAccountsCode,document.account[${ctr}].financialObjectCode:financialObjectCode"
                                    addClass="embed"/>
                    </c:if>
                </td>
                <td valign="top" class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${accountAttributes.financialSubObjectCode}"
                            property="document.account[${ctr}].financialSubObjectCode"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td valign="top" class="infoline">
                    <div align="left">
                        <kul:htmlControlAttribute
                                attributeEntry="${accountAttributes.projectCode}"
                                property="document.account[${ctr}].projectCode"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}"/>
                    </div>
                </td>
                <td valign="top" class="infoline">
                    <div align="left">
                        <kul:htmlControlAttribute
                                attributeEntry="${accountAttributes.organizationReferenceId}"
                                property="document.account[${ctr}].organizationReferenceId"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}"/>
                    </div>
                </td>
                <td valign="top" class="infoline">
                    <div align="left">
                        <kul:htmlControlAttribute
                                attributeEntry="${accountAttributes.useAmountOrPercent}"
                                property="document.account[${ctr}].useAmountOrPercent"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}"
                                onchange="updateAccountsTotal('document.totalDollarAmount', 'document.accountingLinesTotal', '${nbrOfItems}', '${accountsNbr}' )"/>
                    </div>
                </td>
                <td valign="top" class="infoline">
                    <div align="left">
                        <kul:htmlControlAttribute
                                attributeEntry="${accountAttributes.amountOrPercent}"
                                property="document.account[${ctr}].amountOrPercent"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}"
                                onchange="updateAccountsTotal('document.totalDollarAmount', 'document.accountingLinesTotal', '${nbrOfItems}', '${accountsNbr}' )"/>
                    </div>
                </td>
                <td valign="center" class="neutral">
                    <div class="actions">
                        <c:choose>
                            <c:when test="${fullEntryMode}">
                                <html:html-button
                                        property="methodToCall.deleteAccount.line${ctr}"
                                        alt="Delete Account ${ctr+1}"
                                        title="Delete Account ${ctr+1}"
                                        styleClass="btn clean"
                                        value="Delete"
                                        innerHTML="<span class=\"fa fa-trash\"></span>"/>
                            </c:when>
                            <c:otherwise>
                                <div align="center">&nbsp;</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </td>
            </tr>
        </logic:iterate>
    </table>
</div>

<div class="tab-container">
    <h3>Totals</h3>
    <table class="standard" summary="Totals Section">
        <tr>
            <th class="right" width="62%" scope="row">
                <kul:htmlAttributeLabel attributeEntry="${DataDictionary.IWantDocument.attributes.totalDollarAmount}"/>
            </th>
            <td valign="middle" class="datacell right heavy" width="150px">
                <kul:htmlControlAttribute
                        attributeEntry="${DataDictionary.IWantDocument.attributes.totalDollarAmount}"
                        property="document.accountingLinesTotal"
                        readOnly="true"/>
            </td>
            <td class="datacell">&nbsp;</td>
        </tr>
        <tr>
            <th class="right" width="62%" scope="row">
                <kul:htmlAttributeLabel attributeEntry="${DataDictionary.IWantDocument.attributes.itemAndAccountDifference}"/>
            </th>
            <td valign="middle" class="datacell right heavy" width="150px">
                <kul:htmlControlAttribute
                        attributeEntry="${DataDictionary.IWantDocument.attributes.itemAndAccountDifference}"
                        property="document.itemAndAccountDifference"
                        readOnly="true"/>
            </td>
            <td class="datacell">&nbsp;</td>
        </tr>
    </table>
</div>