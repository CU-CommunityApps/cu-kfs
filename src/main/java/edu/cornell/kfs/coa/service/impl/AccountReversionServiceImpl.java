/*
 * Copyright 2006 The Kuali Foundation
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
package edu.cornell.kfs.coa.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.ReversionCategory;
import edu.cornell.kfs.coa.dataaccess.AccountReversionDao;
import edu.cornell.kfs.coa.service.AccountReversionService;
import edu.cornell.kfs.gl.batch.service.ReversionCategoryLogic;
import edu.cornell.kfs.gl.batch.service.impl.GenericReversionCategory;

/**
 * 
 * This service implementation is the default implementation of the OrganizationReversion service that is delivered with Kuali.
 */

public class AccountReversionServiceImpl implements AccountReversionService {
	private static final Logger LOG = LogManager.getLogger(AccountReversionServiceImpl.class);

    private AccountReversionDao accountReversionDao;
    private BusinessObjectService businessObjectService;

    /**
     * @see org.kuali.kfs.coa.service.OrganizationReversionService#getByPrimaryId(java.lang.Integer, java.lang.String,
     *      java.lang.String)
     */
    public AccountReversion getByPrimaryId(Integer fiscalYear, String chartCode, String accountNumber) {
        LOG.debug("getByPrimaryId() started");
        return accountReversionDao.getByPrimaryId(fiscalYear, chartCode, accountNumber);
    }

    /**
     * @see org.kuali.kfs.coa.service.OrganizationReversionService#getCategories()
     */
    public Map<String, ReversionCategoryLogic> getCategories() {
        LOG.debug("getCategories() started");

        Map<String, ReversionCategoryLogic> categories = new HashMap<String, ReversionCategoryLogic>();

        Collection cats = accountReversionDao.getCategories();

        for (Iterator iter = cats.iterator(); iter.hasNext();) {
            ReversionCategory rc = (ReversionCategory) iter.next();

            String categoryCode = rc.getReversionCategoryCode();

            Map<String, ReversionCategoryLogic> beanMap = SpringContext.getBeansOfType(ReversionCategoryLogic.class);
            if (beanMap.containsKey("gl" + categoryCode + "AccountReversionCategory")) {
                LOG.info("Found Account Reversion Category Logic for gl" + categoryCode + "AccountReversionCategory");
                categories.put(categoryCode, beanMap.get("gl" + categoryCode + "AccountReversionCategory"));
            }
            else {
                LOG.info("No Account Reversion Category Logic for gl" + categoryCode + "AccountReversionCategory; using generic");
                GenericReversionCategory cat = SpringContext.getBean(GenericReversionCategory.class);
                cat.setCategoryCode(categoryCode);
                cat.setCategoryName(rc.getReversionCategoryName());
                categories.put(categoryCode, (ReversionCategoryLogic) cat);
            }
        }
        return categories;
    }

    /**
     * 
     * @see org.kuali.kfs.coa.service.OrganizationReversionService#getCategoryList()
     */
    public List<ReversionCategory> getCategoryList() {
        LOG.debug("getCategoryList() started");

        return accountReversionDao.getCategories();
    }

    /**
     * @see org.kuali.kfs.coa.service.OrganizationReversionService#isCategoryActive(java.lang.String)
     */
    public boolean isCategoryActive(String categoryCode) {
        Map<String, Object> pkMap = new HashMap<String, Object>();
        pkMap.put("reversionCategoryCode", categoryCode);
        final ReversionCategory category = (ReversionCategory)businessObjectService.findByPrimaryKey(ReversionCategory.class, pkMap);
        if (category == null) return false;
        return category.isActive();
    }

    /**
     * @see org.kuali.kfs.coa.service.OrganizationReversionService#isCategoryActiveByName(java.lang.String)
     */
    public boolean isCategoryActiveByName(String categoryName) {
        Map<String, Object> fieldMap = new HashMap<String, Object>();
        fieldMap.put("reversionCategoryName", categoryName);
        final Collection categories = businessObjectService.findMatching(ReversionCategory.class, fieldMap);
        final Iterator categoriesIterator = categories.iterator();
        ReversionCategory category = null;
        while (categoriesIterator.hasNext()) {
            category = (ReversionCategory)categoriesIterator.next();
        }
        if (category == null) return false;
        return category.isActive();
    }
    

	/**
	 * @see edu.cornell.kfs.coa.service.AccountReversionService#getAccountReversionsByCashReversionAcount(java.lang.String, java.lang.String)
	 */
	@Override
	public List<AccountReversion> getAccountReversionsByCashReversionAcount(String cashReversionFinancialChartOfAccountsCode, String cashReversionAccountNumber) {
		return accountReversionDao.getAccountReversionsByCashReversionAcount(cashReversionFinancialChartOfAccountsCode, cashReversionAccountNumber);
	}

	/**
	 * @see edu.cornell.kfs.coa.service.AccountReversionService#getAccountReversionsByBudgetReversionAcount(java.lang.String, java.lang.String)
	 */
	@Override
	public List<AccountReversion> getAccountReversionsByBudgetReversionAcount(String budgetReversionChartOfAccountsCode, String budgetReversionAccountNumber) {
		return accountReversionDao.getAccountReversionsByBudgetReversionAcount(budgetReversionChartOfAccountsCode, budgetReversionAccountNumber);
	}

	/**
	 * @see edu.cornell.kfs.coa.service.AccountReversionService#getAccountReversionsByChartAndAccount(java.lang.String, java.lang.String)
	 */
	@Override
	public List<AccountReversion> getAccountReversionsByChartAndAccount(String chartOfAccountsCode, String accountNumber) {
		return accountReversionDao.getAccountReversionsByChartAndAccount(chartOfAccountsCode, accountNumber);
	}

	@Override
	public void forciblyClearCache() {
		accountReversionDao.forciblyClearCache();
	}

    /**
     * 
     * This method injects the OrganizationReversionDao
     * @param orgDao
     */
    public void setAccountReversionDao(AccountReversionDao acctDao) {
        accountReversionDao = acctDao;
    }
    
    /**
     * Sets an implementation of the business object service
     * @param boService the implementation of the BusinessObjectService to set
     */
    public void setBusinessObjectService(BusinessObjectService boService) {
        this.businessObjectService = boService;
    }

}
