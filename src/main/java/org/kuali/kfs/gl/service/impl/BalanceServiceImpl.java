/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.gl.service.impl;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.OrganizationReversion;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.coa.service.ObjectTypeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.GLParameterConstants;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.OJBUtility;
import org.kuali.kfs.gl.batch.BalanceForwardStep;
import org.kuali.kfs.gl.batch.service.FilteringBalanceIterator;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.GlSummary;
import org.kuali.kfs.gl.dataaccess.BalanceDao;
import org.kuali.kfs.gl.service.BalanceService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Transactional
public class BalanceServiceImpl implements BalanceService {

    public static final String SUB_FUND_GROUPS_FOR_INCEPTION_TO_DATE_REPORTING =
            "SUB_FUND_GROUPS_FOR_INCEPTION_TO_DATE_REPORTING";
    private static final Logger LOG = LogManager.getLogger();

    protected static final String PARAMETER_PREFIX = "SELECTION_";

    protected BalanceDao balanceDao;
    protected OptionsService optionsService;
    protected ObjectTypeService objectTypeService;
    protected ParameterService parameterService;
    protected BalanceTypeService balanceTypService;
    private UniversityDateService universityDateService;
    // CU customizations increase visibility from private to protected
    protected ParameterEvaluatorService parameterEvaluatorService;

    // must have no asset, liability or fund balance balances other than object code 9899
    Collection<String> assetLiabilityFundBalanceObjectTypeCodes = null;
    Collection<String> encumbranceBaseBudgetBalanceTypeCodes = null;
    Collection<String> actualBalanceCodes = null;
    Collection<String> incomeObjectTypeCodes = null;
    Collection<String> expenseObjectTypeCodes = null;

    /**
     * @param universityFiscalYear the fiscal year to find balances for
     * @param balanceTypeCodes     the balance types to summarize
     * @return a list of summarized GL balances
     */
    @Override
    public List<GlSummary> getGlSummary(int universityFiscalYear, List<String> balanceTypeCodes) {
        LOG.debug("getGlSummary() started");

        List<GlSummary> sum = new ArrayList<>();

        Iterator<Object[]> i = balanceDao.getGlSummary(universityFiscalYear, balanceTypeCodes);
        while (i.hasNext()) {
            Object[] data = i.next();
            sum.add(new GlSummary(data));
        }
        return sum;
    }

    /**
     * Defers to the DAO to find all balances in the fiscal year.
     *
     * @param fiscalYear the fiscal year to find balances for
     * @return an Iterator full of balances from the given fiscal year
     */
    @Override
    public Iterator<Balance> findBalancesForFiscalYear(Integer fiscalYear) {
        return balanceDao.findBalancesForFiscalYear(fiscalYear);
    }

    /**
     * Checks the given account to see if there are any non zero asset fund liability fund balances for them
     *
     * @param account an account to find balances for
     * @return true if there are non zero asset liability fund balances, false if otherwise
     */
    @Override
    public boolean hasAssetLiabilityFundBalanceBalances(Account account) {
        Integer fiscalYear = universityDateService.getCurrentFiscalYear();
        ArrayList<String> fundBalanceObjectCodes = new ArrayList<>();
        fundBalanceObjectCodes.add(null == account.getChartOfAccounts() ? null :
                account.getChartOfAccounts().getFundBalanceObjectCode());
        Iterator balances = balanceDao.findBalances(account, fiscalYear, null, fundBalanceObjectCodes,
                getAssetLiabilityFundBalanceBalanceTypeCodes(), getActualBalanceCodes());

        KualiDecimal begin;
        KualiDecimal annual;

        // TODO is absolute value necessary to prevent obscure sets of value from masking accounts that should remain
        //  open?

        Map<String, KualiDecimal> groups = new HashMap<>();
        while (balances.hasNext()) {
            Balance balance = (Balance) balances.next();
            begin = balance.getBeginningBalanceLineAmount();
            annual = balance.getAccountLineAnnualBalanceAmount();

            String objectCode = balance.getObjectCode();

            KualiDecimal runningTotal = groups.get(objectCode);

            if (runningTotal == null) {
                runningTotal = KualiDecimal.ZERO;
            }

            runningTotal = runningTotal.add(begin);
            runningTotal = runningTotal.add(annual);

            groups.put(objectCode, runningTotal);
        }

        boolean success = false;

        for (Object element : groups.keySet()) {
            success |= groups.get(element).isNonZero();
        }

        return success;
    }

