package edu.cornell.kfs.cemi.vnd.dataaccess;

public interface CemiRemitToSupplierDao {
    
    void clearExistingListOfExtractableRemitAddressIds();
    
    void updateRemitToSupplierExtractQuerySettings(final String supplierJobRunDate);

    void queryAndStoreAddressIdsForRemitToSupplierExtract();

}
