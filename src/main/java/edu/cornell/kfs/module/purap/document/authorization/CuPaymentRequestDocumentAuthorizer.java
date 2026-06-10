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
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.authorization.PurchasingAccountsPayableTransactionalDocumentAuthorizerBase;

/**
 * CU Customization: KFSPTS-38144 Custom authorizer for PaymentRequestDocument that allows AP staff to edit
 * vendor address fields when the document is in "Awaiting AP Review" status.
 */
public class CuPaymentRequestDocumentAuthorizer extends PurchasingAccountsPayableTransactionalDocumentAuthorizerBase {

    @Override
    public Set<String> getDocumentActions(
            final Document document, final Person user,
            final Set<String> documentActionsFromPresentationController) {

        final Set<String> documentActionsToReturn = super.getDocumentActions(document, user,
                documentActionsFromPresentationController);

        final PaymentRequestDocument preqDocument = (PaymentRequestDocument) document;
        if (canEditVendorAddressInApReview(preqDocument)) {
            documentActionsToReturn.add(PurapAuthorizationConstants.CAN_EDIT_VENDOR_ADDRESS);
        }

        return documentActionsToReturn;
    }

    protected boolean canEditVendorAddressInApReview(final PaymentRequestDocument preqDocument) {
        final String applicationDocumentStatus = preqDocument.getApplicationDocumentStatus();
        return StringUtils.equals(PaymentRequestStatuses.APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, 
                applicationDocumentStatus);
    }

}