    /**
     * Given an iterator of balances, this returns the sum of each balance's beginning balance line amount + annual
     * account line balance amount
     *
     * @param balances an Iterator of balances to sum
     * @return the sum of all of those balances
     */
    protected KualiDecimal sumBalances(Iterator balances) {
        KualiDecimal runningTotal = KualiDecimal.ZERO;

        KualiDecimal begin;
        KualiDecimal annual;

        while (balances.hasNext()) {
            Balance balance = (Balance) balances.next();
            begin = balance.getBeginningBalanceLineAmount();
            annual = balance.getAccountLineAnnualBalanceAmount();

            runningTotal = runningTotal.add(begin);
            runningTotal = runningTotal.add(annual);
        }

        return runningTotal;
    }

    /**
     * Returns the sum of balances considered as income for the given account
     *
     * @param account the account to find income balances for
     * @return the sum of income balances
     */
    protected KualiDecimal incomeBalances(Account account) {
        Integer fiscalYear = universityDateService.getCurrentFiscalYear();

        ArrayList<String> fundBalanceObjectCodes = new ArrayList<>();
        fundBalanceObjectCodes.add(account.getChartOfAccounts().getFundBalanceObjectCode());
        Iterator balances = balanceDao.findBalances(account, fiscalYear, fundBalanceObjectCodes, null,
                getIncomeObjectTypeCodes(), getActualBalanceCodes());

        return sumBalances(balances);
    }

    /**
     * Sums all the balances associated with a given account that would be considered "expense" balances
     *
     * @param account an account to find expense balances for
     * @return the sum of those balances
     */
    protected KualiDecimal expenseBalances(Account account) {
        Integer fiscalYear = universityDateService.getCurrentFiscalYear();
        Iterator balances = balanceDao.findBalances(account, fiscalYear, null, null,
                getExpenseObjectTypeCodes(), getActualBalanceCodes());
        return sumBalances(balances);
    }

    /**
     * Checks to see if the total income balances for the given account equal the total expense balances for the given
     * account
     *
     * @param account account to find balances for
     * @return true if income balances equal expense balances, false otherwise
     */
    @Override
    public boolean fundBalanceWillNetToZero(Account account) {
        KualiDecimal income = incomeBalances(account);
        KualiDecimal expense = expenseBalances(account);
        return income.equals(expense);
    }

    /**
     * Finds all of the encumbrance balances for the given account, and figures out if those encumbrances will have a
     * net impact on the budget
     *
     * @param account an account to find balances for
     * @return true if summed encumbrances for the account are not zero (meaning encumbrances will have a net impact
     *         on the budget), false if otherwise
     */
    @Override
    public boolean hasEncumbrancesOrBaseBudgets(Account account) {
        Integer fiscalYear = universityDateService.getCurrentFiscalYear();
        Iterator balances = balanceDao.findBalances(account, fiscalYear, null, null, null,
                getEncumbranceBaseBudgetBalanceTypeCodes());
        return sumBalances(balances).isNonZero();
    }

    /**
     * Returns whether or not the beginning budget is loaded for the given account. Of course, it doesn't really check
     * the account...just the options for the current year to see if all the beginning balances have been loaded
     *
     * @param account account to check whether the beginning balance is loaded for
     * @return true if the beginning balance is loaded, false otherwise
     */
    @Override
    public boolean beginningBalanceLoaded(Account account) {
        return optionsService.getCurrentYearOptions().isFinancialBeginBalanceLoadInd();
    }

    /**
     * Determines if the account has asset/liability balances associated with it that will have a net impact
     *
     * @param account an account to check balances for
     * @return true if the account has an asset liability balance, false otherwise
     */
    @Override
    public boolean hasAssetLiabilityOrFundBalance(Account account) {
        return hasAssetLiabilityFundBalanceBalances(account) || !fundBalanceWillNetToZero(account)
                || hasEncumbrancesOrBaseBudgets(account);
    }

    public void setBalanceDao(BalanceDao balanceDao) {
        this.balanceDao = balanceDao;
    }

    public void setOptionsService(OptionsService optionsService) {
        this.optionsService = optionsService;
    }

