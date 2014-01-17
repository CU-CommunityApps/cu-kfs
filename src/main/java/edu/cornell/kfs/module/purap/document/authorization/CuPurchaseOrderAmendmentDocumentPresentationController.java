package edu.cornell.kfs.module.purap.document.authorization;

import java.util.List;
import java.util.Set;

import org.kuali.kfs.module.purap.PurapAuthorizationConstants.PurchaseOrderEditMode;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.authorization.PurchaseOrderAmendmentDocumentPresentationController;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.util.PurApItemUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.module.purap.PurapConstants;

public class CuPurchaseOrderAmendmentDocumentPresentationController extends PurchaseOrderAmendmentDocumentPresentationController {

    @Override
    public boolean canEdit(Document document) {
        PurchaseOrderDocument poDocument = (PurchaseOrderDocument)document;
        if (CUPurapConstants.PurchaseOrderStatuses.AWAIT_FISCAL_REVIEW.equals(poDocument.getStatusCode())) {
            return true;
        }
        return super.canEdit(document); 
    }
    
    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);
        PurchaseOrderDocument poDocument = (PurchaseOrderDocument) document;

        if (PurapConstants.PurchaseOrderStatuses.APPDOC_CHANGE_IN_PROCESS.equals(poDocument.getApplicationDocumentStatus())) {
            WorkflowDocument workflowDocument = poDocument.getFinancialSystemDocumentHeader().getWorkflowDocument();
            //  amendment doc needs to lock its field for initiator while enroute
            if (workflowDocument.isInitiated() || workflowDocument.isSaved() || workflowDocument.isCompletionRequested()) {
                editModes.add(PurchaseOrderEditMode.AMENDMENT_ENTRY);
            }
        }
        if (PurapConstants.PurchaseOrderStatuses.APPDOC_AWAIT_NEW_UNORDERED_ITEM_REVIEW.equals(poDocument.getApplicationDocumentStatus())) {
            editModes.add(PurchaseOrderEditMode.AMENDMENT_ENTRY);
        }
        if (CUPurapConstants.PurchaseOrderStatuses.AWAIT_FISCAL_REVIEW.equals(poDocument.getStatusCode())) {
            editModes.add(PurchaseOrderEditMode.AMENDMENT_ENTRY);
        }
        if (SpringContext.getBean(PurapService.class).isDocumentStoppedInRouteNode((PurchasingAccountsPayableDocument) document, "New Unordered Items")) {
            editModes.add(PurchaseOrderEditMode.UNORDERED_ITEM_ACCOUNT_ENTRY);
        }

        boolean showDisableRemoveAccounts = true;
        PurchaseOrderAmendmentDocument purchaseOrderAmendmentDocument = (PurchaseOrderAmendmentDocument) document;
        List<PurApItem> aboveTheLinePOItems = PurApItemUtils.getAboveTheLineOnly(purchaseOrderAmendmentDocument.getItems());
        PurchaseOrderDocument po = (PurchaseOrderDocument) document;
        boolean containsUnpaidPaymentRequestsOrCreditMemos = po.getContainsUnpaidPaymentRequestsOrCreditMemos();
    ItemLoop:
        for (PurApItem poItem : aboveTheLinePOItems) {
            boolean acctLinesEditable = allowAccountingLinesAreEditable((PurchaseOrderItem) poItem, containsUnpaidPaymentRequestsOrCreditMemos);
            for (PurApAccountingLine poAccoutingLine : poItem.getSourceAccountingLines()) {
                if (!acctLinesEditable) {
                    showDisableRemoveAccounts = false;
                    break ItemLoop;
                }
            }
        }

        if (!showDisableRemoveAccounts) {
            editModes.add(PurchaseOrderEditMode.DISABLE_REMOVE_ACCTS);
        }

        return editModes;
    }
}
