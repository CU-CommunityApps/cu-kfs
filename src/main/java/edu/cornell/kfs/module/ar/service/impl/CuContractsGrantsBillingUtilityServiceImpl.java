package edu.cornell.kfs.module.ar.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.service.impl.ContractsGrantsBillingUtilityServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.NoCommaCurrencyFormatter;

import edu.cornell.kfs.module.ar.service.CuContractsGrantsBillingUtilityService;

public class CuContractsGrantsBillingUtilityServiceImpl extends ContractsGrantsBillingUtilityServiceImpl implements CuContractsGrantsBillingUtilityService {

    /**
     * @param amount
     * @return a proper String Value. Also returns proper value for currency (USD)
     * 
     * Cornell customized to:
     *  added second parameter to either show $ symbol in the currency String being 
     *  returned when true is passed or exclude when false.
     *  
     *  format the currency value without commas
     *  
     *  format negative currency values with a minus sign and not parentheses
     */
    @Override
    public String formatForCurrency(KualiDecimal amount, boolean showSymbol) {
        if (ObjectUtils.isNotNull(amount)) {
            final Map<String, String> settings = new HashMap<>();
            settings.put(NoCommaCurrencyFormatter.SHOW_SYMBOL, (showSymbol ? KFSConstants.Booleans.TRUE : KFSConstants.Booleans.FALSE));
            final NoCommaCurrencyFormatter noCommaCurrencyFormatter = new NoCommaCurrencyFormatter();
            noCommaCurrencyFormatter.setSettings(settings);
            return (String) noCommaCurrencyFormatter.format(amount);
        }
        return "";
    }
}
