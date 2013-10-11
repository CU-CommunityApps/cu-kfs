package edu.cornell.kfs.gl.dataaccess;

import java.util.Collection;
import java.util.Iterator;

import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.dataaccess.BalanceDao;

public interface CuBalanceDao extends BalanceDao{
   
    /**
     * This method gets the size collection of cash balance entry groups according to input fields and values if the entries are
     * required to be consolidated
     * 
     * @param fieldValues the input fields and values
     * @return the size collection of cash balance entry groups
     */

    public int countBalancesForFiscalYear(Integer year, Collection<String> charts);

    /**
     * This method returns all of the balances specifically for the nominal activity closing job when charts for the annual closing are specified
     * 
     * @param year year to find balances for
     * @param charts list of charts to find balances for
     * @return an Iterator of nominal activity balances
     */
    public Iterator<Balance> findNominalActivityBalancesForFiscalYear(Integer year, Collection<String> charts);
    
    /**
     * Returns the balances specifically to be forwarded to the next fiscal year, based on the "general" rule
     * 
     * @param year the fiscal year to find balances for
     * @param charts charts to find balances for
     * @return an Iterator full of Balances
     */
    public Iterator<Balance> findGeneralBalancesToForwardForFiscalYear(Integer year, Collection<String> charts);

    /**
     * Returns the C&G balances specifically to be forwarded to the next fiscal year, based on the "cumulative" rule
     * 
     * @param year the fiscal year to find balances for
     * @param charts charts to find balances for
     * @return and Iterator chuck full of Balances
     */
    public Iterator<Balance> findCumulativeBalancesToForwardForFiscalYear(Integer year, Collection<String> charts);
    
    /**
     * Returns the balances that would specifically be picked up by the Reversion year end process
     * 
     * @param year the year to find balances for
     * @return an iterator of the balances to process
     */
    public Iterator<Balance> findReversionBalancesForFiscalYear(Integer year, boolean endOfYear);

   

}
