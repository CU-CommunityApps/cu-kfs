package edu.cornell.kfs.cemi.vnd.dataaccess;
    
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface CemiVendorDaoJdbc {

    void clearExistingListOfBaseVendorData();
    
    void clearExistingListOfExtractableVendorIds();
    
    void updateSupplierExtractQuerySettings(final LocalDate fromDate, final LocalDate toDate);
    
    void prepareBaseVendorDataNeededForMainVendorIdQuery();
    
    void queryAndStoreVendorIdsForSupplierExtract();
    
    void storeSupplierIdVendorIdSupplierExtractRunDateMapping(final String supplierId,
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier,
            final LocalDateTime jobRunDate);

}
