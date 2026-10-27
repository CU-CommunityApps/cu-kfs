package edu.cornell.kfs.cemi.vnd.dataaccess;
    
import java.time.LocalDate;
import java.util.Map;

public interface CemiSupplierDao {

    void clearExistingListOfBaseVendorData();
    
    void clearExistingListOfExtractableVendorIds();
    
    void updateSupplierExtractQuerySettings(final LocalDate fromDate, final LocalDate toDate);
    
    void prepareBaseVendorDataNeededForMainVendorIdQuery();
    
    void queryAndStoreVendorIdsForSupplierExtract();

    Map<String, String> getPaymentTermsMappings();

    boolean isB2BVendor(final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier);

}
