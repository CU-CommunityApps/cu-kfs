package edu.cornell.kfs.module.ar.service;

import java.sql.Date;
import java.util.List;

import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.module.ar.service.ContractsGrantsBillingUtilityService;

public interface CuContractsGrantsBillingUtilityService extends ContractsGrantsBillingUtilityService {
    
    Date determineLastBilledDateByInvoicingOption(List<ContractsAndGrantsBillingAwardAccount> awardAccounts, String invoicingOptionCode, Date awardLastBilledDate);
    
    boolean isNotExpenditureAccount(ContractsAndGrantsBillingAwardAccount billingAwardAccount);
    
}