    /**
     * This method finds the summary records of balance entries according to input fields an values, using the DAO.
     * The results will be limited to the system lookup results limit.
     *
     * @param fieldValues    the input fields an values
     * @param isConsolidated consolidation option is applied or not
     * @return the summary records of balance entries
     */
    @Override
    public Iterator lookupCashBalance(Map fieldValues, boolean isConsolidated) {
        LOG.debug("findCashBalance() started");
        return balanceDao.lookupCashBalance(fieldValues, isConsolidated, getEncumbranceBalanceTypes(fieldValues));
    }

    /**
     * This method gets the size of cash balance entries according to input fields and values
     *
     * @param fieldValues    the input fields and values
     * @param isConsolidated consolidation option is applied or not
     * @return the count of cash balance entries
     */
    @Override
    public Integer getCashBalanceRecordCount(Map fieldValues, boolean isConsolidated) {
        LOG.debug("getCashBalanceRecordCount() started");

        Integer recordCount;
        if (!isConsolidated) {
            recordCount = balanceDao.getDetailedCashBalanceRecordCount(fieldValues,
                    getEncumbranceBalanceTypes(fieldValues));
        } else {
            recordCount = balanceDao.getConsolidatedCashBalanceRecordCount(fieldValues,
                    getEncumbranceBalanceTypes(fieldValues));
        }
        return recordCount;
    }

    /**
     * This method gets the size of balance entries according to input fields and values
     *
     * @param fieldValues    the input fields and values
     * @param isConsolidated consolidation option is applied or not
     * @return the size of balance entries
     */
    @Override
    public Iterator findBalance(Map fieldValues, boolean isConsolidated) {
        LOG.debug("findBalance() started");
        return balanceDao.findBalance(fieldValues, isConsolidated, getEncumbranceBalanceTypes(fieldValues));
    }

    /**
     * This method finds the summary records of balance entries according to input fields and values
     *
     * @param fieldValues    the input fields and values
     * @param isConsolidated consolidation option is applied or not
     * @return the summary records of balance entries
     */
    @Override
    public Integer getBalanceRecordCount(Map fieldValues, boolean isConsolidated) {
        LOG.debug("getBalanceRecordCount() started");

        int recordCount;
        if (!isConsolidated) {
            recordCount = OJBUtility.getResultSizeFromMap(fieldValues, new Balance()).intValue();
        } else {
            Iterator recordCountIterator = balanceDao.getConsolidatedBalanceRecordCount(fieldValues,
                    getEncumbranceBalanceTypes(fieldValues));
            // TODO: WL: why build a list and waste time/memory when we can just iterate through the iterator and do a
            //  count?
            List recordCountList = IteratorUtils.toList(recordCountIterator);
            recordCount = recordCountList.size();
        }
        return recordCount;
    }

    /**
     * Purge the balance table by year/chart
     *
     * @param chart the chart of balances to purge
     * @param year  the year of balances to purge
     */
    @Override
    public void purgeYearByChart(String chart, int year) {
        LOG.debug("purgeYearByChart() started");
        balanceDao.purgeYearByChart(chart, year);
    }

    /**
     * load the values from the system options service and store them locally for later use.
     */
    protected void loadConstantsFromOptions() {
        LOG.debug("loadConstantsFromOptions() started");
        SystemOptions options = optionsService.getCurrentYearOptions();
        // AC
        actualBalanceCodes = Arrays.asList(options.getActualFinancialBalanceTypeCd());
        // IC
        incomeObjectTypeCodes = Arrays.asList(options.getFinObjTypeIncomeNotCashCd(),
            // IN
            options.getFinObjectTypeIncomecashCode(),
            // CH
            options.getFinObjTypeCshNotIncomeCd(),
            // TI
            options.getFinancialObjectTypeTransferIncomeCd()
        );
        // EE?
        expenseObjectTypeCodes = Arrays.asList(options.getFinObjTypeExpendNotExpCode(),
            // ES
            options.getFinObjTypeExpenditureexpCd(),
            // EX?
            options.getFinObjTypeExpNotExpendCode(),
            // TE
            options.getFinancialObjectTypeTransferExpenseCd()
        );
        // AS
        assetLiabilityFundBalanceObjectTypeCodes = Arrays.asList(options.getFinancialObjectTypeAssetsCd(),
            // LI
            options.getFinObjectTypeLiabilitiesCode(),
            // FB
            options.getFinObjectTypeFundBalanceCd()
        );
        // EX
        encumbranceBaseBudgetBalanceTypeCodes = Arrays.asList(options.getExtrnlEncumFinBalanceTypCd(),
            // IE
            options.getIntrnlEncumFinBalanceTypCd(),
            // PE
            options.getPreencumbranceFinBalTypeCd(),
            // BB
            options.getBaseBudgetFinancialBalanceTypeCd()
        );
    }

