package edu.cornell.kfs.cemi.pdp.batch.service;

import java.util.Iterator;

import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

public interface CemiPaymentElectionFileExtractDataBuilder {

    void writePaymentElectionFileGroupTwoTabExtractDataToIntermediateStorage(final Iterator<PayeeACHAccount> payeeAchAccounts);

}
