/*
 * Copyright 2005-2006 The Kuali Foundation
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
package edu.cornell.kfs.coa.dataaccess;

import java.util.List;

import org.kuali.kfs.coa.businessobject.OrganizationReversionCategory;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.ReversionCategory;


/**
 * This interface provides data access methods for {@link AccountReversion} and {@link AccountReversionCategory}
 */
public interface AccountReversionDao {

    /**
     * Retrieves an AccountReversion by primary key.
     * 
     * @param universityFiscalYear - part of composite key
     * @param financialChartOfAccountsCode - part of composite key
     * @param organizationCode - part of composite key
     * @return {@link AccountReversion} based on primary key
     */
    public AccountReversion getByPrimaryId(Integer universityFiscalYear, String financialChartOfAccountsCode, String accountNumber);
    
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

    /**
     * Get all the categories {@link OrganizationReversionCategory}
     * 
     * @return list of categories
     */
    public List<ReversionCategory> getCategories();
    
    public void forciblyClearCache();
}
