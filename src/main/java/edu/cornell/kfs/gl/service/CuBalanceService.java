package edu.cornell.kfs.gl.service;

import java.util.Iterator;

import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.service.BalanceService;

public interface CuBalanceService extends BalanceService {
    /**
     * Returns all of the balances to be forwarded for the reversion process
     * 
     * @param year the year of balances to find
     * @param endOfYear whether the organization reversion process is running end of year (before the fiscal year change over) or
     *        beginning of year (after the fiscal year change over)
     * @return an iterator of balances to put through the strenuous reversion process
     */
    Iterator<Balance> findReversionBalancesForFiscalYear(
            final Integer year, 
            final boolean endOfYear
            );

}
