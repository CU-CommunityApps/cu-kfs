/*
 * Copyright 2005 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.dataaccess.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.ReversionCategory;
import edu.cornell.kfs.coa.dataaccess.AccountReversionDao;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;


/**
 * This class implements the {@link OrganizationReversionDao} data access methods using Ojb
 */
public class AccountReversionDaoOjb extends PlatformAwareDaoBaseOjb implements AccountReversionDao {
	private static final Logger LOG = LogManager.getLogger(AccountReversionDaoOjb.class);

    /**
     * @see org.kuali.kfs.coa.dataaccess.OrganizationReversionDao#getByPrimaryId(java.lang.Integer, java.lang.String,
     *      java.lang.String)
     */
    public AccountReversion getByPrimaryId(Integer universityFiscalYear, String financialChartOfAccountsCode, String accountNumber) {
        LOG.debug("getByPrimaryId() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo("universityFiscalYear", universityFiscalYear);
        criteria.addEqualTo("chartOfAccountsCode", financialChartOfAccountsCode);
        criteria.addEqualTo("accountNumber", accountNumber);

        return (AccountReversion) getPersistenceBrokerTemplate().getObjectByQuery(QueryFactory.newQuery(AccountReversion.class, criteria));
    }

    /**
     * @see org.kuali.kfs.coa.dataaccess.OrganizationReversionDao#getCategories()
     */
    public List<ReversionCategory> getCategories() {
        LOG.debug("getCategories() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo("active", true);
        QueryByCriteria q = QueryFactory.newQuery(ReversionCategory.class, criteria);
        q.addOrderByAscending("reversionSortCode");

        return (List) getPersistenceBrokerTemplate().getCollectionByQuery(q);
    }

    /**
     * @see edu.cornell.kfs.coa.dataaccess.AccountReversionDao#getAccountReversionsByCashReversionAcount(java.lang.String, java.lang.String)
     */
    @Override
    public List<AccountReversion> getAccountReversionsByCashReversionAcount(String cashReversionFinancialChartOfAccountsCode, String cashReversionAccountNumber) {
        LOG.debug("getAccountReversionsByCashReversionAcount() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo(CUKFSPropertyConstants.ACCT_REVERSION_CASH_REVERSION_CHART_OF_ACCT_CODE, cashReversionFinancialChartOfAccountsCode);
        criteria.addEqualTo(CUKFSPropertyConstants.ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER, cashReversionAccountNumber);
        criteria.addEqualTo(CUKFSPropertyConstants.ACCT_REVERSION_ACTIVE, true);
        
        QueryByCriteria q = QueryFactory.newQuery(AccountReversion.class, criteria);
        return (List) getPersistenceBrokerTemplate().getCollectionByQuery(q);
    }


    /**
     * @see edu.cornell.kfs.coa.dataaccess.AccountReversionDao#getAccountReversionsByBudgetReversionAcount(java.lang.String, java.lang.String)
     */
    @Override
    public List<AccountReversion> getAccountReversionsByBudgetReversionAcount(String budgetReversionChartOfAccountsCode, String budgetReversionAccountNumber) {
        LOG.debug("getAccountReversionsByBudgetReversionAcount() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo(CUKFSPropertyConstants.ACCT_REVERSION_BUDGET_REVERSION_CHART_OF_ACCT_CODE, budgetReversionChartOfAccountsCode);
        criteria.addEqualTo(CUKFSPropertyConstants.ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER, budgetReversionAccountNumber);
        criteria.addEqualTo(CUKFSPropertyConstants.ACCT_REVERSION_ACTIVE, true);
        
        QueryByCriteria q = QueryFactory.newQuery(AccountReversion.class, criteria);
        return (List) getPersistenceBrokerTemplate().getCollectionByQuery(q);
    }
    

    /**
     * @see edu.cornell.kfs.coa.dataaccess.AccountReversionDao#getAccountReversionsByChartAndAccount(java.lang.String, java.lang.String)
     */
    @Override
    public List<AccountReversion> getAccountReversionsByChartAndAccount(String chartOfAccountsCode, String accountNumber) {
        LOG.debug("getAccountReversionsByChartAndAccount() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo(CUKFSPropertyConstants.ACCT_REVERSION_CHART_OF_ACCT_CODE, chartOfAccountsCode);
        criteria.addEqualTo(CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER, accountNumber);
        criteria.addEqualTo(CUKFSPropertyConstants.ACCT_REVERSION_ACTIVE, true);
        
        QueryByCriteria q = QueryFactory.newQuery(AccountReversion.class, criteria);
        return (List) getPersistenceBrokerTemplate().getCollectionByQuery(q);
    }
    
    @Override
    public void forciblyClearCache() {
        getPersistenceBrokerTemplate().clearCache();
    }

}
