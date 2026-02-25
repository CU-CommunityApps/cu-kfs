package edu.cornell.kfs.vnd.batch.service;

import java.time.LocalDateTime;

public interface CemiSupplierExtractService {

    void resetState();

    void initializeVendorActivityDateRangeSettings();

    void populateListOfBaseVendorData();

    void populateListOfInScopeVendors();

    void generateIntermediateSupplierExtractData(final LocalDateTime jobRunDate);

    void generateSupplierExtractFile(final LocalDateTime jobRunDate);

}
