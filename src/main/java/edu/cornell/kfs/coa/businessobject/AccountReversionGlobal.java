/*
 * Copyright 2007 The Kuali Foundation
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
package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.service.AccountReversionService;

/**
 * The representation of a Global Account Reversion. A Global Account Reversion is made up of three sections: 1. The
 * University Fiscal Year for the Accounts going through reversion, with some account information.
 * 2. A list of the appropriate Object Reversion Details 3. A list of Accounts to apply the Account Reversion to
 */
public class AccountReversionGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {
	private static final Logger LOG = LogManager.getLogger(AccountReversionGlobal.class);

    private String documentNumber;

    private Integer universityFiscalYear;
    private String budgetReversionChartOfAccountsCode;
    private String budgetReversionAccountNumber;
    private Boolean carryForwardByObjectCodeIndicator;
    private String cashReversionFinancialChartOfAccountsCode;
    private String cashReversionAccountNumber;
    private Boolean reversionActiveIndicator;

    private Account cashReversionAccount;
    private Account budgetReversionAccount;
    private Chart budgetReversionChartOfAccounts;
    private Chart cashReversionFinancialChartOfAccounts;
    private SystemOptions universityFiscal;

    private List<AccountReversionGlobalDetail> accountReversionGlobalDetails;
    private List<AccountReversionGlobalAccount> accountReversionGlobalAccounts;

    public AccountReversionGlobal() {
        super();
        accountReversionGlobalDetails = new ArrayList<AccountReversionGlobalDetail>();
        accountReversionGlobalAccounts = new ArrayList<AccountReversionGlobalAccount>();
    }

    /**
     * Gets the budgetReversionAccount attribute.
     * 
     * @return Returns the budgetReversionAccount.
     */
    public Account getBudgetReversionAccount() {
        return budgetReversionAccount;
    }

    /**
     * Gets the documentNumber attribute.
     * 
     * @return Returns the documentNumber.
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Sets the documentNumber attribute value.
     * 
     * @param documentNumber The documentNumber to set.
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * Sets the budgetReversionAccount attribute value.
     * 
     * @param budgetReversionAccount The budgetReversionAccount to set.
     * @deprecated
     */
    public void setBudgetReversionAccount(Account budgetReversionAccount) {
        this.budgetReversionAccount = budgetReversionAccount;
    }

    /**
     * Gets the budgetReversionAccountNumber attribute.
     * 
     * @return Returns the budgetReversionAccountNumber.
     */
    public String getBudgetReversionAccountNumber() {
        return budgetReversionAccountNumber;
    }

    /**
     * Sets the budgetReversionAccountNumber attribute value.
     * 
     * @param budgetReversionAccountNumber The budgetReversionAccountNumber to set.
     */
    public void setBudgetReversionAccountNumber(String budgetReversionAccountNumber) {
        this.budgetReversionAccountNumber = budgetReversionAccountNumber;
    }

    /**
     * Gets the budgetReversionChartOfAccounts attribute.
     * 
     * @return Returns the budgetReversionChartOfAccounts.
     */
    public Chart getBudgetReversionChartOfAccounts() {
        return budgetReversionChartOfAccounts;
    }

    /**
     * Sets the budgetReversionChartOfAccounts attribute value.
     * 
     * @param budgetReversionChartOfAccounts The budgetReversionChartOfAccounts to set.
     * @deprecated
     */
    public void setBudgetReversionChartOfAccounts(Chart budgetReversionChartOfAccounts) {
        this.budgetReversionChartOfAccounts = budgetReversionChartOfAccounts;
    }

    /**
     * Gets the budgetReversionChartOfAccountsCode attribute.
     * 
     * @return Returns the budgetReversionChartOfAccountsCode.
     */
    public String getBudgetReversionChartOfAccountsCode() {
        return budgetReversionChartOfAccountsCode;
    }

    /**
     * Sets the budgetReversionChartOfAccountsCode attribute value.
     * 
     * @param budgetReversionChartOfAccountsCode The budgetReversionChartOfAccountsCode to set.
     */
    public void setBudgetReversionChartOfAccountsCode(String budgetReversionChartOfAccountsCode) {
        this.budgetReversionChartOfAccountsCode = budgetReversionChartOfAccountsCode;
    }

    /**
     * Gets the carryForwardByObjectCodeIndicator attribute.
     * 
     * @return Returns the carryForwardByObjectCodeIndicator.
     */
    public Boolean isCarryForwardByObjectCodeIndicator() {
        return carryForwardByObjectCodeIndicator;
    }

