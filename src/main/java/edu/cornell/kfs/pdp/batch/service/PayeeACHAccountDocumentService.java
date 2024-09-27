package edu.cornell.kfs.pdp.batch.service;

import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

import java.util.List;

import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;

public interface PayeeACHAccountDocumentService {
    
    String addOrUpdateACHAccountIfNecessary(Person payee, PayeeACHAccountExtractDetail achDetail,
            String payeeType, String payeeIdNumber);
    
    String addACHAccount(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType);

    String updateACHAccountIfNecessary(Person payee, PayeeACHAccountExtractDetail achDetail, PayeeACHAccount achAccount);

    String getDirectDepositTransactionType();

    List<PayeeACHAccountExtractDetail> getPersistedPayeeACHAccountExtractDetails();

    void updateACHAccountExtractDetailRetryCount(PayeeACHAccountExtractDetail achDetail, int retryCount);

}
