package edu.cornell.kfs.cemi.vnd.dataaccess;

public interface CemiRemitToSupplierExtractDao {
    
    void clearExistingListOfExtractableRemitAddressIds();
    
    void updateRemitToSupplierExtractQuerySettings(final String supplierJobRunDate);

    void queryAndStoreAddressIdsForRemitToSupplierExtract();

}