    /**
     * Gets the carryForwardByObjectCodeIndicator attribute: but for other methods that don't like to call "is" and would rather
     * call "get"
     * 
     * @return Returns the carryForwardByObjectCodeIndicator.
     */
    public Boolean getCarryForwardByObjectCodeIndicator() {
        return this.isCarryForwardByObjectCodeIndicator();
    }

    /**
     * Sets the carryForwardByObjectCodeIndicator attribute value.
     * 
     * @param carryForwardByObjectCodeIndicator The carryForwardByObjectCodeIndicator to set.
     */
    public void setCarryForwardByObjectCodeIndicator(Boolean carryForwardByObjectCodeIndicator) {
        this.carryForwardByObjectCodeIndicator = carryForwardByObjectCodeIndicator;
    }

    /**
     * Gets the cashReversionAccount attribute.
     * 
     * @return Returns the cashReversionAccount.
     */
    public Account getCashReversionAccount() {
        return cashReversionAccount;
    }

    /**
     * Sets the cashReversionAccount attribute value.
     * 
     * @param cashReversionAccount The cashReversionAccount to set.
     * @deprecated
     */
    public void setCashReversionAccount(Account cashReversionAccount) {
        this.cashReversionAccount = cashReversionAccount;
    }

    /**
     * Gets the cashReversionAccountNumber attribute.
     * 
     * @return Returns the cashReversionAccountNumber.
     */
    public String getCashReversionAccountNumber() {
        return cashReversionAccountNumber;
    }

    /**
     * Sets the cashReversionAccountNumber attribute value.
     * 
     * @param cashReversionAccountNumber The cashReversionAccountNumber to set.
     */
    public void setCashReversionAccountNumber(String cashReversionAccountNumber) {
        this.cashReversionAccountNumber = cashReversionAccountNumber;
    }

    /**
     * Gets the cashReversionFinancialChartOfAccounts attribute.
     * 
     * @return Returns the cashReversionFinancialChartOfAccounts.
     */
    public Chart getCashReversionFinancialChartOfAccounts() {
        return cashReversionFinancialChartOfAccounts;
    }

    /**
     * Sets the cashReversionFinancialChartOfAccounts attribute value.
     * 
     * @param cashReversionFinancialChartOfAccounts The cashReversionFinancialChartOfAccounts to set.
     * @deprecated
     */
    public void setCashReversionFinancialChartOfAccounts(Chart cashReversionFinancialChartOfAccounts) {
        this.cashReversionFinancialChartOfAccounts = cashReversionFinancialChartOfAccounts;
    }

    /**
     * Gets the cashReversionFinancialChartOfAccountsCode attribute.
     * 
     * @return Returns the cashReversionFinancialChartOfAccountsCode.
     */
    public String getCashReversionFinancialChartOfAccountsCode() {
        return cashReversionFinancialChartOfAccountsCode;
    }

    /**
     * Sets the cashReversionFinancialChartOfAccountsCode attribute value.
     * 
     * @param cashReversionFinancialChartOfAccountsCode The cashReversionFinancialChartOfAccountsCode to set.
     */
    public void setCashReversionFinancialChartOfAccountsCode(String cashReversionFinancialChartOfAccountsCode) {
        this.cashReversionFinancialChartOfAccountsCode = cashReversionFinancialChartOfAccountsCode;
    }

    public Boolean isReversionActiveIndicator() {
        return reversionActiveIndicator; 
    }

    public Boolean getReversionActiveIndicator() {
        return this.isReversionActiveIndicator();
    }

    public void setReversionActiveIndicator(Boolean reversionActiveIndicator) {
        this.reversionActiveIndicator = reversionActiveIndicator;
    }

	/**
	 * @return the accountReversionGlobalDetails
	 */
	public List<AccountReversionGlobalDetail> getAccountReversionGlobalDetails() {
		return accountReversionGlobalDetails;
	}

	/**
	 * @param accountReversionGlobalDetails the accountReversionGlobalDetails to set
	 */
	public void setAccountReversionGlobalDetails(
			List<AccountReversionGlobalDetail> accountReversionGlobalDetails) {
		this.accountReversionGlobalDetails = accountReversionGlobalDetails;
	}

	/**
	 * @return the accountReversionGlobalAccounts
	 */
	public List<AccountReversionGlobalAccount> getAccountReversionGlobalAccounts() {
		return accountReversionGlobalAccounts;
	}

