package edu.cornell.kfs.cemi.pdp.dataaccess;

public interface CemiPaymentElectionDao {

    void clearExistingListOfExtractablePayeeAchAccountGeneratedIds();

    void queryAndStorePayeeAchAccountGeneratedIdsForPaymentElectionExtract();

}
