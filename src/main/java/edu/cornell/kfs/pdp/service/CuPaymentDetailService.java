package edu.cornell.kfs.pdp.service;

import java.util.Iterator;

import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.service.PaymentDetailService;

public interface CuPaymentDetailService extends PaymentDetailService {

    /*
     * This is similar to the 4-arg method from base code, except that this CU-specific variant
     * also filters based on the Process Immediate flag.
     */
    Iterator<PaymentDetail> getByDisbursementNumber(Integer disbursementNumber, Integer processId,
            String disbursementType, String bankCode, boolean processImmediate);

}
