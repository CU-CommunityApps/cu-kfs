package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.document.NonCheckDisbursementDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * Custom variant of DisbursementVoucherDocumentAmountValidation that is intended
 * for NonCheckDisbursementDocument positive-total-amount validation instead.
 */
public class CuNonCheckDisbursementDocumentAmountValidation extends GenericValidation {

    private AccountingDocument accountingDocumentForValidation;

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        boolean isValid = true;
        NonCheckDisbursementDocument nonCheckDisbursementDocument = (NonCheckDisbursementDocument) accountingDocumentForValidation;
        
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.DOCUMENT);
        
        // Accounting line total cannot be zero or negative.
        if (KualiDecimal.ZERO.compareTo(nonCheckDisbursementDocument.getSourceTotal()) >= 0) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(
                    KFSConstants.ACCOUNTING_LINE_ERRORS, CUKFSKeyConstants.ERROR_ZERO_OR_NEGATIVE_ACCOUNTING_TOTAL);
            isValid = false;
        }
        
        GlobalVariables.getMessageMap().removeFromErrorPath(KFSPropertyConstants.DOCUMENT);
        
        return isValid;
    }

    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

}
