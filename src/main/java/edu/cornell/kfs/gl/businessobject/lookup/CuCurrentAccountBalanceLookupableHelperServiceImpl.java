package edu.cornell.kfs.gl.businessobject.lookup;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.CurrentAccountBalance;
import org.kuali.kfs.gl.businessobject.lookup.CurrentAccountBalanceLookupableHelperServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

/**
 * Custom CurrentAccountBalance lookupable helper subclass that adds an option
 * for excluding "CB" amounts from the calculations. This allows for effectively
 * turning the results into Current Fund Balances instead.
 */
public class CuCurrentAccountBalanceLookupableHelperServiceImpl extends CurrentAccountBalanceLookupableHelperServiceImpl {

    private static final long serialVersionUID = 2542719296293895780L;

    /**
     * Overridden to forcibly return a zero amount if the caller is trying to find a CB period amount
     * for updating the total income or expense and the "EXCLUDE_CB_PERIOD" parameter is enabled.
     * The superclass's "updateCurrentBalance" method should be passing in a balance of Actual Balance type
     * when calculating CB period amounts for total income/expense updates.
     * 
     * @see org.kuali.kfs.gl.businessobject.lookup.CurrentAccountBalanceLookupableHelperServiceImpl#accumulateMonthlyAmounts(
     * org.kuali.kfs.gl.businessobject.Balance, java.lang.String)
     */
    @Override
    protected KualiDecimal accumulateMonthlyAmounts(Balance balance, String fiscalPeriodCode) {
        if (StringUtils.equals(KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE, fiscalPeriodCode)
                && StringUtils.equals(getActualBalanceTypeCode(), balance.getBalanceTypeCode())
                && shouldExcludeCBPeriod()) {
            return KualiDecimal.ZERO;
        }
        
        return super.accumulateMonthlyAmounts(balance, fiscalPeriodCode);
    }

    /*
     * TODO: This method currently just returns the value of a deprecated constant,
     * to match what the superclass's "updateCurrentBalance" method is using.
     * If the superclass gets updated to use SystemOptions and the OptionsService for this,
     * then this method will need to be updated accordingly.
     */
    @SuppressWarnings("deprecation")
    protected String getActualBalanceTypeCode() {
        return KFSConstants.BALANCE_TYPE_ACTUAL;
    }

    protected boolean shouldExcludeCBPeriod() {
        @SuppressWarnings("deprecation")
        Boolean excludeCBPeriod = getParameterService().getParameterValueAsBoolean(
                CurrentAccountBalance.class, CUKFSParameterKeyConstants.GlParameterConstants.EXCLUDE_CB_PERIOD);
        return Boolean.TRUE.equals(excludeCBPeriod);
    }

}
