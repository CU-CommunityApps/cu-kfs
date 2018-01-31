package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class TotalAmountNonZeroValidation extends GenericValidation {

    private AccountingDocument accountingDocumentForValidation;

    public boolean validate(AttributedDocumentEvent event) {
        KualiDecimal sourceTotalAmount = accountingDocumentForValidation.getSourceTotal();
        KualiDecimal targetTotalAmount = accountingDocumentForValidation.getTargetTotal();

        boolean ret = true;

        if (ObjectUtils.isNull(sourceTotalAmount) || sourceTotalAmount.isZero()) {
            addGlobalThresholdValidationError("From", sourceTotalAmount);
            ret = false;
        }
        if(ObjectUtils.isNull(targetTotalAmount) || targetTotalAmount.isZero()){
            addGlobalThresholdValidationError("To", targetTotalAmount);
            ret = false;
        }

        return ret;
    }

    private void addGlobalThresholdValidationError(String FromOrTo, KualiDecimal totalAmount){
        GlobalVariables.getMessageMap().putError(KFSConstants.AMOUNT_PROPERTY_NAME, CUKFSKeyConstants.ERROR_ZERO_AMOUNT_TOTAL, FromOrTo, totalAmount.toString());
    }

    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }
}
