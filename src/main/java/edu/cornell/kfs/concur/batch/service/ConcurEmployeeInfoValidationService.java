package edu.cornell.kfs.concur.batch.service;

public interface ConcurEmployeeInfoValidationService {
    
    boolean validPerson(String employeeId);
    
    boolean validPdpAddress(String employeeId);
    
    boolean isPayeeSignedUpForACH(String employeeId);
    
    String getAddressValidationMessageIfCheckPayment(String employeeId);
}
