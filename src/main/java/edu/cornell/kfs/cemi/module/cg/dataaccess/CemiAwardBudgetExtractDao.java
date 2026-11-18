package edu.cornell.kfs.cemi.module.cg.dataaccess;

public interface CemiAwardBudgetExtractDao {

    void clearExistingListOfExtractableProposalNumbers();

    void queryAndStoreAwardProposalNumbersForAwardBudgetExtract();

}
