package edu.cornell.kfs.module.ar.document.service.impl;

import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.module.ar.document.service.impl.ContractsGrantsBillingAwardVerificationServiceImpl;

public class CuContractsGrantsBillingAwardVerificationServiceImpl
        extends ContractsGrantsBillingAwardVerificationServiceImpl {
    
    /*
     * CU Customization backport FINP-7366
     */
    @Override
    public boolean isAwardFinalInvoiceAlreadyBuilt(ContractsAndGrantsBillingAward award) {
        return award.getActiveAwardAccounts().stream()
                .allMatch(ContractsAndGrantsBillingAwardAccount::isFinalBilledIndicator);
    }

}
