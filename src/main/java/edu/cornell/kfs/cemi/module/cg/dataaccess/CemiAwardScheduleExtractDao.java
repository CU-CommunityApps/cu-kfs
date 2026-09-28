package edu.cornell.kfs.cemi.module.cg.dataaccess;

public interface CemiAwardScheduleExtractDao {
    
    void clearExistingListOfExtractableProposalNumbers();
   
    void queryAndStoreAwardProposalNumbersForAwardScheduleExtract();
    
}
