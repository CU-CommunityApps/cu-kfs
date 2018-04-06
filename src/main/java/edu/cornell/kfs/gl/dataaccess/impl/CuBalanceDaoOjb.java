package edu.cornell.kfs.gl.dataaccess.impl;

import edu.cornell.kfs.gl.dataaccess.CuBalanceDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.dataaccess.impl.BalanceDaoOjb;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CuBalanceDaoOjb extends BalanceDaoOjb implements CuBalanceDao {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuBalanceDaoOjb.class);

    /**
     * Returns a list of balances to return for the Organization Reversion year end job to process
     * 
     * @param year the university fiscal year to find balances for
     * @param endOfYear if true, use currrent year accounts, otherwise use prior year accounts
     * @return an Iterator of Balances to process
     * @see org.kuali.kfs.gl.dataaccess.BalanceDao#findOrganizationReversionBalancesForFiscalYear(java.lang.Integer, boolean,
     *      org.kuali.kfs.sys.businessobject.SystemOptions, java.util.List)
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public Iterator<Balance> findReversionBalancesForFiscalYear(Integer year, boolean endOfYear,
            SystemOptions options, List<ParameterEvaluator> parameterEvaluators) {
        LOG.debug("findReversionBalancesForFiscalYear() started");
        Criteria c = new Criteria();
        c.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);

        for (ParameterEvaluator parameterEvaluator : parameterEvaluators) {

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
                    } else {
                        c.addNotIn(propertyName, ruleValues);
                    }
                }
            }
        }
        // we only ever calculate on CB, AC, and encumbrance types, so let's only select those
        List<String> reversionBalancesToSelect = new ArrayList<String>();
        reversionBalancesToSelect.add(options.getActualFinancialBalanceTypeCd());
        reversionBalancesToSelect.add(options.getFinObjTypeExpenditureexpCd());
        reversionBalancesToSelect.add(options.getCostShareEncumbranceBalanceTypeCd());
        reversionBalancesToSelect.add(options.getIntrnlEncumFinBalanceTypCd());
        reversionBalancesToSelect.add(KFSConstants.BALANCE_TYPE_CURRENT_BUDGET);
        c.addIn(KFSPropertyConstants.BALANCE_TYPE_CODE, reversionBalancesToSelect);
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

}
