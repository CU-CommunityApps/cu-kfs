package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.businessobject.AddressValidationResults;

public interface ConcurPersonValidationService {
    
    boolean validPerson(String employeeId);
    
    AddressValidationResults validPdpAddress(String employeeId);
    
    boolean isPayeeSignedUpForACH(String employeeId);
}
