package edu.cornell.kfs.cemi.patterntemplate.dataaccess;

import java.time.LocalDateTime;

public interface CemiEXTRACTNAMEDao {
    
    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
   
    void queryAndStoreInScopeBusinessObjectKeysForDataExtract(String inScopeBusinessObjectName);
    
    void storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(final String spreadsheetKey,
            final String awardProposalNumber, final LocalDateTime jobRunDate);

}
