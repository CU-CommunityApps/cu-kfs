package edu.cornell.kfs.cemi.module.purap.dataaccess;

import java.util.Map;

public interface CemiPurchaseOrderExtractDao {
    
    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
   
    void queryAndStoreInScopeBusinessObjectKeysForDataExtract();

    String getSupplierIdForVendor(final Integer vendorHeaderGeneratedIdentifier,
            final Integer vendorDetailAssignedIdentifier, final String supplierJobRunDateString);

    Map<Integer, String> getContractManagerEmployeeIdMappings();

}