    /**
     * Use the options table to get a list of all the balance type codes associated with actual balances
     *
     * @return an array of balance type codes for actual balances
     */
    protected Collection<String> getActualBalanceCodes() {
        if (actualBalanceCodes == null) {
            loadConstantsFromOptions();
        }
        return actualBalanceCodes;
    }

    /**
     * Uses the options table to find all the balance type codes associated with income
     *
     * @return an array of income balance type codes
     */
    protected Collection<String> getIncomeObjectTypeCodes() {
        if (incomeObjectTypeCodes == null) {
            loadConstantsFromOptions();
        }
        return incomeObjectTypeCodes;
    }

    /**
     * Uses the options table to find all the balance type codes associated with expenses
     *
     * @return an array of expense option type codes
     */
    protected Collection<String> getExpenseObjectTypeCodes() {
        if (expenseObjectTypeCodes == null) {
            loadConstantsFromOptions();
        }
        return expenseObjectTypeCodes;
    }

    /**
     * Uses the options table to find all the balance type codes associated with asset/liability
     *
     * @return an array of asset/liability balance type codes
     */
    protected Collection<String> getAssetLiabilityFundBalanceBalanceTypeCodes() {
        if (assetLiabilityFundBalanceObjectTypeCodes == null) {
            loadConstantsFromOptions();
        }
        return assetLiabilityFundBalanceObjectTypeCodes;
    }

    /**
     * Uses the options table to create a list of all the balance type codes associated with encumbrances
     *
     * @return an array of encumbrance balance type codes
     */
    protected Collection<String> getEncumbranceBaseBudgetBalanceTypeCodes() {
        if (encumbranceBaseBudgetBalanceTypeCodes == null) {
            loadConstantsFromOptions();
        }
        return encumbranceBaseBudgetBalanceTypeCodes;
    }

    /**
     * Uses the DAO to count the number of balances associated with the given fiscal year
     *
     * @param year a fiscal year to count balances for
     * @return an integer with the number of balances
     */
    @Override
    public int countBalancesForFiscalYear(Integer year) {
        return balanceDao.countBalancesForFiscalYear(year);
    }

    /**
     * Uses the DAO to count the number of balances associated with the given fiscal year and all specified  charts
     *
     * @param year   a fiscal year to count balances for
     * @param charts a list of specified charts
     * @return an integer with the number of balances
     */
    @Override
    public int countBalancesForFiscalYear(Integer year, List<String> charts) {
        return balanceDao.countBalancesForFiscalYear(year, charts);
    }

    /**
     * This method returns all of the balances specifically for the nominal activity closing job
     *
     * @param year year to find balances for
     * @return an Iterator of nominal activity balances
     */
    @Override
    public Iterator<Balance> findNominalActivityBalancesForFiscalYear(Integer year) {
        // generate List of nominal activity object type codes
        List<String> nominalActivityObjectTypeCodes =
                objectTypeService.getNominalActivityClosingAllowedObjectTypes(year);
        SystemOptions currentYearOptions = optionsService.getCurrentYearOptions();
        return balanceDao.findNominalActivityBalancesForFiscalYear(year, nominalActivityObjectTypeCodes,
                currentYearOptions);
    }

    /**
     * This method returns all of the balances specifically for the nominal activity closing job when annual closing
     * charts are specified
     *
     * @param year   year to find balances for
     * @param charts list of charts to find balances for
     * @return an Iterator of nominal activity balances
     */
    @Override
    public Iterator<Balance> findNominalActivityBalancesForFiscalYear(Integer year, List<String> charts) {
        // generate List of nominal activity object type codes
        List<String> nominalActivityObjectTypeCodes =
                objectTypeService.getNominalActivityClosingAllowedObjectTypes(year);
        SystemOptions currentYearOptions = optionsService.getCurrentYearOptions();
        return balanceDao.findNominalActivityBalancesForFiscalYear(year, nominalActivityObjectTypeCodes,
                currentYearOptions, charts);
    }

