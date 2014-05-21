package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.kuali.kfs.module.purap.PurapAuthorizationConstants.PaymentRequestEditMode;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.authorization.VendorCreditMemoDocumentPresentationController;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.rice.krad.document.Document;

import edu.cornell.kfs.module.purap.CUPurapAuthorizationConstants.CUPaymentRequestEditMode;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants.PaymentRequestDocument.NodeDetailEnum;

public class CuVendorCreditMemoDocumentPresentationController extends VendorCreditMemoDocumentPresentationController {
	
	@Override
	public Set<String> getEditModes(Document document) {
		Set<String> editModes = super.getEditModes(document);
		
        // KFSPTS-1891
        editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.FRN_ENTRY);
        editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.WIRE_ENTRY);
        
        VendorCreditMemoDocument vendorCreditMemoDocument = (VendorCreditMemoDocument)document;
        
        // KFSPTS-1891, KFSPTS-2851
        if (canApprove(vendorCreditMemoDocument) && canEditAmount(vendorCreditMemoDocument)) {
            editModes.add(CUPaymentRequestEditMode.EDIT_AMOUNT);
        }
        if (vendorCreditMemoDocument.isDocumentStoppedInRouteNode(PurapConstants.PaymentRequestStatuses.NODE_PAYMENT_METHOD_REVIEW)) {
            editModes.add(CUPaymentRequestEditMode.WAIVE_WIRE_FEE_EDITABLE);
        }
        
        return editModes;
	}
	
    // KFSPTS-1891, KFSPTS-2851
    private boolean canEditAmount(VendorCreditMemoDocument vendorCreditMemoDocument) {
    		return  PurapConstants.PaymentRequestStatuses.APPDOC_PAYMENT_METHOD_REVIEW.contains(vendorCreditMemoDocument.getApplicationDocumentStatus());
    }

}
