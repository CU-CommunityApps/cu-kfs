package edu.cornell.kfs.gl.dataaccess.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.OrganizationReversion;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.coa.service.ObjectTypeService;
import org.kuali.kfs.coa.service.SubFundGroupService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.BalanceForwardStep;
import org.kuali.kfs.gl.batch.service.FilteringBalanceIterator;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.dataaccess.impl.BalanceDaoOjb;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.coa.businessobject.Reversion;
import edu.cornell.kfs.gl.dataaccess.CuBalanceDao;

public class CuBalanceDaoOjb extends BalanceDaoOjb implements CuBalanceDao {
	 private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuBalanceDaoOjb.class);
	 protected static final String PARAMETER_PREFIX = "SELECTION_";
	 
	 private ParameterEvaluatorService parameterEvaluatorService;
	 private OptionsService optionsService;
	 private BalanceTypeService balanceTypService;
	 
	 
	 /**
	     * this is for KFSPTS-1786 begin
	     */ 
	    
	    public Collection<Balance> getAccountBalance(Map<String, String> input) {
	        Criteria criteria = new Criteria();
	       for(String key:input.keySet()){
	           criteria.addEqualTo(key, input.get(key));
	       }
	        
	        Collection<Balance> balance = getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(Balance.class, criteria));
	        return balance;
	        
	    }
	    public Collection<Balance> getAccountBalance(Map<String, String> input,Collection objectTypeCode) {
	        Criteria criteria = new Criteria();
	       for(String key:input.keySet()){
	           criteria.addEqualTo(key, input.get(key));
	       }
	       criteria.addIn(KFSPropertyConstants.OBJECT_TYPE_CODE, objectTypeCode);
	        Collection<Balance> balance = getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(Balance.class, criteria));
	        return balance;
	        
	    }
	    
	    /**
	     * this is for KFSPTS-1786 end
	     */ 

    /**
     * Returns the count of balances for a given fiscal year and specified charts; this method is used for year end job reporting
     * @param year the university fiscal year to count balances for
     * @param list of charts to count balances for
     * @return an int with the count of balances for all charts specied in that fiscal year
     * @see org.kuali.kfs.gl.dataaccess.BalanceDao#countBalancesForFiscalYear(java.lang.Integer, java.util.List)
     */
    @Override
    public int countBalancesForFiscalYear(Integer year, Collection<String> charts) {
        LOG.debug("countBalancesForFiscalYear(year, charts) started");
    
        Criteria c = new Criteria();
        c.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
        c.addIn(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, charts);
        QueryByCriteria query = QueryFactory.newQuery(Balance.class, c);
    
        return getPersistenceBrokerTemplate().getCount(query);
    }
    
    /**
     * Finds all of the balances for the fiscal year and specified charts that should be processed by nominal activity closing
     * 
     * @param year the university fiscal year of balances to find
     * @param charts list of charts to find balances for
     * @return an Iterator of Balances to process
     * @see org.kuali.kfs.gl.dataaccess.BalanceDao#findNominalActivityBalancesForFiscalYear(java.lang.Integer, java.util.List)
     */
    @Override
    public Iterator<Balance> findNominalActivityBalancesForFiscalYear(Integer year, Collection<String> charts) {
        LOG.debug("findNominalActivityBalancesForFiscalYear(year, charts) started");
    
        SystemOptions currentYearOptions = SpringContext.getBean(OptionsService.class).getCurrentYearOptions();
    
        // generate List of nominal activity object type codes
        ObjectTypeService objectTypeService = SpringContext.getBean(ObjectTypeService.class);
    
        Criteria c = new Criteria();
        c.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
        c.addEqualTo(KFSPropertyConstants.BALANCE_TYPE_CODE, currentYearOptions.getActualFinancialBalanceTypeCd());
        c.addIn(KFSPropertyConstants.OBJECT_TYPE_CODE, objectTypeService.getNominalActivityClosingAllowedObjectTypes(year));
        c.addIn(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, charts);
        c.addNotEqualTo("accountLineAnnualBalanceAmount", KualiDecimal.ZERO);
        
        QueryByCriteria query = QueryFactory.newQuery(Balance.class, c);
        query.addOrderByAscending(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
        query.addOrderByAscending(KFSPropertyConstants.ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.SUB_ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.SUB_OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.BALANCE_TYPE_CODE);
        query.addOrderByAscending(KFSPropertyConstants.OBJECT_TYPE_CODE);
        
        return getPersistenceBrokerTemplate().getIteratorByQuery(query);
    }
    
    /**
     * Returns all of the balances that should be processed by the BalanceForward year end job under the general rule
     * 
     * @param the university fiscal year to find balances for
     * @param charts to find balances for
     * @return an Iterator of Balances to process
     * @see org.kuali.kfs.gl.dataaccess.BalanceDao#findCumulativeBalancesToForwardForFiscalYear(java.lang.Integer, java.util.List)
     */
    public Iterator<Balance> findGeneralBalancesToForwardForFiscalYear(Integer year, Collection<String> charts) {
        ObjectTypeService objectTypeService = SpringContext.getBean(ObjectTypeService.class);

        String[] generalBalanceForwardBalanceTypesArray = SpringContext.getBean(ParameterService.class)
                .getParameterValuesAsString(BalanceForwardStep.class, 
                        GeneralLedgerConstants.BalanceForwardRule.BALANCE_TYPES_TO_ROLL_FORWARD_FOR_BALANCE_SHEET).toArray(new String[] {});
        List<String> generalBalanceForwardBalanceTypes = new ArrayList<String>();
        for (String bt : generalBalanceForwardBalanceTypesArray) {
            generalBalanceForwardBalanceTypes.add(bt);
        }

        Criteria c = new Criteria();
        c.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
        c.addIn(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, charts);
        c.addIn(KFSPropertyConstants.BALANCE_TYPE_CODE, generalBalanceForwardBalanceTypes);
        c.addIn(KFSPropertyConstants.OBJECT_TYPE_CODE, objectTypeService.getGeneralForwardBalanceObjectTypes(year));

        QueryByCriteria query = QueryFactory.newQuery(Balance.class, c);
        query.addOrderByAscending(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
        query.addOrderByAscending(KFSPropertyConstants.ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.SUB_ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.SUB_OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.BALANCE_TYPE_CODE);
        query.addOrderByAscending(KFSPropertyConstants.OBJECT_TYPE_CODE);

        Iterator<Balance> balances = getPersistenceBrokerTemplate().getIteratorByQuery(query);

        Map<String, FilteringBalanceIterator> balanceIterators = SpringContext.getBeansOfType(FilteringBalanceIterator.class);
        FilteringBalanceIterator filteredBalances = balanceIterators.get("glBalanceTotalNotZeroIterator");
        filteredBalances.setBalancesSource(balances);

        return filteredBalances;
    }
    
    /**
     * Returns all of the balances that should be processed by the BalanceForward year end job under the active rule
     * 
     * @param year the university fiscal year to find balances for
     * @param charts charts to find balances for
     * @return an Iterator of Balances to process
     * @see org.kuali.kfs.gl.dataaccess.BalanceDao#findGeneralBalancesToForwardForFiscalYear(java.lang.Integer, java.util.List)
     */
    public Iterator<Balance> findCumulativeBalancesToForwardForFiscalYear(Integer year, Collection<String> charts) {
        ObjectTypeService objectTypeService = SpringContext.getBean(ObjectTypeService.class);
        SubFundGroupService subFundGroupService = SpringContext.getBean(SubFundGroupService.class);

        final String[] subFundGroupsForCumulativeBalanceForwardingArray = SpringContext.getBean(ParameterService.class)
                .getParameterValuesAsString(BalanceForwardStep.class, 
                        GeneralLedgerConstants.BalanceForwardRule.SUB_FUND_GROUPS_FOR_INCEPTION_TO_DATE_REPORTING).toArray(new String[] {});
        List<String> subFundGroupsForCumulativeBalanceForwarding = new ArrayList<String>();
        for (String subFundGroup : subFundGroupsForCumulativeBalanceForwardingArray) {
            subFundGroupsForCumulativeBalanceForwarding.add(subFundGroup);
        }

        String[] cumulativeBalanceForwardBalanceTypesArray = SpringContext.getBean(ParameterService.class).
                getParameterValuesAsString(BalanceForwardStep.class, 
                        GeneralLedgerConstants.BalanceForwardRule.BALANCE_TYPES_TO_ROLL_FORWARD_FOR_INCOME_EXPENSE).toArray(new String[] {});
        List<String> cumulativeBalanceForwardBalanceTypes = new ArrayList<String>();
        for (String bt : cumulativeBalanceForwardBalanceTypesArray) {
            cumulativeBalanceForwardBalanceTypes.add(bt);
        }

        Criteria c = new Criteria();
        c.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
        c.addIn(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, charts);
        c.addIn(KFSPropertyConstants.BALANCE_TYPE_CODE, cumulativeBalanceForwardBalanceTypes);
        c.addIn(KFSPropertyConstants.OBJECT_TYPE_CODE, objectTypeService.getCumulativeForwardBalanceObjectTypes(year));

        Criteria forCGCrit = new Criteria();
        if (SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(Account.class, KFSConstants.ChartApcParms.ACCOUNT_FUND_GROUP_DENOTES_CG)) {
            for (String value : subFundGroupService.getContractsAndGrantsDenotingValues()) {
                forCGCrit.addEqualTo("priorYearAccount.subFundGroup.fundGroupCode", value);
            }
        } else {
            for (String value : subFundGroupService.getContractsAndGrantsDenotingValues()) {
                forCGCrit.addEqualTo("priorYearAccount.subFundGroupCode", value);
            }
        }
 
        Criteria subFundGroupCrit = new Criteria();
        subFundGroupCrit.addIn("priorYearAccount.subFundGroupCode", subFundGroupsForCumulativeBalanceForwarding);
        forCGCrit.addOrCriteria(subFundGroupCrit);
        c.addAndCriteria(forCGCrit);

        QueryByCriteria query = QueryFactory.newQuery(Balance.class, c);
        query.addOrderByAscending(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
        query.addOrderByAscending(KFSPropertyConstants.ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.SUB_ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.SUB_OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.BALANCE_TYPE_CODE);
        query.addOrderByAscending(KFSPropertyConstants.OBJECT_TYPE_CODE);

        Iterator<Balance> balances = getPersistenceBrokerTemplate().getIteratorByQuery(query);

        FilteringBalanceIterator filteredBalances = SpringContext.getBean(FilteringBalanceIterator.class,"glBalanceAnnualAndCGTotalNotZeroIterator");
        filteredBalances.setBalancesSource(balances);

        return filteredBalances;
    }
    
    	 /**
     * Returns a list of balances to return for the Organization Reversion year end job to process
     * 
     * @param the university fiscal year to find balances for
     * @param endOfYear if true, use currrent year accounts, otherwise use prior year accounts
     * @return an Iterator of Balances to process
     * @see org.kuali.kfs.gl.dataaccess.BalanceDao#findOrganizationReversionBalancesForFiscalYear(java.lang.Integer, boolean)
     */
    public Iterator<Balance> findReversionBalancesForFiscalYear(Integer year, boolean endOfYear) {
        LOG.debug("findReversionBalancesForFiscalYear() started");
        Criteria c = new Criteria();
        c.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        Map<Integer, String> parsedRules = new TreeMap<Integer, String>();
        int i = 1;
        boolean moreParams = true;
        while (moreParams) {
            if (parameterService.parameterExists(Reversion.class, PARAMETER_PREFIX + i)) {
            	ParameterEvaluator parameterEvaluator = parameterEvaluatorService.getParameterEvaluator(Reversion.class, PARAMETER_PREFIX + i);
                String currentRule = parameterEvaluator.getValue();
                if (endOfYear) {
                    currentRule = currentRule.replaceAll("account\\.", "priorYearAccount.");
                }
                if (StringUtils.isNotBlank(currentRule)) {
                    String propertyName = StringUtils.substringBefore(currentRule, "=");
                    List<String> ruleValues = Arrays.asList(StringUtils.substringAfter(currentRule, "=").split(";"));
                    if (propertyName != null && propertyName.length() > 0 && ruleValues.size() > 0 && !StringUtils.isBlank(ruleValues.get(0))) {
                        if (parameterEvaluator.constraintIsAllow()) {
                            c.addIn(propertyName, ruleValues);
                        }
                        else {
                            c.addNotIn(propertyName, ruleValues);
                        }
                    }
                }
            }
            else {
                moreParams = false;
            }
            i++;
        }
        // we only ever calculate on CB, AC, and encumbrance types, so let's only select those
        SystemOptions options = SpringContext.getBean(OptionsService.class).getOptions(year);
        List ReversionBalancesToSelect = new ArrayList();
        ReversionBalancesToSelect.add(options.getActualFinancialBalanceTypeCd());
        ReversionBalancesToSelect.add(options.getFinObjTypeExpenditureexpCd());
        ReversionBalancesToSelect.add(options.getCostShareEncumbranceBalanceTypeCd());
        ReversionBalancesToSelect.add(options.getIntrnlEncumFinBalanceTypCd());
        ReversionBalancesToSelect.add(KFSConstants.BALANCE_TYPE_CURRENT_BUDGET);
        c.addIn(KFSPropertyConstants.BALANCE_TYPE_CODE, ReversionBalancesToSelect);
        //c.addLike("accountNumber", "L013%");
        QueryByCriteria query = QueryFactory.newQuery(Balance.class, c);
        query.addOrderByAscending(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
        query.addOrderByAscending(KFSPropertyConstants.ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.SUB_ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.SUB_OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.BALANCE_TYPE_CODE);
        query.addOrderByAscending(KFSPropertyConstants.OBJECT_TYPE_CODE);

        return getPersistenceBrokerTemplate().getIteratorByQuery(query);
    }
    
	/**
	 * Gets the optionsService
	 * 
	 * @return optionsService
	 */
	public OptionsService getOptionsService() {
		return optionsService;
	}

	/**
	 * Sets the optionsService
	 * 
	 * @param optionsService
	 */
	public void setOptionsService(OptionsService optionsService) {
		this.optionsService = optionsService;
	}

	/**
	 * Gets the balanceTypService
	 * @return balanceTypService
	 */
	public BalanceTypeService getBalanceTypService() {
		return balanceTypService;
	}

	/**
	 * Sets the balanceTypService.
	 * 
	 * @param balanceTypService
	 */
	public void setBalanceTypService(BalanceTypeService balanceTypService) {
		this.balanceTypService = balanceTypService;
	}

	/**
	 * Gets the parameterEvaluatorService.
	 * 
	 * @return parameterEvaluatorService
	 */
	public ParameterEvaluatorService getParameterEvaluatorService() {
		return parameterEvaluatorService;
	}

	/**
	 * Sets the parameterEvaluatorService.
	 * 
	 * @param parameterEvaluatorService
	 */
	public void setParameterEvaluatorService(
			ParameterEvaluatorService parameterEvaluatorService) {
		this.parameterEvaluatorService = parameterEvaluatorService;
	}
    
}
