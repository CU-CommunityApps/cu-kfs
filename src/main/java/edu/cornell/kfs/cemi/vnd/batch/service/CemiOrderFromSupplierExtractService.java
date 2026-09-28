package edu.cornell.kfs.cemi.vnd.batch.service;

import java.time.LocalDateTime;

public interface CemiOrderFromSupplierExtractService {

    void resetState();

    void initializeExtractDateSettings();

    void populateListOfKfsVendorAddressMappings();

    void populateListOfSupplierAddressMappings();

    void populateListOfInScopeAddresses();

    void generateIntermediateOrderFromSupplierExtractData(final LocalDateTime jobRunDate);

    void generateOrderFromSupplierExtractFile(final LocalDateTime jobRunDate);

}
