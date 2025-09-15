package edu.cornell.kfs.module.ar.service;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.module.ar.service.ContractsGrantsBillingUtilityService;

public interface CuContractsGrantsBillingUtilityService extends ContractsGrantsBillingUtilityService {

    /**
     * @param amount
     * @return a proper String Value. Also returns proper value for currency (USD)
     * Cornell customized to added second parameter to either show the $ symbol
     * when true or exclude symbol when false
     */
    String formatForCurrency(KualiDecimal amount, boolean showSymbol);
}
