package edu.cornell.kfs.fp.document.validation.impl;
import java.util.List;
import java.util.Set;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherAccountingLineTotalsValidation;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuDisbursementVoucherAccountingLineTotalsValidation extends DisbursementVoucherAccountingLineTotalsValidation {

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
                if (persistedDocument.getDocumentHeader().getWorkflowDocument().isSaved() && persistedDocument.getDisbVchrCheckTotalAmount().isZero()) {
                    return true;
                }

                // check total cannot decrease
                if (!persistedDocument.getDocumentHeader().getWorkflowDocument().isCompletionRequested() && (!persistedDocument.getDisbVchrCheckTotalAmount().equals(dvDocument.getDisbVchrCheckTotalAmount()))) {
                    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.DISB_VCHR_CHECK_TOTAL_AMOUNT, CUKFSKeyConstants.ERROR_DV_CHECK_TOTAL_NO_CHANGE);
                    return false;
                }
            }

            return true;
        }

        return super.validate(event);
    }

}
