package edu.cornell.kfs.module.purap.document.authorization;

import java.util.List;
import java.util.Set;

import org.kuali.kfs.module.purap.PurapAuthorizationConstants.PurchaseOrderEditMode;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants.RequisitionEditMode;
import org.kuali.kfs.module.purap.PurapConstants;
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
import org.kuali.kfs.krad.document.Document;

import edu.cornell.kfs.module.purap.CUPurapConstants.PurchaseOrderStatuses;

public class CuPurchaseOrderAmendmentDocumentPresentationController extends PurchaseOrderAmendmentDocumentPresentationController {

    @Override
    public boolean canEdit(Document document) {
        // KFSUPGRADE-339
        if (PurchaseOrderStatuses.APPDOC_AWAITING_FISCAL_REVIEW.equals(((PurchaseOrderDocument)document).getApplicationDocumentStatus())) {
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
            if (workflowDocument.isInitiated() || workflowDocument.isSaved()
                    || workflowDocument.isCompletionRequested()) {
                editModes.add(PurchaseOrderEditMode.AMENDMENT_ENTRY);
            }
        }
		// KFSUPGRADE-339
        if (PurchaseOrderStatuses.APPDOC_AWAITING_FISCAL_REVIEW.equals(((PurchaseOrderDocument)document).getApplicationDocumentStatus())) {
        	    editModes.add(PurchaseOrderEditMode.AMENDMENT_ENTRY);
        }
        if (getPurapService().isDocumentStoppedInRouteNode((PurchasingAccountsPayableDocument) document, "New Unordered Items")) {
            editModes.add(PurchaseOrderEditMode.UNORDERED_ITEM_ACCOUNT_ENTRY);
        }

        PurchaseOrderAmendmentDocument purchaseOrderAmendmentDocument = (PurchaseOrderAmendmentDocument) document;
        List<PurApItem> aboveTheLinePOItems =
                PurApItemUtils.getAboveTheLineOnly(purchaseOrderAmendmentDocument.getItems());
        boolean containsUnpaidPaymentRequestsOrCreditMemos = poDocument.getContainsUnpaidPaymentRequestsOrCreditMemos();
        for (PurApItem poItem : aboveTheLinePOItems) {
            if (!allowAccountingLinesAreEditable((PurchaseOrderItem) poItem,
                    containsUnpaidPaymentRequestsOrCreditMemos)) {
                editModes.add(PurchaseOrderEditMode.DISABLE_REMOVE_ACCTS);
                break;
            }
        }

        // KFSPTS-985
        if (document instanceof PurchaseOrderDocument && !editModes.contains(RequisitionEditMode.DISABLE_SETUP_ACCT_DISTRIBUTION) && !hasEmptyAcctline((PurchaseOrderDocument)document) ) {
            editModes.add(RequisitionEditMode.DISABLE_SETUP_ACCT_DISTRIBUTION);
        }
        return editModes;
    }
    	
    
}
