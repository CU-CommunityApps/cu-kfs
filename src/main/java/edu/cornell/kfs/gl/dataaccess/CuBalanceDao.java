package edu.cornell.kfs.gl.dataaccess;

import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.dataaccess.BalanceDao;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;

import java.util.Iterator;
import java.util.List;

public interface CuBalanceDao extends BalanceDao {

    /**
     * Returns the balances that would specifically be picked up by the Reversion year end process
     * 
     * @param year the year to find balances for
     * @param endOfYear
     * @param options
     * @param parameterEvaluators a list of parameter evaluators
     * @return an iterator of the balances to process
     */
    Iterator<Balance> findReversionBalancesForFiscalYear(Integer year, boolean endOfYear, SystemOptions options, List<ParameterEvaluator> parameterEvaluators);

}
