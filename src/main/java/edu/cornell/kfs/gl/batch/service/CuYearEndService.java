package edu.cornell.kfs.gl.batch.service;

import java.util.Collection;

import org.kuali.kfs.gl.batch.service.YearEndService;

public interface CuYearEndService extends YearEndService {
    /**
     * Logs all of the missing prior year accounts based on chart that balances and encumbrances processed by year end jobs would attempt to call
     * on
     * 
     * @param balanceFiscalYear the fiscal year to find balances encumbrances for
     * @param balanceCharts list of charts to find balances for
     */
    public void logAllMissingPriorYearAccounts(Integer fiscalYear, Collection<String> charts);

    /**
     * Logs all of the missing sub fund groups based on chart that balances and encumbrances processed by the year end job would attempt to call on
     * 
     * @param balanceFiscalYear the fiscal year to find balances and encumbrances for
     * @param charts list of charts to sub fund groups for
     */
    public void logAllMissingSubFundGroups(Integer fiscalYear, Collection<String> charts);
}
