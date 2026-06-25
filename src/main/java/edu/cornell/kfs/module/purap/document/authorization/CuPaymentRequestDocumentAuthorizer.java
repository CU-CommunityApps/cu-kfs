package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.authorization.PaymentRequestDocumentAuthorizer;

/**
 * CU Customization: KFSPTS-38144 Custom authorizer for PaymentRequestDocument that allows AP Approver to edit
 * vendor address fields when the document is in "Awaiting AP Review" status.
 */
public class CuPaymentRequestDocumentAuthorizer extends PaymentRequestDocumentAuthorizer {

    @Override
    public Set<String> getDocumentActions(final Document document, final Person user,
            final Set<String> documentActionsFromPresentationController) {

        final Set<String> documentActionsToReturn = super.getDocumentActions(document, user, documentActionsFromPresentationController);

        final PaymentRequestDocument preqDocument = (PaymentRequestDocument) document;
        final WorkflowDocument workflowDocument = preqDocument.getDocumentHeader().getWorkflowDocument();

        if (!workflowDocument.isInitiated() && !workflowDocument.isSaved() && !canEditVendorAddressInApReview(preqDocument)) {
            documentActionsToReturn.remove(PurapAuthorizationConstants.CAN_EDIT_VENDOR_ADDRESS);
        }

        return documentActionsToReturn;
    }

    protected boolean canEditVendorAddressInApReview(final PaymentRequestDocument preqDocument) {
        final WorkflowDocument workflowDocument = preqDocument.getDocumentHeader().getWorkflowDocument();
        
        if (!workflowDocument.isEnroute()) {
            return false;
        }
        
        final String applicationDocumentStatus = preqDocument.getApplicationDocumentStatus();
        if (!StringUtils.equals(PaymentRequestStatuses.APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, applicationDocumentStatus)) {
            return false;
        }
        
        return workflowDocument.isApprovalRequested();
    }

}
