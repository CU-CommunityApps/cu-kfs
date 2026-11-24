package edu.cornell.kfs.cemi.module.cg.dataaccess;

public interface CemiImportBudgetExtractDao {

    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();

    void queryAndStoreInScopeBusinessObjectKeysForDataExtract();

}
