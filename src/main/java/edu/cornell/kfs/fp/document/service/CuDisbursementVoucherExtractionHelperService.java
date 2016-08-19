package edu.cornell.kfs.fp.document.service;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.batch.service.PaymentSourceToExtractService;
import org.kuali.kfs.sys.document.service.PaymentSourceHelperService;

public interface CuDisbursementVoucherExtractionHelperService extends PaymentSourceToExtractService<DisbursementVoucherDocument> {
    
    /**
     * @return an implementation of the PaymentSourceHelperService
     */
    PaymentSourceHelperService getPaymentSourceHelperService();

}