    /**
     * Returns all the balances to be forwarded for the "cumulative" rule
     *
     * @param year the fiscal year to find balances for
     * @return an Iterator of balances to process for the cumulative/active balance forward process
     */
    @Override
    public Iterator<Balance> findCumulativeBalancesToForwardForFiscalYear(Integer year) {
        List<String> cumulativeForwardBalanceObjectTypes = objectTypeService.getCumulativeForwardBalanceObjectTypes(
                year);
        Collection<String> fundGroupsForCumulativeBalanceForwarding = parameterService.getParameterValuesAsString(
                BalanceForwardStep.class, BalanceService.FUND_GROUPS_FOR_INCEPTION_TO_DATE_REPORTING);
        Collection<String> subFundGroupsForCumulativeBalanceForwardingArray = parameterService
                .getParameterValuesAsString(BalanceForwardStep.class, SUB_FUND_GROUPS_FOR_INCEPTION_TO_DATE_REPORTING);
        Collection<String> cumulativeBalanceForwardBalanceTypesArray = parameterService.getParameterValuesAsString(
                BalanceForwardStep.class, GLParameterConstants.BALANCE_TYPES_TO_ROLL_FORWARD_FOR_INCOME_EXPENSE);
        Iterator<Balance> balances = balanceDao.findCumulativeBalancesToForwardForFiscalYear(year,
                cumulativeForwardBalanceObjectTypes, fundGroupsForCumulativeBalanceForwarding,
                subFundGroupsForCumulativeBalanceForwardingArray, cumulativeBalanceForwardBalanceTypesArray);

        FilteringBalanceIterator filteredBalances = getFilteringBalanceIterator();
        filteredBalances.setBalancesSource(balances);

        return filteredBalances;
    }

    /**
     * Returns all the balances to be forwarded for the "cumulative" rule
     *
     * @param year   the fiscal year to find balances for
     * @param charts charts to find balances for
     * @return an Iterator of balances to process for the cumulative/active balance forward process
     */
    @Override
    public Iterator<Balance> findCumulativeBalancesToForwardForFiscalYear(Integer year, List<String> charts) {
        List<String> cumulativeForwardBalanceObjectTypes = objectTypeService.getCumulativeForwardBalanceObjectTypes(
                year);
        Collection<String> fundGroupsForCumulativeBalanceForwarding = parameterService.getParameterValuesAsString(
                BalanceForwardStep.class, BalanceService.FUND_GROUPS_FOR_INCEPTION_TO_DATE_REPORTING);
        Collection<String> subFundGroupsForCumulativeBalanceForwardingArray = parameterService
                .getParameterValuesAsString(BalanceForwardStep.class, SUB_FUND_GROUPS_FOR_INCEPTION_TO_DATE_REPORTING);
        Collection<String> cumulativeBalanceForwardBalanceTypesArray = parameterService.getParameterValuesAsString(
                BalanceForwardStep.class, GLParameterConstants.BALANCE_TYPES_TO_ROLL_FORWARD_FOR_INCOME_EXPENSE);
        Iterator<Balance> balances = balanceDao.findCumulativeBalancesToForwardForFiscalYear(year,
                cumulativeForwardBalanceObjectTypes, fundGroupsForCumulativeBalanceForwarding,
                subFundGroupsForCumulativeBalanceForwardingArray, cumulativeBalanceForwardBalanceTypesArray, charts);

        FilteringBalanceIterator filteredBalances = getFilteringBalanceIterator();
        filteredBalances.setBalancesSource(balances);

        return filteredBalances;
    }

    /**
     * Returns all the balances specifically to be processed by the balance forwards job for the "general" rule
     *
     * @param year the fiscal year to find balances for
     * @return an Iterator of balances to process for the general balance forward process
     */
    @Override
    public Iterator<Balance> findGeneralBalancesToForwardForFiscalYear(Integer year) {
        List<String> generalForwardBalanceObjectTypes = objectTypeService.getGeneralForwardBalanceObjectTypes(year);
        Collection<String> generalBalanceForwardBalanceTypesArray = parameterService.getParameterValuesAsString(
                BalanceForwardStep.class, GLParameterConstants.BALANCE_TYPES_TO_ROLL_FORWARD_FOR_BALANCE_SHEET);
        Iterator<Balance> balances = balanceDao.findGeneralBalancesToForwardForFiscalYear(year,
                generalForwardBalanceObjectTypes, generalBalanceForwardBalanceTypesArray);

        FilteringBalanceIterator filteredBalances = SpringContext.getBean(FilteringBalanceIterator.class,
                GeneralLedgerConstants.GL_BALANCE_TOTAL_NOT_ZERO_ITERATOR);
        filteredBalances.setBalancesSource(balances);

        return filteredBalances;
    }

