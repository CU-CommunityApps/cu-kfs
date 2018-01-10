<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<kul:documentPage showDocumentInfo="true"
                  documentTypeName="AccountFundsUpdateDocument"
                  htmlFormAction="financialAccountFundsUpdate" renderMultipart="true"
                  showTabButtons="true">
    <c:set var="accountFundsUpdateAttributes" value="${DataDictionary.AccountFundsUpdateDocument.attributes}"/>

    <sys:documentOverview editingMode="${KualiForm.editingMode}"/>

    <kul:tab tabTitle="Account Funds Update Detail" defaultOpen="true" tabErrorKey="document.reason">
        <div class="tab-container">
            <table class="standard old-new">
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${accountFundsUpdateAttributes.reason}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.reason" attributeEntry="${accountFundsUpdateAttributes.reason}"/>
                    </td>
                    <th class="grid right" width="25%">
                    </th>
                    <td class="grid" width="25%">
                    </td>
                </tr>
            </table>
        </div>
    </kul:tab>

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
