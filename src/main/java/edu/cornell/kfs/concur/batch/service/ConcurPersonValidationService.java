package edu.cornell.kfs.concur.batch.service;

public interface ConcurPersonValidationService {
    
    boolean validPerson(String employeeId);
    
    boolean validPdpAddress(String employeeId);
    
    boolean isPayeeSignedUpForACH(String employeeId);
}