    /**
     * Returns all the balances specifically to be processed by the balance forwards job for the "general" rule
     *
     * @param year   the fiscal year to find balances for
     * @param charts charts to find balances for
     * @return an Iterator of balances to process for the general balance forward process
     */
    @Override
    public Iterator<Balance> findGeneralBalancesToForwardForFiscalYear(Integer year, List<String> charts) {
        List<String> generalForwardBalanceObjectTypes = objectTypeService.getGeneralForwardBalanceObjectTypes(year);
        Collection<String> generalBalanceForwardBalanceTypesArray = parameterService.getParameterValuesAsString(
                BalanceForwardStep.class, GLParameterConstants.BALANCE_TYPES_TO_ROLL_FORWARD_FOR_BALANCE_SHEET);
        Iterator<Balance> balances = balanceDao.findGeneralBalancesToForwardForFiscalYear(year,
                generalForwardBalanceObjectTypes, generalBalanceForwardBalanceTypesArray, charts);

        FilteringBalanceIterator filteredBalances = SpringContext.getBean(FilteringBalanceIterator.class,
                GeneralLedgerConstants.GL_BALANCE_TOTAL_NOT_ZERO_ITERATOR);
        filteredBalances.setBalancesSource(balances);

        return filteredBalances;
    }

    /**
     * Returns all of the balances to be forwarded for the organization reversion process
     *
     * @param year      the year of balances to find
     * @param endOfYear whether the organization reversion process is running end of year (before the fiscal year
     *                  change over) or beginning of year (after the fiscal year change over)
     * @return an iterator of balances to put through the strenuous organization reversion process
     */
    @Override
    public Iterator<Balance> findOrganizationReversionBalancesForFiscalYear(Integer year, boolean endOfYear) {
        SystemOptions options = optionsService.getOptions(year);
        List<ParameterEvaluator> parameterEvaluators = new ArrayList<>();

        int i = 1;
        boolean moreParams = true;
        while (moreParams) {
            if (parameterService.parameterExists(OrganizationReversion.class, PARAMETER_PREFIX + i)) {
                ParameterEvaluator parameterEvaluator = parameterEvaluatorService
                        .getParameterEvaluator(OrganizationReversion.class, PARAMETER_PREFIX + i);
                parameterEvaluators.add(parameterEvaluator);
            } else {
                moreParams = false;
            }
            i++;
        }
        return balanceDao.findOrganizationReversionBalancesForFiscalYear(year, endOfYear, options, parameterEvaluators);
    }

    protected List<String> getEncumbranceBalanceTypes(Map fieldValues) {
        List<String> encumbranceBalanceTypes = null;
        if (fieldValues.containsKey(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)) {
            // the year should be part of the results for both the cash balance and regular balance lookupables
            String universityFiscalYearStr = (String) fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR);
            Integer universityFiscalYear = new Integer(universityFiscalYearStr);
            encumbranceBalanceTypes = balanceTypService.getEncumbranceBalanceTypes(universityFiscalYear);
        }
        return encumbranceBalanceTypes;
    }

    public FilteringBalanceIterator getFilteringBalanceIterator() {
        return SpringContext.getBean(FilteringBalanceIterator.class,
                GeneralLedgerConstants.GL_BALANCE_ANNUAL_AND_CG_TOTAL_NOT_ZERO_ITERATOR);
    }

    public void setObjectTypeService(ObjectTypeService objectTypeService) {
        this.objectTypeService = objectTypeService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setBalanceTypService(BalanceTypeService balanceTypService) {
        this.balanceTypService = balanceTypService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setParameterEvaluatorService(ParameterEvaluatorService parameterEvaluatorService) {
        this.parameterEvaluatorService = parameterEvaluatorService;
    }
}
