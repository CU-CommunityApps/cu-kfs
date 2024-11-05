package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants.PaymentRequestEditMode;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.authorization.PaymentRequestDocumentPresentationController;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapAuthorizationConstants.CUPaymentRequestEditMode;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;

public class CuPaymentRequestDocumentPresentationController extends PaymentRequestDocumentPresentationController {
	
	@Override
	public Set<String> getEditModes(final Document document) {
		final Set<String> editModes = super.getEditModes(document);
		
		final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
		final PaymentRequestDocument paymentRequestDocument = (PaymentRequestDocument)document;
		
        if(workflowDocument.isInitiated() || workflowDocument.isSaved()){
            // KFSPTS-1891
            editModes.add(KfsAuthorizationConstants.TransactionalEditMode.FRN_ENTRY);
            editModes.add(KfsAuthorizationConstants.TransactionalEditMode.WIRE_ENTRY);
        }
		
        // KFSPTS-1891
		if (canApprove(paymentRequestDocument) && canEditAmount(paymentRequestDocument)) {
			editModes.add(CUPaymentRequestEditMode.EDIT_AMOUNT);
		}	
		
		if (paymentRequestDocument.isDocumentStoppedInRouteNode(PaymentRequestStatuses.NODE_PAYMENT_METHOD_REVIEW)) {
            editModes.add(CUPaymentRequestEditMode.WAIVE_WIRE_FEE_EDITABLE);
            // KFSPTS-1891
            editModes.add(KfsAuthorizationConstants.TransactionalEditMode.FRN_ENTRY);
            // KFSPTS-2968 allows DM to edit additional charge amount
            editModes.add(CUPaymentRequestEditMode.ADDITONAL_CHARGE_AMOUNT_EDITABLE);
		}
		if(editModes.contains(PaymentRequestEditMode.TAX_INFO_VIEWABLE)){
			editModes.remove(PaymentRequestEditMode.TAX_INFO_VIEWABLE);
		}
        // KFSPTS-2712 : allow payment method review to view tax info
		if ((PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED.equals(paymentRequestDocument.getApplicationDocumentStatus()) || PaymentRequestStatuses.APPDOC_AWAITING_PAYMENT_METHOD_REVIEW.equals(paymentRequestDocument.getApplicationDocumentStatus()))&&
               // if and only if the preq has gone through tax review would TaxClassificationCode be non-empty
				!StringUtils.isEmpty(paymentRequestDocument.getTaxClassificationCode())) {
			editModes.add(PaymentRequestEditMode.TAX_INFO_VIEWABLE);
		}
		
		return editModes;
	}

	// KFSPTS-1891
	private boolean canEditAmount(final PaymentRequestDocument paymentRequestDocument) {
		if (ObjectUtils.isNotNull(paymentRequestDocument.getApplicationDocumentStatus())) {
			return PaymentRequestStatuses.APPDOC_AWAITING_PAYMENT_METHOD_REVIEW.contains(paymentRequestDocument.getApplicationDocumentStatus());
		} else {
			return false;
		}
	}
	
	// KFSUPGRADE-964
    @Override
    public boolean canDisapprove(final Document document) {
        // disapprove is never allowed for PREQ except PRNC by Treasury
        final CuPaymentRequestDocument paymentRequestDocument = (CuPaymentRequestDocument) document;
        final String paymentMethodCode = paymentRequestDocument.getPaymentMethodCode();
        
        if ((KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT.equalsIgnoreCase(paymentMethodCode) || KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equalsIgnoreCase(paymentMethodCode)) && paymentRequestDocument.isDocumentStoppedInRouteNode(PaymentRequestStatuses.NODE_PAYMENT_METHOD_REVIEW)) {
            return true;
        } else {
            return false;
        }
    }

}
