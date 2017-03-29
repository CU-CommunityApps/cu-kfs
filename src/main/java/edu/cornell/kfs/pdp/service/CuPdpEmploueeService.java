package edu.cornell.kfs.pdp.service;

import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;

public interface CuPdpEmploueeService {
    
    /**
     * Based on the customer in the payment file, this function will decide if the caller should process the payee as an employee
     * @param paymentFile
     * @return
     */
    boolean shouldProcessPayeeAsEmployee(PaymentFileLoad paymentFile);
}
