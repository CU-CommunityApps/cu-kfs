package edu.cornell.kfs.cemi.patterntemplate.dataaccess;

// Refer to implementation class CemiEXTRACTNAMEDaoJdbcImpl for details pertaining to each method signature.

public interface CemiEXTRACTNAMEExtractDao {
    
    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
   
    void queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    
    void storeSpreadsheetRowItemKeyLegecyObjectKeyExractRunDateMapping(final String spreadsheetKey,
            final String legacyObjectKey, final String jobRunDateString);

}
