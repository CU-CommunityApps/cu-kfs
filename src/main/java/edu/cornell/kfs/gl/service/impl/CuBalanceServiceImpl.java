package edu.cornell.kfs.gl.service.impl;

import java.util.Collection;
import java.util.Iterator;

import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.service.impl.BalanceServiceImpl;

import edu.cornell.kfs.gl.dataaccess.CuBalanceDao;
import edu.cornell.kfs.gl.service.CuBalanceService;

public class CuBalanceServiceImpl extends BalanceServiceImpl implements CuBalanceService {
	
    /**
     * Uses the DAO to count the number of balances associated with the given fiscal year and all specified  charts 
     * 
     * @param fiscal year a fiscal year to count balances for
     * @param list of specified charts
     * @return an integer with the number of balances 
     * @see org.kuali.kfs.gl.service.BalanceService#countBalancesForFiscalYear(java.lang.Integer, java.util.List)
     */
    @Override
    public int countBalancesForFiscalYear(Integer year, Collection<String> charts) {
        return ((CuBalanceDao) balanceDao).countBalancesForFiscalYear(year, charts);
    }
    
    /**
     * This method returns all of the balances specifically for the nominal activity closing job when annual closing charts are specified
     * 
     * @param year year to find balances for
     * @param charts list of charts to find balances for
     * @return an Iterator of nominal activity balances
     * @see org.kuali.kfs.gl.service.BalanceService#findNominalActivityBalancesForFiscalYear(java.lang.Integer, java.util.List)
     */
    @Override
    public Iterator<Balance> findNominalActivityBalancesForFiscalYear(Integer year, Collection<String> charts) {
        return ((CuBalanceDao) balanceDao).findNominalActivityBalancesForFiscalYear(year, charts);
    }
    
    /**
     * Returns all the balances to be forwarded for the "cumulative" rule
     * @param year the fiscal year to find balances for
     * @param charts charts to find balances for
     * @return an Iterator of balances to process for the cumulative/active balance forward process
     * @see org.kuali.kfs.gl.service.BalanceService#findCumulativeBalancesToForwardForFiscalYear(java.lang.Integer, java.util.List)
     */
    @Override
    public Iterator<Balance> findCumulativeBalancesToForwardForFiscalYear(Integer year, Collection<String> charts) {
        return ((CuBalanceDao) balanceDao).findCumulativeBalancesToForwardForFiscalYear(year, charts);
    }    
   
    /**
     * Returns all the balances specifically to be processed by the balance forwards job for the "general" rule
     * @param year the fiscal year to find balances for
     * @param charts charts to find balances for
     * @return an Iterator of balances to process for the general balance forward process
     * @see org.kuali.kfs.gl.service.BalanceService#findGeneralBalancesToForwardForFiscalYear(java.lang.Integer, java.util.List)
     */
    @Override
    public Iterator<Balance> findGeneralBalancesToForwardForFiscalYear(Integer year, Collection<String> charts) {
        return ((CuBalanceDao) balanceDao).findGeneralBalancesToForwardForFiscalYear(year, charts);
    }
    
    /**
     * Returns all of the balances to be forwarded for the organization reversion process
     * @param year the year of balances to find
     * @param endOfYear whether the organization reversion process is running end of year (before the fiscal year change over) or beginning of year (after the fiscal year change over)
     * @return an iterator of balances to put through the strenuous organization reversion process
     * @see org.kuali.kfs.gl.service.BalanceService#findOrganizationReversionBalancesForFiscalYear(java.lang.Integer, boolean)
     */
    public Iterator<Balance> findReversionBalancesForFiscalYear(Integer year, boolean endOfYear) {
        return ((CuBalanceDao) balanceDao).findReversionBalancesForFiscalYear(year, endOfYear);
    }

}
