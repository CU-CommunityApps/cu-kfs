package edu.cornell.kfs.cemi.module.cg.dataaccess;

import java.time.LocalDateTime;

public interface CemiAwardScheduleDao {
    
    void clearExistingListOfExtractableProposalNumbers();
   
    void queryAndStoreAwardProposalNumbersForAwardScheduleExtract();
    
    void storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(final String spreadsheetKey,
            final String awardProposalNumber, final LocalDateTime jobRunDate);

}
