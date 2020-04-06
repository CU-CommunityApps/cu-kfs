package edu.cornell.kfs.module.ar.batch.service;

import java.sql.Date;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.integration.ar.ArIntegrationConstants;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.module.ar.batch.service.VerifyBillingFrequencyService;
import org.kuali.kfs.module.ar.businessobject.BillingPeriod;

public interface CuVerifyBillingFrequencyService extends VerifyBillingFrequencyService {

    BillingPeriod getStartDateAndEndDateOfPreviousBillingPeriod(ContractsAndGrantsBillingAward award, Date calculatedLastBilledDate, 
            AccountingPeriod currPeriod, String creationProcessTypeCode);

    boolean isFirstMilestoneOrPredeterminedInvoiceBySchedule(String billingFrequency, String invoicingOptionCode, Date lastBilledDate);
}
