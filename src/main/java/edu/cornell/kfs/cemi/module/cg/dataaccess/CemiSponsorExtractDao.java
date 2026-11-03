package edu.cornell.kfs.cemi.module.cg.dataaccess;

public interface CemiSponsorExtractDao {
    
    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
   
    void queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    
    // EXAMPLE:
    //void storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping(final String spreadsheetKey,
    //        final String legacyObjectKey, final String jobRunDateString);

}
