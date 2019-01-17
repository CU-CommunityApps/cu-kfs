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

<kul:documentPage showDocumentInfo="true" documentTypeName="VendorCreditMemoDocument"
                  htmlFormAction="purapVendorCreditMemo" renderMultipart="true" showTabButtons="true">

    <c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" scope="request"/>

    <!-- KFSPTS-1891 -->
    <c:set var="wireEntryMode" value="${KualiForm.editingMode['wireEntry']}" scope="request"/>
    <c:set var="frnEntryMode" value="${KualiForm.editingMode['frnEntry']}" scope="request"/>

    <c:set var="displayInitTab" value="${KualiForm.editingMode['displayInitTab']}" scope="request"/>

    <c:if test="${displayInitTab}">
        <purap:creditMemoInit documentAttributes="${DataDictionary.VendorCreditMemoDocument.attributes}"/>
        <c:set var="globalButtonTabIndex" value="50"/>


        <div align="right"><br><bean:message key="message.creditMemo.initMessage"/></div>
        <br>
    </c:if>

    <c:if test="${not displayInitTab}">
        <!-- Display hold message if payment is on hold -->
        <c:if test="${KualiForm.document.holdIndicator}">
            <h4>This Credit Memo has been Held by <c:out
                    value="${KualiForm.document.lastActionPerformedByPersonName}"/></h4>
        </c:if>

        <sys:documentOverview editingMode="${KualiForm.editingMode}" includePostingYear="true" fiscalYearReadOnly="true"
                              postingYearAttributes="${DataDictionary.VendorCreditMemoDocument.attributes}"/>

        <purap:vendor documentAttributes="${DataDictionary.VendorCreditMemoDocument.attributes}"
                      displayPurchaseOrderFields="false" displayCreditMemoFields="true"/>

        <purap:creditMemoInfo documentAttributes="${DataDictionary.VendorCreditMemoDocument.attributes}"/>

        <purap:paymentRequestProcessItems
                documentAttributes="${DataDictionary.VendorCreditMemoDocument.attributes}"
                itemAttributes="${DataDictionary.CreditMemoItem.attributes}"
                accountingLineAttributes="${DataDictionary.CreditMemoAccount.attributes}"
                isCreditMemo="true"/>

        <purap:summaryaccounts
                itemAttributes="${DataDictionary.CreditMemoItem.attributes}"
                documentAttributes="${DataDictionary.SourceAccountingLine.attributes}"/>

        <purap:relatedDocuments documentAttributes="${DataDictionary.RelatedDocuments.attributes}"/>

        <purap:paymentHistory documentAttributes="${DataDictionary.RelatedDocuments.attributes}"/>

        <purap:cmWireTransfer/>
        <purap:cmForeignDraft/>

        <gl:generalLedgerPendingEntries/>

        <kul:notes attachmentTypesValuesFinderClass="${documentEntry.attachmentTypesValuesFinderClass}"
                   defaultOpen="${KualiForm.document.openAttachmentTab}"/>

        <kul:adHocRecipients/>

        <kul:routeLog/>

        <kul:superUserActions/>
    </c:if>

    <c:set var="extraButtons" value="${KualiForm.extraButtons}" scope="request"/>

    <sys:documentControls
            transactionalDocument="true"
            extraButtons="${extraButtons}"
            suppressRoutingControls="${displayInitTab}"
            tabindex="${globalButtonTabIndex}"/>

</kul:documentPage>
