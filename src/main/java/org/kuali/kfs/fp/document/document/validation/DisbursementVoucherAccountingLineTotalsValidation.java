/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.fp.document.validation.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentPresentationController;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.KfsAuthorizationConstants.DisbursementVoucherEditMode;
import org.kuali.kfs.sys.KfsAuthorizationConstants.TransactionalEditMode;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.impl.AccountingLineGroupTotalsUnchangedValidation;
import org.kuali.rice.kim.api.identity.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DisbursementVoucherAccountingLineTotalsValidation extends AccountingLineGroupTotalsUnchangedValidation {

    private static final Logger LOG = LogManager.getLogger(DisbursementVoucherAccountingLineTotalsValidation.class);

    private DocumentHelperService documentHelperService;

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validate start");
        }

        DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) event.getDocument();

        Person financialSystemUser = GlobalVariables.getUserSession().getPerson();
        final Set<String> currentEditModes = getEditModesFromDocument(dvDocument, financialSystemUser);

        // amounts can only decrease
        List<String> candidateEditModes = this.getCandidateEditModes();
        if (this.hasRequiredEditMode(currentEditModes, candidateEditModes)) {

            //users in foreign or wire workgroup can increase or decrease amounts because of currency conversion
            List<String> foreignDraftAndWireTransferEditModes = this.getForeignDraftAndWireTransferEditModes(dvDocument);
            if (!this.hasRequiredEditMode(currentEditModes, foreignDraftAndWireTransferEditModes)) {
                DisbursementVoucherDocument persistedDocument = (DisbursementVoucherDocument) retrievePersistedDocument(dvDocument);
                if (persistedDocument == null) {
                    handleNonExistentDocumentWhenApproving(dvDocument);
                    return true;
                }
                // KFSMI- 5183
                if (persistedDocument.getDocumentHeader().getWorkflowDocument().isSaved() && persistedDocument
                        .getDisbVchrCheckTotalAmount().isZero()) {
                    return true;
                }

                // check total cannot decrease
                if (!persistedDocument.getDocumentHeader().getWorkflowDocument()
                        .isCompletionRequested() && persistedDocument.getDisbVchrCheckTotalAmount().isLessThan(
                        dvDocument.getDisbVchrCheckTotalAmount())) {
                    GlobalVariables.getMessageMap().putError(
                            KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.DISB_VCHR_CHECK_TOTAL_AMOUNT,
                            KFSKeyConstants.ERROR_DV_CHECK_TOTAL_CHANGE);
                    return false;
                }
            }

            return true;
        }

        return super.validate(event);
    }

    /**
     * determine whether the give user has permission to any edit mode defined in the given candidate edit modes
     *
     * @param currentEditModes   the edit modes currently available to the given user on the document
     * @param candidateEditModes the given candidate edit modes
     * @return true if the give user has permission to any edit mode defined in the given candidate edit modes; otherwise, false
     */
    protected boolean hasRequiredEditMode(Set<String> currentEditModes, List<String> candidateEditModes) {
        for (String editMode : candidateEditModes) {
            if (currentEditModes.contains(editMode)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieves the current edit modes from the document
     *
     * @param accountingDocument  the document to find edit modes of
     * @param financialSystemUser the user requesting the edit modes
     * @return the Set of current edit modes
     */
    protected Set<String> getEditModesFromDocument(AccountingDocument accountingDocument, Person financialSystemUser) {
        final TransactionalDocumentAuthorizer documentAuthorizer =
                (TransactionalDocumentAuthorizer) documentHelperService.getDocumentAuthorizer(accountingDocument);
        final TransactionalDocumentPresentationController presentationController =
                (TransactionalDocumentPresentationController) documentHelperService
                        .getDocumentPresentationController(accountingDocument);

        final Set<String> presentationControllerEditModes = presentationController.getEditModes(accountingDocument);
        return documentAuthorizer.getEditModes(accountingDocument, financialSystemUser,
                presentationControllerEditModes);
    }

    /**
     * @return the possibly desired edit modes
     */
    protected List<String> getCandidateEditModes() {
        List<String> candidateEdiModes = new ArrayList<>();
        candidateEdiModes.add(DisbursementVoucherEditMode.TAX_ENTRY);
        candidateEdiModes.add(TransactionalEditMode.FRN_ENTRY);
        candidateEdiModes.add(DisbursementVoucherEditMode.TRAVEL_ENTRY);
        candidateEdiModes.add(TransactionalEditMode.WIRE_ENTRY);

        return candidateEdiModes;
    }

    /**
     * get foreign draft And wire transfer edit mode names, as well as tax if the payee is a non-resident alien
     *
     * @param dvDocument the document we're validating
     * @return foreign draft And wire transfer edit mode names
     */
    protected List<String> getForeignDraftAndWireTransferEditModes(DisbursementVoucherDocument dvDocument) {
        List<String> foreignDraftAndWireTransferEditModes = new ArrayList<>();
        foreignDraftAndWireTransferEditModes.add(TransactionalEditMode.FRN_ENTRY);
        foreignDraftAndWireTransferEditModes.add(TransactionalEditMode.WIRE_ENTRY);

        if (includeTaxAsTotalChangingMode(dvDocument)) {
            foreignDraftAndWireTransferEditModes.add(DisbursementVoucherEditMode.TAX_ENTRY);
        }

        return foreignDraftAndWireTransferEditModes;
    }

    /**
     * Determines whether the tax edit mode should be allowed to change the accounting line totals,
     * based on whether the payee is a non-resident alien or not
     *
     * @param dvDocument the document to check
     * @return true if the tax entry mode can change accounting line totals, false otherwise
     */
    protected boolean includeTaxAsTotalChangingMode(DisbursementVoucherDocument dvDocument) {
        return dvDocument.getDvPayeeDetail().isDisbVchrAlienPaymentCode();
    }

    public void setDocumentHelperService(DocumentHelperService documentHelperService) {
        this.documentHelperService = documentHelperService;
    }
}
