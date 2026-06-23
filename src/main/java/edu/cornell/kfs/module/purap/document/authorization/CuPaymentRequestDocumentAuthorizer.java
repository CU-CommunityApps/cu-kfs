/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        
        // If document is not in INITIATED or SAVED status, check if we need to remove CAN_EDIT_VENDOR_ADDRESS
        if (!workflowDocument.isInitiated() && !workflowDocument.isSaved()) {
            if (!canEditVendorAddressInApReview(preqDocument)) {
                documentActionsToReturn.remove(PurapAuthorizationConstants.CAN_EDIT_VENDOR_ADDRESS);
            }
        }
        
        // Add CAN_EDIT_VENDOR_ADDRESS if canEditVendorAddressInApReview returns true
        if (canEditVendorAddressInApReview(preqDocument)) {
            documentActionsToReturn.add(PurapAuthorizationConstants.CAN_EDIT_VENDOR_ADDRESS);
        }

        return documentActionsToReturn;
    }

    protected boolean canEditVendorAddressInApReview(final PaymentRequestDocument preqDocument) {
        final WorkflowDocument workflowDocument = preqDocument.getDocumentHeader().getWorkflowDocument();
        
        // Return false if document is not in ENROUTE status
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
