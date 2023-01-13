package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherBankCodeValidation;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

import edu.cornell.kfs.sys.document.validation.impl.CuBankCodeValidation;

public class CuDisbursementVoucherBankCodeValidation extends DisbursementVoucherBankCodeValidation {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        LOG.debug("validate start");        
        DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) getAccountingDocumentForValidation();      
		return CuBankCodeValidation.validate(dvDocument, dvDocument.getDisbVchrBankCode(),
				KFSPropertyConstants.DISB_VCHR_BANK_CODE, false, true);
    }
}
