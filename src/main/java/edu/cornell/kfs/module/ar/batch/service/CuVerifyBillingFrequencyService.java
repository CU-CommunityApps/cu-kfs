package edu.cornell.kfs.module.ar.batch.service;

import java.sql.Date;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.module.ar.batch.service.VerifyBillingFrequencyService;
import org.kuali.kfs.module.ar.businessobject.BillingPeriod;

//CUMod: KFSPTS-15655 required this interface exist with no content.
public interface CuVerifyBillingFrequencyService extends VerifyBillingFrequencyService {

    //CUMod: KFSPTS-14970
    BillingPeriod getStartDateAndEndDateOfPreviousBillingPeriod(ContractsAndGrantsBillingAward award, Date calculatedLastBilledDate, AccountingPeriod currPeriod, String creationProcessTypeCode);

}
