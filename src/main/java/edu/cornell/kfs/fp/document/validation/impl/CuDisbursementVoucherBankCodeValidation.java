package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherBankCodeValidation;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

import edu.cornell.kfs.sys.document.validation.impl.CuBankCodeValidation;

public class CuDisbursementVoucherBankCodeValidation extends DisbursementVoucherBankCodeValidation {
	 private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuDisbursementVoucherBankCodeValidation.class);

    public boolean validate(AttributedDocumentEvent event) {
        LOG.debug("validate start");
        
        DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) getAccountingDocumentForValidation();
        
        boolean isValid = CuBankCodeValidation.validate(dvDocument, dvDocument.getDisbVchrBankCode(), KFSPropertyConstants.DISB_VCHR_BANK_CODE, false, true);

        return isValid;
    }
}
