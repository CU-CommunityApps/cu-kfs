package edu.cornell.kfs.cemi.patterntemplate.dataaccess;

import java.time.LocalDateTime;

public interface CemiEXTRACTNAMEDao {
    
    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
   
    void queryAndStoreInScopeBusinessObjectKeysForDataExtract(String inScopeBusinessObjectName);
    
    // This method populates the table holding an association that links the legacy object keys to new Workday
    // object keys for a partiuclar data extraction run date. Table names for this data would follow the pattern:
    //      CU_CEMI_MAPPING_{EXTRACT_NAME}_EXTR_FILE_T
    void storeSpreadsheetRowItemKeyLegecyObjectKeyExractRunDateMapping(final String spreadsheetKey,
            final String legacyObjectKey, final LocalDateTime jobRunDate);

}