	/**
	 * @param accountReversionGlobalAccounts the accountReversionGlobalAccounts to set
	 */
	public void setAccountReversionGlobalAccounts(
			List<AccountReversionGlobalAccount> accountReversionGlobalAccounts) {
		this.accountReversionGlobalAccounts = accountReversionGlobalAccounts;
	}

	/**
     * Gets the universityFiscalYear attribute.
     * 
     * @return Returns the universityFiscalYear.
     */
    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    /**
     * Sets the universityFiscalYear attribute value.
     * 
     * @param universityFiscalYear The universityFiscalYear to set.
     */
    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }

    /**
     * Gets the universityFiscal attribute.
     * 
     * @return Returns the universityFiscal.
     */
    public SystemOptions getUniversityFiscal() {
        return universityFiscal;
    }

    /**
     * Sets the universityFiscal attribute value.
     * 
     * @param universityFiscal The universityFiscal to set.
     */
    public void setUniversityFiscal(SystemOptions universityFiscal) {
        this.universityFiscal = universityFiscal;
    }

    /**
     * @see org.kuali.kfs.kns.bo.GlobalBusinessObject#generateDeactivationsToPersist() As global account reversions only update
     *      existing (or add new) records, deactivations will never be produced by creating one; thus, this method always returns an empty list.
     */
    public List<PersistableBusinessObject> generateDeactivationsToPersist() {
        return null;
    }

    /**
     * @see org.kuali.kfs.kns.bo.GlobalBusinessObject#generateGlobalChangesToPersist() This creates a list of changes to be made to the
     *      existing and/or new Account Reversion records impacted by this global reversion.
     */
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
        List<PersistableBusinessObject> persistingChanges = new ArrayList<PersistableBusinessObject>();

        AccountReversionService accountReversionService = SpringContext.getBean(AccountReversionService.class);
        Map<String, AccountReversionGlobalDetail> detailsMap = this.rearrangeAccountReversionDetailsAsMap();

        for (AccountReversionGlobalAccount acctRevAccount : this.getAccountReversionGlobalAccounts()) {
            // 1. find that account reversion
            AccountReversion currAcctRev = accountReversionService.getByPrimaryId(
                    this.getUniversityFiscalYear(), acctRevAccount.getChartOfAccountsCode(), acctRevAccount.getAccountNumber());

            if (ObjectUtils.isNull(currAcctRev)) {
                // If account reversion does not exist, then create it and its details.
                currAcctRev = new AccountReversion();
                currAcctRev.setUniversityFiscalYear(this.getUniversityFiscalYear());
                currAcctRev.setChartOfAccountsCode(acctRevAccount.getChartOfAccountsCode());
                currAcctRev.setAccountNumber(acctRevAccount.getAccountNumber());
                for (AccountReversionGlobalDetail globalDetail : this.getAccountReversionGlobalDetails()) {
                    AccountReversionDetail revDetail = new AccountReversionDetail();
                    revDetail.setAccountReversionCategoryCode(globalDetail.getAccountReversionCategoryCode());
                    revDetail.setUniversityFiscalYear(currAcctRev.getUniversityFiscalYear());
                    revDetail.setChartOfAccountsCode(currAcctRev.getChartOfAccountsCode());
                    revDetail.setAccountNumber(currAcctRev.getAccountNumber());
                    currAcctRev.addAccountReversionDetail(revDetail);
                }
            }
            
            // 2. update account reversion
            if (!StringUtils.isBlank(this.getBudgetReversionChartOfAccountsCode())) {
                currAcctRev.setBudgetReversionChartOfAccountsCode(this.getBudgetReversionChartOfAccountsCode());
            }
            if (!StringUtils.isBlank(this.getBudgetReversionAccountNumber())) {
                currAcctRev.setBudgetReversionAccountNumber(this.getBudgetReversionAccountNumber());
            }
            if (!StringUtils.isBlank(this.getCashReversionFinancialChartOfAccountsCode())) {
                currAcctRev.setCashReversionFinancialChartOfAccountsCode(this.getCashReversionFinancialChartOfAccountsCode());
            }
            if (!StringUtils.isBlank(this.getCashReversionAccountNumber())) {
                currAcctRev.setCashReversionAccountNumber(this.getCashReversionAccountNumber());
            }

            if (this.isCarryForwardByObjectCodeIndicator() != null) {
                currAcctRev.setCarryForwardByObjectCodeIndicator(this.isCarryForwardByObjectCodeIndicator().booleanValue());
            }
            if (this.isReversionActiveIndicator() != null) {
                currAcctRev.setActive(this.isReversionActiveIndicator().booleanValue());
            }

            // 3. now, go through each account reversion detail and update each of those
            for (AccountReversionDetail acctRevDetail : currAcctRev.getAccountReversionDetails()) {
                AccountReversionGlobalDetail changeDetail = detailsMap.get(acctRevDetail.getAccountReversionCategoryCode());
                if (changeDetail != null) {
                    if (!StringUtils.isBlank(changeDetail.getAccountReversionCode())) {
                        acctRevDetail.setAccountReversionCode(changeDetail.getAccountReversionCode());
                    }
                    if (!StringUtils.isBlank(changeDetail.getAccountReversionObjectCode())) {
                        acctRevDetail.setAccountReversionObjectCode(changeDetail.getAccountReversionObjectCode());
                    }
                    if (this.isReversionActiveIndicator() != null) {
                        acctRevDetail.setActive(this.isReversionActiveIndicator().booleanValue());
                    }
                }
            }

            currAcctRev.refreshNonUpdateableReferences();
            persistingChanges.add(currAcctRev);

        }
        return persistingChanges;
    }

    /**
     * This sticks all of the Account Reversion Change Details into a map, for quicker access in
     * generateGlobalChangesToPersist.
     * 
     * @return a map of all account reversion change details, keyed by AccountReversionCategory
     */
    private Map<String, AccountReversionGlobalDetail> rearrangeAccountReversionDetailsAsMap() {
        Map<String, AccountReversionGlobalDetail> acctRevMap = new HashMap<String, AccountReversionGlobalDetail>();
        for (AccountReversionGlobalDetail acctRevDetail : this.getAccountReversionGlobalDetails()) {
            if (!StringUtils.isBlank(acctRevDetail.getAccountReversionObjectCode()) || !StringUtils.isBlank(acctRevDetail.getAccountReversionCode())) {
                acctRevMap.put(acctRevDetail.getAccountReversionCategoryCode(), acctRevDetail);
            }
        }
        return acctRevMap;
    }

    /**
     * @see org.kuali.kfs.kns.bo.GlobalBusinessObject#getAllDetailObjects() This returns a list of all the detail objects held within
     *      this main global account reversion container.
     */
    public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
        List<GlobalBusinessObjectDetail> detailObjects = new ArrayList<GlobalBusinessObjectDetail>();
        detailObjects.addAll(this.getAccountReversionGlobalDetails());
        detailObjects.addAll(this.getAccountReversionGlobalAccounts());
        return detailObjects;
    }

    /**
     * @see org.kuali.kfs.kns.bo.GlobalBusinessObject#isPersistable() returns whether this global object reversion can be stored in the
     *      database, which is really a question of whether it and all of its details have all of their appropriate primary keys
     *      set.
     */
    public boolean isPersistable() {
        PersistenceStructureService persistenceStructureService = SpringContext.getBean(PersistenceStructureService.class);

        if (!persistenceStructureService.hasPrimaryKeyFieldValues(this)) {
            return false;
        }

        for (AccountReversionGlobalDetail acctRevDetail : this.getAccountReversionGlobalDetails()) {
            if (!persistenceStructureService.hasPrimaryKeyFieldValues(acctRevDetail)) {
                return false;
            }
        }

        for (AccountReversionGlobalAccount acctRevOrg : this.getAccountReversionGlobalAccounts()) {
            if (!persistenceStructureService.hasPrimaryKeyFieldValues(acctRevOrg)) {
                return false;
            }
        }

        // are we still here? really? Then, hey, let's persist!
        return true;
    }

    /**
     * @see org.kuali.kfs.krad.bo.PersistableBusinessObjectBase#buildListOfDeletionAwareLists()
     */
    @Override
    public List<Collection<PersistableBusinessObject>> buildListOfDeletionAwareLists() {
    	List<Collection<PersistableBusinessObject>> managedLists = super.buildListOfDeletionAwareLists();

        managedLists.add( new ArrayList<PersistableBusinessObject>(getAccountReversionGlobalDetails()));
        managedLists.add(new ArrayList<PersistableBusinessObject>(getAccountReversionGlobalAccounts()));

        return managedLists;
    }
}