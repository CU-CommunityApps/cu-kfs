package edu.cornell.kfs.pdp.batch.service;

public interface CuPayeeAddressService {
    
    String findPayerName();
    String findPayerAddressLine1();
    String findPayerAddressLine2();
    String findPayerCity();
    String findPayerState();
    String findPayerZipCode();

}
