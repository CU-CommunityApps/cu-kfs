package edu.cornell.kfs.cemi.module.cg.dataaccess;

public interface CemiAwardScheduleExtractDao {
    
    void clearExistingListOfExtractableProposalNumbers();
   
    void queryAndStoreAwardProposalNumbersForAwardScheduleExtract();
    
    void storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(final String spreadsheetKey,
            final String awardProposalNumber, final String jobRunDateString);

}
