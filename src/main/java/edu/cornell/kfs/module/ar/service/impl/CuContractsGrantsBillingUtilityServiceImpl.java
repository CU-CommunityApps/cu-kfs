package edu.cornell.kfs.module.ar.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.web.format.CurrencyFormatter;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.service.impl.ContractsGrantsBillingUtilityServiceImpl;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.ar.service.CuContractsGrantsBillingUtilityService;

public class CuContractsGrantsBillingUtilityServiceImpl extends ContractsGrantsBillingUtilityServiceImpl implements CuContractsGrantsBillingUtilityService {

    /**
     * @param amount
     * @return a proper String Value. Also returns proper value for currency (USD)
     * Cornell customized to added second parameter to either show symbols in the
     * currency String being returned when true is passed or exclude when false.
     */
    @Override
    public String formatForCurrency(KualiDecimal amount, boolean showSymbol) {
        if (ObjectUtils.isNotNull(amount)) {
            final Map<String, String> settings = new HashMap<>();
            settings.put(CurrencyFormatter.SHOW_SYMBOL, (showSymbol ? KFSConstants.Booleans.TRUE : KFSConstants.Booleans.FALSE));
            final CurrencyFormatter currencyFormatter = new CurrencyFormatter();
            currencyFormatter.setSettings(settings);
            return (String) currencyFormatter.format(amount);
        }
        return "";
    }
}
