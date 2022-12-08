package edu.cornell.kfs.pdp.batch.service;

import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;

public interface PayeeACHAccountDocumentService {
    
    String addACHAccount(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType);
    String updateACHAccountIfNecessary(Person payee, PayeeACHAccountExtractDetail achDetail, PayeeACHAccount achAccount);
    String getDirectDepositTransactionType();

}
