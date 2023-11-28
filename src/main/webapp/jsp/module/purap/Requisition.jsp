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
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<kul:documentPage showDocumentInfo="true"
                  documentTypeName="RequisitionDocument"
                  htmlFormAction="purapRequisition" renderMultipart="true"
                  showTabButtons="true">

    <c:set var="fullEntryMode"
           value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])}"/>

    <sys:documentOverview editingMode="${KualiForm.editingMode}"
                          includePostingYear="true"
                          fiscalYearReadOnly="${not KualiForm.editingMode['allowPostingYearEntry']}"
                          postingYearAttributes="${DataDictionary.RequisitionDocument.attributes}">

        <purap:purapDocumentDetail
                documentAttributes="${DataDictionary.RequisitionDocument.attributes}"
                detailSectionLabel="Requisition Detail"
                editableAccountDistributionMethod="${KualiForm.readOnlyAccountDistributionMethod}"/>
    </sys:documentOverview>

    <purap:delivery
            documentAttributes="${DataDictionary.RequisitionDocument.attributes}"
            showDefaultBuildingOption="true"/>

    <purap:vendor
            documentAttributes="${DataDictionary.RequisitionDocument.attributes}"
            displayRequisitionFields="true"/>

    <purap:puritems itemAttributes="${DataDictionary.RequisitionItem.attributes}"
                    accountingLineAttributes="${DataDictionary.RequisitionAccount.attributes}"
                    displayRequisitionFields="true"/>

    <purap:purCams documentAttributes="${DataDictionary.RequisitionDocument.attributes}"
                   itemAttributes="${DataDictionary.RequisitionItem.attributes}"
                   camsItemAttributes="${DataDictionary.RequisitionCapitalAssetItem.attributes}"
                   camsSystemAttributes="${DataDictionary.RequisitionCapitalAssetSystem.attributes}"
                   camsAssetAttributes="${DataDictionary.RequisitionItemCapitalAsset.attributes}"
                   camsLocationAttributes="${DataDictionary.RequisitionCapitalAssetLocation.attributes}"
                   isRequisition="true"
                   fullEntryMode="${fullEntryMode}"/>

    <purap:paymentinfo
            documentAttributes="${DataDictionary.RequisitionDocument.attributes}"/>

    <purap:additional
            documentAttributes="${DataDictionary.RequisitionDocument.attributes}"
            displayRequisitionFields="true"/>

    <purap:summaryaccounts
            itemAttributes="${DataDictionary.RequisitionItem.attributes}"
            documentAttributes="${DataDictionary.SourceAccountingLine.attributes}"/>

    <purap:relatedDocuments />

    <purap:paymentHistory />

    <%--KFSPTS-974:   --%>

    <purap:notes-sciquest notesBo="${KualiForm.document.notes}"
                          noteType="${KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE}"
                          allowsNoteFYI="true"
                          defaultOpen="true"
                          attachmentTypesValuesFinder="${DataDictionary.RequisitionDocument.attachmentTypesValuesFinder}"/>

    <kul:adHocRecipients/>

    <kul:routeLog/>

    <kul:superUserActions/>

    <c:set var="extraButtons" value="${KualiForm.extraButtons}"/>

    <sys:documentControls transactionalDocument="true" extraButtons="${extraButtons}"/>
    
    <kul:modernLookupSupport />
    <script type="application/javascript">
      document.addEventListener('DOMContentLoaded', () => {
        wireReplaceInvalidCharacters();
      });
    </script>
</kul:documentPage>
