<%--
   - The Kuali Financial System, a comprehensive financial management system for higher education.
   -
   - Copyright 2005-2017 Kuali, Inc.
   -
   - This program is free software: you can redistribute it and/or modify
   - it under the terms of the GNU Affero General Public License as
   - published by the Free Software Foundation, either version 3 of the
   - License, or (at your option) any later version.
   -
   - This program is distributed in the hope that it will be useful,
   - but WITHOUT ANY WARRANTY; without even the implied warranty of
   - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   - GNU Affero General Public License for more details.
   -
   - You should have received a copy of the GNU Affero General Public License
   - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<kul:documentPage showDocumentInfo="true"
                  htmlFormAction="financialDistributionOfIncomeAndExpense"
                  documentTypeName="DistributionOfIncomeAndExpenseDocument"
                  renderMultipart="true" showTabButtons="true">

    <sys:hiddenDocumentFields/>

    <c:if test="${!empty KualiForm.editingMode['sourceLinesReadOnlyMode']}">
        <c:set var="sourceLinesReadOnlyMode" value="true" scope="request"/>
    </c:if>

    <fp:diTripLink/>

    <sys:documentOverview editingMode="${KualiForm.editingMode}"/>

    <kul:tab tabTitle="Accounting Lines" defaultOpen="true" tabErrorKey="${KFSConstants.ACCOUNTING_LINE_ERRORS}"
             helpUrl="${KualiForm.accountingLineImportInstructionsUrl}" helpLabel="Import Templates">
        <sys-java:accountingLines>
            <sys-java:accountingLineGroup newLinePropertyName="newSourceLine" collectionPropertyName="document.sourceAccountingLines" collectionItemPropertyName="document.sourceAccountingLine" attributeGroupName="source"/>
            <sys-java:accountingLineGroup newLinePropertyName="newTargetLine" collectionPropertyName="document.targetAccountingLines" collectionItemPropertyName="document.targetAccountingLine" attributeGroupName="target"/>
        </sys-java:accountingLines>
    </kul:tab>

    <c:set var="readOnly" value="${!KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}"/>

    <fp:capitalAccountingLines readOnly="${readOnly}"/>

    <c:if test="${KualiForm.capitalAccountingLine.canCreateAsset}">
        <fp:capitalAssetCreateTab readOnly="${readOnly}"/>
    </c:if>

    <fp:capitalAssetModifyTab readOnly="${readOnly}"/>

    <gl:generalLedgerPendingEntries/>

    <kul:notes attachmentTypesValuesFinderClass="${documentEntry.attachmentTypesValuesFinderClass}" />

    <kul:adHocRecipients/>

    <kul:routeLog/>

    <kul:superUserActions/>

    <sys:documentControls
            transactionalDocument="${documentEntry.transactionalDocument}" extraButtons="${KualiForm.extraButtons}"/>

</kul:documentPage>
