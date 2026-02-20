package edu.cornell.kfs.vnd.batch.service;

public interface CemiSupplierExtractService {

    void resetState();

    void initializeVendorActivityDateRangeSettings();

    void populateListOfInScopeVendors();

    void generateSupplierExtractFile();

}
