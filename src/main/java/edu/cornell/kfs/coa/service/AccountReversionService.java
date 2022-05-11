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
package edu.cornell.kfs.coa.service;

import java.util.List;
import java.util.Map;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.ReversionCategory;
import edu.cornell.kfs.gl.batch.service.ReversionCategoryLogic;


/**
 * 
 * This service interface defines methods necessary for retrieving fully populated OrganizationReversion business objects from the database
 * that are necessary for transaction processing in the application. It also defines methods to retrieve org reversion categories
 */
public interface AccountReversionService {
    /**
     * Get an organization reversion record
     * 
     * @param fiscalYear Fiscal Year
     * @param chartCode Chart
     * @param organizationCode Organization code
     * @return Org Reversion record
     */
    public AccountReversion getByPrimaryId(Integer fiscalYear, String chartCode, String accountNumber);

    /**
     * Get org reversion categories
     * 
     * @return map of org reversion categories
     */
    public Map<String, ReversionCategoryLogic> getCategories();

    /**
     * List of reversion categories in order of the sort code
     * 
     * @return list of org reversion category codes
     */
    public List<ReversionCategory> getCategoryList();
    
    /**
     * Determines if the given category code represents an active category
     * @param categoryCode the category code to check
     * @return true if the given category code represents an active category; false otherwise
     */
    public boolean isCategoryActive(String categoryCode);
    
    /**
     * Determines if the given category name represents an active category
     * @param categoryName the category name to check
     * @return true if the given category name represents an active category; false otherwise
     */
    public boolean isCategoryActiveByName(String categoryName);
    
    /**
     * Retrieves all AccountReversion entries that have the given CashReversionAcount.
     * 
     * @param cashReversionFinancialChartOfAccountsCode
     * @param cashReversionAccountNumber
     * @return a list of all AccountReversion entries that have the given CashReversionAcount
     */
    public List<AccountReversion> getAccountReversionsByCashReversionAcount(String cashReversionFinancialChartOfAccountsCode, String cashReversionAccountNumber);
    
    /**
     * Retrieves all AccountReversion entries that have the given BudgetReversionAccount.
     * 
     * @param budgetReversionChartOfAccountsCode
     * @param budgetReversionAccountNumber
     * @return a list of all AccountReversion entries that have the given BudgetReversionAccount
     */
    public List<AccountReversion> getAccountReversionsByBudgetReversionAcount(String budgetReversionChartOfAccountsCode, String budgetReversionAccountNumber);
    
    /**
     * Retrieve all account Reversions with the given chart and account number
     * 
     * @param chartOfAccountsCode
     * @param accountNumber
     * @return a list of account Reversions with the given chart and account number
     */
    public List<AccountReversion> getAccountReversionsByChartAndAccount(String chartOfAccountsCode, String accountNumber);

    public void forciblyClearCache();
}
