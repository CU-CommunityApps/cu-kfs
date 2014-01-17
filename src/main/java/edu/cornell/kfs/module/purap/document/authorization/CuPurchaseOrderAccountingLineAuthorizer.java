package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.authorization.PurapAccountingLineAuthorizer;
import org.kuali.kfs.module.purap.document.authorization.PurchaseOrderAccountingLineAuthorizer;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kew.api.WorkflowDocument;

import edu.cornell.kfs.module.purap.service.CuPurapAccountingService;



public class CuPurchaseOrderAccountingLineAuthorizer extends PurchaseOrderAccountingLineAuthorizer {

        /**
         * Allow new lines to be rendered at NewUnorderedItems node
         * @see org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizerBase#renderNewLine
         * (org.kuali.kfs.sys.document.AccountingDocument, java.lang.String)
         */
        @Override
        public boolean renderNewLine(AccountingDocument accountingDocument, String accountingGroupProperty) {
        WorkflowDocument workflowDocument = ((PurchasingAccountsPayableDocument) accountingDocument)
                .getFinancialSystemDocumentHeader().getWorkflowDocument();

        Set<String> currentRouteNodeName = workflowDocument.getCurrentNodeNames();

            //  if its in the NEW_UNORDERED_ITEMS node, then allow the new line to be drawn
        if (CollectionUtils.isNotEmpty(currentRouteNodeName) 
                && PurchaseOrderAccountingLineAuthorizer.NEW_UNORDERED_ITEMS_NODE.equals(currentRouteNodeName.toString())) {
            return true;
        }

        if (PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_AMENDMENT_DOCUMENT.equals(workflowDocument.getDocumentTypeName())
                && StringUtils.isNotBlank(accountingGroupProperty) && accountingGroupProperty.contains(PurapPropertyConstants.ITEM)) {
            //KFSMI-8961: The accounting line should be addable in the new items as well as
            //existing items in POA..
            //    int itemNumber = determineItemNumberFromGroupProperty(accountingGroupProperty);
            //    PurchaseOrderAmendmentDocument poaDoc = (PurchaseOrderAmendmentDocument) accountingDocument;
            //    List <PurchaseOrderItem> items = poaDoc.getItems();
            //     PurchaseOrderItem item = items.get(itemNumber);
            //    return item.isNewItemForAmendment() || item.getSourceAccountingLines().size() == 0;
            return true;
        }

        return super.renderNewLine(accountingDocument, accountingGroupProperty) || (accountingDocument instanceof PurchaseOrderAmendmentDocument
                && accountingDocument.getDocumentHeader().getWorkflowDocument().getCurrentNodeNames().equals(RequisitionStatuses.NODE_ACCOUNT) 
                && SpringContext.getBean(CuPurapAccountingService.class).isFiscalOfficersForAllAcctLines((PurchaseOrderAmendmentDocument) accountingDocument));
    
    }

    @Override
    public boolean determineEditPermissionOnLine(AccountingDocument accountingDocument, AccountingLine accountingLine,
            String accountingLineCollectionProperty, boolean currentUserIsDocumentInitiator, boolean pageIsEditable) {
     // the fields in a new line should be always editable
        if (accountingLine.getSequenceNumber() == null) {
            return true;
        }

        // check the initiation permission on the document if it is in the state of preroute, but only if
        // the PO status is not In Process.
        WorkflowDocument workflowDocument = ((PurchasingAccountsPayableDocument) accountingDocument).getFinancialSystemDocumentHeader().getWorkflowDocument();

        PurchaseOrderDocument poDocument = (PurchaseOrderDocument) accountingDocument;
        if (!poDocument.getApplicationDocumentStatus().equals(PurapConstants.PurchaseOrderStatuses.APPDOC_IN_PROCESS)
                && (workflowDocument.isInitiated() || workflowDocument.isSaved() || workflowDocument.isCompletionRequested())) {
            if (PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_AMENDMENT_DOCUMENT.equals(workflowDocument.getDocumentTypeName())) {
                PurApAccountingLine purapAccount = (PurApAccountingLine) accountingLine;
                purapAccount.refreshReferenceObject("purapItem");

                PurchaseOrderItem item = (PurchaseOrderItem) purapAccount.getPurapItem();
                return item == null || item.isNewItemForAmendment() || item.getSourceAccountingLines().size() == 0;
            } else {
                return currentUserIsDocumentInitiator;
            }
        } else {
            return true;
        }
    }
    
}
