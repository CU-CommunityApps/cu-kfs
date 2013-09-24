package edu.cornell.kfs.gl.service;

import java.util.Collection;
import java.util.Iterator;

import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.service.BalanceService;

public interface CuBalanceService extends BalanceService {
    /** 
     * This method returns the total count of balances for a fiscal year and specified charts
     * 
     * @param year fiscal year to check
     * @param list of specified charts
     * @return the count of balances
     */
    public int countBalancesForFiscalYear(Integer year, Collection<String> charts);

    /**
     * This method returns all of the balances specifically for the nominal activity closing job when annual closing charts are specified
     * 
     * @param year year to find balances for
     * @param charts list of charts to find balances for
     * @return an Iterator of nominal activity balances
     */
    public Iterator<Balance> findNominalActivityBalancesForFiscalYear(Integer year, Collection<String> charts);
  
    /**
     * Returns all the balances specifically to be processed by the balance forwards job for the "general" rule 
     * for the specified fiscal year and charts
     * 
     * @param year the fiscal year to find balances for
     * @param charts charts to find balances for
     * @return an Iterator of balances to process for the general balance forward process
     */
    public Iterator<Balance> findGeneralBalancesToForwardForFiscalYear(Integer year, Collection<String> charts);

    /**
     * Returns all the balances to be forwarded for the "cumulative" rule
     * @param year the fiscal year to find balances for
     * @param charts charts to find balances for
     * @return an Iterator of balances to process for the cumulative/active balance forward process
     */
    public Iterator<Balance> findCumulativeBalancesToForwardForFiscalYear(Integer year, Collection<String> charts);

}
