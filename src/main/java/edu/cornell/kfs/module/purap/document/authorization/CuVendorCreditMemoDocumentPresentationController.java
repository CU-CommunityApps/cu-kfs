package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.authorization.VendorCreditMemoDocumentPresentationController;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.kfs.krad.document.Document;

import edu.cornell.kfs.module.purap.CUPurapAuthorizationConstants.CUPaymentRequestEditMode;

public class CuVendorCreditMemoDocumentPresentationController extends VendorCreditMemoDocumentPresentationController {
    
    @Override
    public Set<String> getEditModes(final Document document) {
        final Set<String> editModes = super.getEditModes(document);
        
        // KFSPTS-1891
        editModes.add(KfsAuthorizationConstants.TransactionalEditMode.FRN_ENTRY);
        editModes.add(KfsAuthorizationConstants.TransactionalEditMode.WIRE_ENTRY);
        
        final VendorCreditMemoDocument vendorCreditMemoDocument = (VendorCreditMemoDocument)document;
        
        // KFSPTS-1891, KFSPTS-2851
        if (canApprove(vendorCreditMemoDocument) && canEditAmount(vendorCreditMemoDocument)) {
            editModes.add(CUPaymentRequestEditMode.EDIT_AMOUNT);
        }
        if (vendorCreditMemoDocument.isDocumentStoppedInRouteNode(PaymentRequestStatuses.NODE_PAYMENT_METHOD_REVIEW)) {
            editModes.add(CUPaymentRequestEditMode.WAIVE_WIRE_FEE_EDITABLE);
        }
        
        return editModes;
	}
	
    // KFSPTS-1891, KFSPTS-2851
    private boolean canEditAmount(final VendorCreditMemoDocument vendorCreditMemoDocument) {
    		return  PaymentRequestStatuses.APPDOC_PAYMENT_METHOD_REVIEW.contains(vendorCreditMemoDocument.getApplicationDocumentStatus());
    }

}
