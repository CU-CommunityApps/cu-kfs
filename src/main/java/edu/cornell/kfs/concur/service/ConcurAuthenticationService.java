package edu.cornell.kfs.concur.service;

public interface ConcurAuthenticationService {
    
    boolean isConcurTokenValid(String userPasswordToken);

}
