package edu.cornell.kfs.pdp.service;

import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;

public interface CuPdpEmployeeService {
    
    boolean shouldPayeeBeProcessedAsEmployeeForThisCustomer(PaymentFileLoad paymentFile);
}
