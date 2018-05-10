package edu.cornell.kfs.pdp.service;

import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchService;

public interface CuAchService extends AchService {

    PayeeACHAccount getAchInformationIncludingInactive(String idType, String payeeId, String achTransactionType);

}
