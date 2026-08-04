package edu.cornell.kfs.cemi.vnd.dataaccess;

public interface CemiEntityContactExtractDao {

    void clearAnyExistingInScopeVendorContactKeysFromPreviousExecution();

    void updateEntityContactExtractQuerySettings(final String supplierJobRunDateString);

    void queryAndStoreInScopeVendorContactKeysForDataExtract();

    String findSupplierIdForVendorContact(final Integer vendorContactGeneratedIdentifier,
            final String supplierJobRunDateString);

}
