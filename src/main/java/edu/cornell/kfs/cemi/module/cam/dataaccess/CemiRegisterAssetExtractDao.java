package edu.cornell.kfs.cemi.module.cam.dataaccess;

// Refer to implementation class CemiEXTRACTNAMEDaoJdbcImpl for details pertaining to each method signature.

public interface CemiRegisterAssetExtractDao {
    
    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
   
    void queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    
    // EXAMPLE:
    //void storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping(final String spreadsheetKey,
    //        final String legacyObjectKey, final String jobRunDateString);

}
