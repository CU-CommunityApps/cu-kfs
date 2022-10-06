package edu.cornell.kfs.gl.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.service.impl.BalanceServiceImpl;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;

import edu.cornell.kfs.coa.businessobject.Reversion;
import edu.cornell.kfs.gl.dataaccess.CuBalanceDao;
import edu.cornell.kfs.gl.service.CuBalanceService;

public class CuBalanceServiceImpl extends BalanceServiceImpl implements CuBalanceService {
	 public static final String INCEPTION_TO_DATE_SUB_FUNDS = "INCEPTION_TO_DATE_SUB_FUNDS";

    /**
     * Returns all of the balances to be forwarded for the organization reversion process
     * @param year the year of balances to find
     * @param endOfYear whether the organization reversion process is running end of year (before the fiscal year change over) or
     *        beginning of year (after the fiscal year change over)
     * @return an iterator of balances to put through the strenuous organization reversion process
     * @see org.kuali.kfs.gl.service.BalanceService#findOrganizationReversionBalancesForFiscalYear(java.lang.Integer, boolean)
     */
    public Iterator<Balance> findReversionBalancesForFiscalYear(Integer year, boolean endOfYear) {
        SystemOptions options = optionsService.getOptions(year);
        List<ParameterEvaluator> parameterEvaluators = new ArrayList<ParameterEvaluator>();

        int i = 1;
        boolean moreParams = true;
        while (moreParams) {
            if (parameterService.parameterExists(Reversion.class, PARAMETER_PREFIX + i)) {
                ParameterEvaluator parameterEvaluator = getParameterEvaluatorService().getParameterEvaluator(
                        Reversion.class, PARAMETER_PREFIX + i);
                parameterEvaluators.add(parameterEvaluator);
            } else {
                moreParams = false;
            }
            i++;
        }

        return ((CuBalanceDao) balanceDao).findReversionBalancesForFiscalYear(year, endOfYear, options, parameterEvaluators);
    }

}
