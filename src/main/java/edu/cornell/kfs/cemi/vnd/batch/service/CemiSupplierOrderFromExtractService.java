package edu.cornell.kfs.cemi.vnd.batch.service;

import java.time.LocalDateTime;

public interface CemiSupplierOrderFromExtractService {

    void resetState();

    void initializeExtractDateSettings();

    void populateListOfKfsVendorAddressMappings();

    void populateListOfSupplierAddressMappings();

    void populateListOfInScopeAddresses();

    void generateIntermediateSupplierOrderFromExtractData(final LocalDateTime jobRunDate);

    void generateSupplierOrderFromExtractFile(final LocalDateTime jobRunDate);

}
