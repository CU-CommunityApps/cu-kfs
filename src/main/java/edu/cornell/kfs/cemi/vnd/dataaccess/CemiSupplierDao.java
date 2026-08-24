package edu.cornell.kfs.cemi.vnd.dataaccess;
    
import java.time.LocalDate;

public interface CemiSupplierDao {

    void clearExistingListOfBaseVendorData();
    
    void clearExistingListOfExtractableVendorIds();
    
    void updateSupplierExtractQuerySettings(final LocalDate fromDate, final LocalDate toDate);
    
    void prepareBaseVendorDataNeededForMainVendorIdQuery();
    
    void queryAndStoreVendorIdsForSupplierExtract();

}
