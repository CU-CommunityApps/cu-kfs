package edu.cornell.kfs.gl.dataaccess;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.dataaccess.BalanceDao;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;

public interface CuBalanceDao extends BalanceDao {

    /**
     * this is for KFSPTS-1786 begin
     */ 
    Collection<Balance> getAccountBalance(Map<String, String> input);
    @SuppressWarnings("rawtypes")
    Collection<Balance> getAccountBalance(Map<String, String> input,Collection objectTypeCode);

    /**
      * this is for KFSPTS-1786 end
      */

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
