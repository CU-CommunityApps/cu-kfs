package edu.cornell.kfs.gl.batch.dataaccess;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.gl.batch.dataaccess.YearEndDao;

public interface CuYearEndDao extends YearEndDao {
    /**
     * Returns the keys (Chart Code and Account Number) of PriorYearAccounts that are missing for the balances associated with the
     * given fiscal year and specified charts
     * 
     * @param balanceFiscalYear a fiscal year to find balances for
     * @param balanceCharts list of charts to find balances for
     * @return a set of the missing primary keys
     */
    public Set<Map<String, String>> findKeysOfMissingPriorYearAccountsForBalances(Integer balanceFiscalYear, Collection<String> balanceCharts);

    /**
     * Returns a set of the keys (chartOfAccountsCode and accountNumber) of PriorYearAccounts that are missing for the open
     * encumbrances of a given fiscal year and specified charts
     * 
     * @param balanceFiscalYear a fiscal year to find open encumbrances for
     * 
     * @return a set of the missing primary keys
     */
    public Set<Map<String, String>> findKeysOfMissingPriorYearAccountsForOpenEncumbrances(Integer encumbranceFiscalYear, Collection<String> charts);

    /**
     * Returns a set of the keys (subFundGroupCode) of sub fund groups that are missing for the prior year accounts associated with
     * a fiscal year and specified charts to find balances for
     * 
     * @param balanceFiscalYear the fiscal year to find balances for
     * @param chartsList the charts to find balances for
     * @return a set of missing primary keys
     */
    public Set<Map<String, String>> findKeysOfMissingSubFundGroupsForBalances(Integer balanceFiscalYear, Collection<String> chartsList);

    /**
     * Returns a set of the keys (subFundGroupCode) of sub fund groups that are missing for the prior year accounts associated with
     * a fiscal year and specified charts to find open encumbrances for
     * 
     * @param encumbranceFiscalYear the fiscal year to find encumbrnaces for
     * @param encumbranceCharts charts to find encumbrnaces for
     * @return a set of missing primary keys
     */
    public Set<Map<String, String>> findKeysOfMissingSubFundGroupsForOpenEncumbrances(Integer encumbranceFiscalYear, Collection<String> encumbranceCharts);

}
