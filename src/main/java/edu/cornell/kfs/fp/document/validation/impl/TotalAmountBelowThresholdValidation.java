package edu.cornell.kfs.fp.document.validation.impl;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class TotalAmountBelowThresholdValidation extends GenericValidation {
    private AccountingDocument accountingDocumentForValidation;

    private final static Integer ACCOUNT_FUNDS_UPDATE_MAX_TOTAL_ALLOWED = 10000; //todo: move to properties file?

    public boolean validate(AttributedDocumentEvent event) {
        KualiDecimal sourceTotalAmount = accountingDocumentForValidation.getSourceTotal();

        if(sourceTotalAmount.compareTo(new KualiDecimal(ACCOUNT_FUNDS_UPDATE_MAX_TOTAL_ALLOWED)) > 0){
            GlobalVariables.getMessageMap().putError(KFSConstants.AMOUNT_PROPERTY_NAME, CUKFSKeyConstants.ERROR_MAX_TOTAL_THRESHOLD_AMOUNT_EXCEEDED,
                    sourceTotalAmount.toString(), ACCOUNT_FUNDS_UPDATE_MAX_TOTAL_ALLOWED.toString());
            return false;
        }

        return true;
    }

    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }
}
