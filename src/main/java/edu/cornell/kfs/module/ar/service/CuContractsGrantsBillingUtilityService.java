package edu.cornell.kfs.module.ar.service;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.module.ar.service.ContractsGrantsBillingUtilityService;

public interface CuContractsGrantsBillingUtilityService extends ContractsGrantsBillingUtilityService {

    /**
     * @param amount
     * @return a proper String Value. Also returns proper value for currency (USD)
     * 
     * KFSPTS-33340
     * Cornell customized to:
     *  added second parameter to either show $ symbol in the currency String being 
     *  returned when true is passed or exclude with false passed.
     *  
     *  format the currency value without commas
     *  
     *  format negative currency values with a minus sign and not parentheses
     */
    String formatForCurrency(KualiDecimal amount, boolean showSymbol);
}
