package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants.PaymentRequestEditMode;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.authorization.PaymentRequestDocumentPresentationController;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapAuthorizationConstants.CUPaymentRequestEditMode;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.CUPaymentRequestStatuses;
import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants.PaymentRequestDocument.NodeDetailEnum;

public class CuPaymentRequestDocumentPresentationController extends PaymentRequestDocumentPresentationController {
	
	@Override
	public Set<String> getEditModes(Document document) {
		Set<String> editModes = super.getEditModes(document);
		
		WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
		PaymentRequestDocument paymentRequestDocument = (PaymentRequestDocument)document;
		
        if(workflowDocument.isInitiated() || workflowDocument.isSaved()){
            // KFSPTS-1891
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.FRN_ENTRY);
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.WIRE_ENTRY);
        }
		
        // KFSPTS-1891
		if (canApprove(paymentRequestDocument) && canEditAmount(paymentRequestDocument)) {
			editModes.add(CUPaymentRequestEditMode.EDIT_AMOUNT);
		}	
		
		if (paymentRequestDocument.isDocumentStoppedInRouteNode(PaymentRequestStatuses.NODE_PAYMENT_METHOD_REVIEW)) {
            editModes.add(CUPaymentRequestEditMode.WAIVE_WIRE_FEE_EDITABLE);
            // KFSPTS-1891
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.FRN_ENTRY);
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.WIRE_ENTRY);
            // KFSPTS-2968 allows DM to edit additional charge amount
            editModes.add(CUPaymentRequestEditMode.ADDITONAL_CHARGE_AMOUNT_EDITABLE);
		}
		if(editModes.contains(PaymentRequestEditMode.TAX_INFO_VIEWABLE)){
			editModes.remove(PaymentRequestEditMode.TAX_INFO_VIEWABLE);
		}
        // KFSPTS-2712 : allow payment method review to view tax info
		if ((PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED.equals(paymentRequestDocument.getApplicationDocumentStatus()) || PaymentRequestStatuses.APPDOC_PAYMENT_METHOD_REVIEW.equals(paymentRequestDocument.getApplicationDocumentStatus()))&&
               // if and only if the preq has gone through tax review would TaxClassificationCode be non-empty
				!StringUtils.isEmpty(paymentRequestDocument.getTaxClassificationCode())) {
			editModes.add(PaymentRequestEditMode.TAX_INFO_VIEWABLE);
		}
		
		return editModes;
	}

	// KFSPTS-1891
	private boolean canEditAmount(PaymentRequestDocument paymentRequestDocument) {
		if (ObjectUtils.isNotNull(paymentRequestDocument.getApplicationDocumentStatus())) {
			return PaymentRequestStatuses.APPDOC_PAYMENT_METHOD_REVIEW.contains(paymentRequestDocument.getApplicationDocumentStatus());
		} else {
			return false;
		}
	}

}
