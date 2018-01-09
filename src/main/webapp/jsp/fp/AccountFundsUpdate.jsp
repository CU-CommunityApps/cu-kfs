<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<kul:documentPage showDocumentInfo="true"
                  documentTypeName="AccountFundsUpdateDocument"
                  htmlFormAction="financialAccountFundsUpdate" renderMultipart="true"
                  showTabButtons="true">

    <sys:documentOverview editingMode="${KualiForm.editingMode}"/>

    <kul:tab tabTitle="Accounting Lines" defaultOpen="true" tabErrorKey="${KFSConstants.ACCOUNTING_LINE_ERRORS}"
             helpUrl="${KualiForm.accountingLineImportInstructionsUrl}" helpLabel="Import Templates">
        <sys-java:accountingLines>
            <sys-java:accountingLineGroup newLinePropertyName="newSourceLine" collectionPropertyName="document.sourceAccountingLines" collectionItemPropertyName="document.sourceAccountingLine" attributeGroupName="source"/>
            <sys-java:accountingLineGroup newLinePropertyName="newTargetLine" collectionPropertyName="document.targetAccountingLines" collectionItemPropertyName="document.targetAccountingLine" attributeGroupName="target"/>
        </sys-java:accountingLines>
    </kul:tab>

    <gl:generalLedgerPendingEntries/>
    <kul:notes/>
    <kul:adHocRecipients/>
    <kul:routeLog/>
    <kul:superUserActions/>
    <sys:documentControls transactionalDocument="true" extraButtons="${KualiForm.extraButtons}"/>

</kul:documentPage>
