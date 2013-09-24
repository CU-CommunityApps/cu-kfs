package edu.cornell.kfs.gl.batch.dataaccess.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.gl.batch.dataaccess.impl.YearEndDaoJdbc;

import edu.cornell.kfs.gl.batch.dataaccess.CuYearEndDao;

public class CuYearEndDaoJdbc extends YearEndDaoJdbc implements CuYearEndDao {
    /**
     * Queries the database to find missing prior year accounts
     * 
     * @param balanceFiscalyear the fiscal year of balances to check for missing prior year accounts for
     * @param chartsList list of charts to find balances for
     * @return a Set of Maps holding the primary keys of missing prior year accounts
     * @see org.kuali.kfs.gl.batch.dataaccess.YearEndDao#findKeysOfMissingPriorYearAccountsForBalances(java.lang.Integer, Collection<String>)
     */
    public Set<Map<String, String>> findKeysOfMissingPriorYearAccountsForBalances(Integer balanceFiscalYear, Collection<String> chartsList) {
        //0. create the JDBC SQL string to run
        StringBuilder buildQuery = new StringBuilder();
        buildQuery.append("select distinct fin_coa_cd, account_nbr from gl_balance_t where univ_fiscal_yr = "
                + balanceFiscalYear + " and fin_coa_cd in " + formatListForSqlInClause(chartsList) + " order by fin_coa_cd, account_nbr");
        
        // 1. get a sorted list of the prior year account keys that are used by balances for the given fiscal year and list of charts
        List priorYearKeys = getJdbcTemplate().query(buildQuery.toString(), priorYearAccountRowMapper);     
        
        // 2. go through that list, finding which prior year accounts don't show up in the database
        return selectMissingPriorYearAccounts(priorYearKeys);
    }
    
    /**
     * Queries the database to find missing sub fund groups
     * 
     * @param balanceFiscalYear the fiscal year of the balance to find missing sub fund groups for
     * @param chartsList list of charts to use when finding the balance of missing sub fund groups
     * @return a Set of Maps holding the primary keys of missing sub fund groups
     * @see org.kuali.kfs.gl.batch.dataaccess.YearEndDao#findKeysOfMissingSubFundGroupsForBalances(java.lang.Integer, java.util.List)
     */
    public Set<Map<String, String>> findKeysOfMissingSubFundGroupsForBalances(Integer balanceFiscalYear, Collection<String> chartsList) {
        // see algorithm for findKeysOfMissingPriorYearAccountsForBalances
        StringBuilder buildQuery = new StringBuilder();
        buildQuery.append("select distinct ca_prior_yr_acct_t.sub_fund_grp_cd from ca_prior_yr_acct_t, gl_balance_t "
                + "where ca_prior_yr_acct_t.fin_coa_cd = gl_balance_t.fin_coa_cd and ca_prior_yr_acct_t.account_nbr = gl_balance_t.account_nbr "
                + "and gl_balance_t.univ_fiscal_yr = " + balanceFiscalYear + " and gl_balance_t.fin_coa_cd in " + formatListForSqlInClause(chartsList)
                + " and ca_prior_yr_acct_t.sub_fund_grp_cd is not null order by ca_prior_yr_acct_t.sub_fund_grp_cd");

        List subFundGroupKeys = getJdbcTemplate().query(buildQuery.toString(), subFundGroupRowMapper);
        return selectMissingSubFundGroups(subFundGroupKeys);
    }
    
    /**
     * Queries the database to find missing prior year account records for specified charts referred to by encumbrance records
     * 
     * @param encumbranceFiscalYear the fiscal year of balances to find missing encumbrance records for
     * @param encumbranceCharts list of charts to find missing encumbrances for
     * @return a Set of Maps holding the primary keys of missing prior year accounts
     * @see org.kuali.kfs.gl.batch.dataaccess.YearEndDao#findKeysOfMissingPriorYearAccountsForOpenEncumbrances(java.lang.Integer, java.util.List)
     */
    public Set<Map<String, String>> findKeysOfMissingPriorYearAccountsForOpenEncumbrances(Integer encumbranceFiscalYear, Collection<String> encumbranceCharts) {
        
        StringBuilder buildQuery = new StringBuilder();
        buildQuery.append("select distinct fin_coa_cd, account_nbr from gl_encumbrance_t where univ_fiscal_yr = "
                + encumbranceFiscalYear + " and fin_coa_cd in " + formatListForSqlInClause(encumbranceCharts).toString()
                + " and acln_encum_amt <> acln_encum_cls_amt order by fin_coa_cd, account_nbr");
        
        List priorYearKeys = getJdbcTemplate().query(buildQuery.toString(), priorYearAccountRowMapper);
        return selectMissingPriorYearAccounts(priorYearKeys);
    }
    
    /**
     * Queries the database to find missing sub fund group records referred to by encumbrances
     * 
     * @param  encumbranceFiscalYear the fiscal year of encumbrances to find missing sub fund group records for
     * @param  encumbranceCharts charts of encumbrances to find missing sub fund group records for
     * @return a Set of Maps holding the primary keys of missing sub fund group records
     * @see org.kuali.kfs.gl.batch.dataaccess.YearEndDao#findKeysOfMissingSubFundGroupsForOpenEncumbrances(java.lang.Integer, java.util.List)
     */
    public Set<Map<String, String>> findKeysOfMissingSubFundGroupsForOpenEncumbrances(Integer encumbranceFiscalYear, Collection<String> encumbranceCharts) {
        StringBuilder buildQuery = new StringBuilder();
        buildQuery.append("select distinct ca_prior_yr_acct_t.sub_fund_grp_cd from ca_prior_yr_acct_t, gl_encumbrance_t "
                + "where ca_prior_yr_acct_t.fin_coa_cd = gl_encumbrance_t.fin_coa_cd and ca_prior_yr_acct_t.account_nbr = gl_encumbrance_t.account_nbr "
                + "and gl_encumbrance_t.univ_fiscal_yr = " + encumbranceFiscalYear + " and gl_encumbrance_t.fin_coa_cd in " 
                + formatListForSqlInClause(encumbranceCharts) + " and gl_encumbrance_t.acln_encum_amt <> gl_encumbrance_t.acln_encum_cls_amt "
                + "and ca_prior_yr_acct_t.sub_fund_grp_cd is not null order by ca_prior_yr_acct_t.sub_fund_grp_cd");
        
        List subFundGroupKeys = getJdbcTemplate().query(buildQuery.toString(), subFundGroupRowMapper);
        return selectMissingSubFundGroups(subFundGroupKeys);
    }
        
    /**
     * 
     * @param valuesListToFormat
     * @return String representing the values to use as the "in" clause for JDBC query
     */
    private String formatListForSqlInClause(Collection<String> valuesListToFormat) {
        //NOTE: presuming input parameter contains one or more values and is NOT empty as this method should not have been called otherwise
        Iterator<String> listIterator = valuesListToFormat.iterator();
        StringBuilder valuesFormattedForInClause = new StringBuilder();
        valuesFormattedForInClause.append("(");
        while (listIterator.hasNext()) {
            valuesFormattedForInClause.append("'");
            valuesFormattedForInClause.append(listIterator.next().toString());
            if (listIterator.hasNext()) {
                valuesFormattedForInClause.append("',");
            } else {              
                valuesFormattedForInClause.append("')");            
            }
        }
        return valuesFormattedForInClause.toString();
    } 
}
