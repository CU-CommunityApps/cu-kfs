package edu.cornell.kfs.vnd.dataaccess;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface CemiVendorDao {

    void clearExistingListOfBaseVendorData();

    void clearExistingListOfExtractableVendorIds();

    void updateSupplierExtractQuerySettings(final LocalDate fromDate, final LocalDate toDate);

    void prepareBaseVendorDataNeededForMainVendorIdQuery();

    void queryAndStoreVendorIdsForSupplierExtract();
    
    void storeSupplierIdVendorIdSupplierExtractRunDateMapping(final String supplierId,
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier,
            final LocalDateTime jobRunDate);

}
