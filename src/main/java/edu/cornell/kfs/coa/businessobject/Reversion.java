/*
 * Copyright 2012 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 * This class...
 */
public abstract class Reversion extends PersistableBusinessObjectBase implements CarryForwardReversionProcessInfo {

    private Integer universityFiscalYear;
    private String chartOfAccountsCode;
    private String budgetReversionChartOfAccountsCode;
    private String budgetReversionAccountNumber;
    private boolean carryForwardByObjectCodeIndicator;
    private String cashReversionFinancialChartOfAccountsCode;
    private String cashReversionAccountNumber;
    private Chart chartOfAccounts;
    private Account cashReversionAccount;
    private Account budgetReversionAccount;
    private Chart budgetReversionChartOfAccounts;
    private Chart cashReversionFinancialChartOfAccounts;
    private SystemOptions universityFiscal;
    private boolean active;

    
    /**
     * Gets the universityFiscalYear attribute. 
     * @return Returns the universityFiscalYear.
     */
    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    /**
     * Sets the universityFiscalYear attribute value.
     * @param universityFiscalYear The universityFiscalYear to set.
     */
    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }

    /**
     * Gets the chartOfAccountsCode attribute. 
     * @return Returns the chartOfAccountsCode.
     */
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    /**
     * Sets the chartOfAccountsCode attribute value.
     * @param chartOfAccountsCode The chartOfAccountsCode to set.
     */
    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    /**
     * Gets the budgetReversionChartOfAccountsCode attribute. 
     * @return Returns the budgetReversionChartOfAccountsCode.
     */
    public String getBudgetReversionChartOfAccountsCode() {
        return budgetReversionChartOfAccountsCode;
    }

    /**
     * Sets the budgetReversionChartOfAccountsCode attribute value.
     * @param budgetReversionChartOfAccountsCode The budgetReversionChartOfAccountsCode to set.
     */
    public void setBudgetReversionChartOfAccountsCode(String budgetReversionChartOfAccountsCode) {
        this.budgetReversionChartOfAccountsCode = budgetReversionChartOfAccountsCode;
    }

    /**
     * Gets the budgetReversionAccountNumber attribute. 
     * @return Returns the budgetReversionAccountNumber.
     */
    public String getBudgetReversionAccountNumber() {
        return budgetReversionAccountNumber;
    }

    /**
     * Sets the budgetReversionAccountNumber attribute value.
     * @param budgetReversionAccountNumber The budgetReversionAccountNumber to set.
     */
    public void setBudgetReversionAccountNumber(String budgetReversionAccountNumber) {
        this.budgetReversionAccountNumber = budgetReversionAccountNumber;
    }

    /**
     * Gets the carryForwardByObjectCodeIndicator attribute. 
     * @return Returns the carryForwardByObjectCodeIndicator.
     */
    public boolean isCarryForwardByObjectCodeIndicator() {
        return carryForwardByObjectCodeIndicator;
    }

    /**
     * Sets the carryForwardByObjectCodeIndicator attribute value.
     * @param carryForwardByObjectCodeIndicator The carryForwardByObjectCodeIndicator to set.
     */
    public void setCarryForwardByObjectCodeIndicator(boolean carryForwardByObjectCodeIndicator) {
        this.carryForwardByObjectCodeIndicator = carryForwardByObjectCodeIndicator;
    }

    /**
     * Gets the cashReversionFinancialChartOfAccountsCode attribute. 
     * @return Returns the cashReversionFinancialChartOfAccountsCode.
     */
    public String getCashReversionFinancialChartOfAccountsCode() {
        return cashReversionFinancialChartOfAccountsCode;
    }

    /**
     * Sets the cashReversionFinancialChartOfAccountsCode attribute value.
     * @param cashReversionFinancialChartOfAccountsCode The cashReversionFinancialChartOfAccountsCode to set.
     */
    public void setCashReversionFinancialChartOfAccountsCode(String cashReversionFinancialChartOfAccountsCode) {
        this.cashReversionFinancialChartOfAccountsCode = cashReversionFinancialChartOfAccountsCode;
    }

    /**
     * Gets the cashReversionAccountNumber attribute. 
     * @return Returns the cashReversionAccountNumber.
     */
    public String getCashReversionAccountNumber() {
        return cashReversionAccountNumber;
    }

    /**
     * Sets the cashReversionAccountNumber attribute value.
     * @param cashReversionAccountNumber The cashReversionAccountNumber to set.
     */
    public void setCashReversionAccountNumber(String cashReversionAccountNumber) {
        this.cashReversionAccountNumber = cashReversionAccountNumber;
    }

    /**
     * Gets the chartOfAccounts attribute. 
     * @return Returns the chartOfAccounts.
     */
    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    /**
     * Sets the chartOfAccounts attribute value.
     * @param chartOfAccounts The chartOfAccounts to set.
     */
    public void setChartOfAccounts(Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    /**
     * Gets the cashReversionAccount attribute. 
     * @return Returns the cashReversionAccount.
     */
    public Account getCashReversionAccount() {
        return cashReversionAccount;
    }

    /**
     * Sets the cashReversionAccount attribute value.
     * @param cashReversionAccount The cashReversionAccount to set.
     */
    public void setCashReversionAccount(Account cashReversionAccount) {
        this.cashReversionAccount = cashReversionAccount;
    }

    /**
     * Gets the budgetReversionAccount attribute. 
     * @return Returns the budgetReversionAccount.
     */
    public Account getBudgetReversionAccount() {
        return budgetReversionAccount;
    }

    /**
     * Sets the budgetReversionAccount attribute value.
     * @param budgetReversionAccount The budgetReversionAccount to set.
     */
    public void setBudgetReversionAccount(Account budgetReversionAccount) {
        this.budgetReversionAccount = budgetReversionAccount;
    }

    /**
     * Gets the budgetReversionChartOfAccounts attribute. 
     * @return Returns the budgetReversionChartOfAccounts.
     */
    public Chart getBudgetReversionChartOfAccounts() {
        return budgetReversionChartOfAccounts;
    }

    /**
     * Sets the budgetReversionChartOfAccounts attribute value.
     * @param budgetReversionChartOfAccounts The budgetReversionChartOfAccounts to set.
     */
    public void setBudgetReversionChartOfAccounts(Chart budgetReversionChartOfAccounts) {
        this.budgetReversionChartOfAccounts = budgetReversionChartOfAccounts;
    }

    /**
     * Gets the cashReversionFinancialChartOfAccounts attribute. 
     * @return Returns the cashReversionFinancialChartOfAccounts.
     */
    public Chart getCashReversionFinancialChartOfAccounts() {
        return cashReversionFinancialChartOfAccounts;
    }

    /**
     * Sets the cashReversionFinancialChartOfAccounts attribute value.
     * @param cashReversionFinancialChartOfAccounts The cashReversionFinancialChartOfAccounts to set.
     */
    public void setCashReversionFinancialChartOfAccounts(Chart cashReversionFinancialChartOfAccounts) {
        this.cashReversionFinancialChartOfAccounts = cashReversionFinancialChartOfAccounts;
    }

    /**
     * Gets the universityFiscal attribute. 
     * @return Returns the universityFiscal.
     */
    public SystemOptions getUniversityFiscal() {
        return universityFiscal;
    }

    /**
     * Sets the universityFiscal attribute value.
     * @param universityFiscal The universityFiscal to set.
     */
    public void setUniversityFiscal(SystemOptions universityFiscal) {
        this.universityFiscal = universityFiscal;
    }

    /**
     * Gets the active attribute. 
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active attribute value.
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getCashReversionChartCashObjectCode()
     */
    public String getCashReversionChartCashObjectCode() {
        if (ObjectUtils.isNull(getCashReversionFinancialChartOfAccounts())) {
            this.refreshReferenceObject("cashReversionFinancialChartOfAccounts");
        }
        if (!ObjectUtils.isNull(getCashReversionFinancialChartOfAccounts())) {
            return getCashReversionFinancialChartOfAccounts().getFinancialCashObjectCode();
        } else {
            return null;
        }
    }

    /**
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getOrganizationChartCashObjectCode()
     */
    public String getChartCashObjectCode() {
        if (ObjectUtils.isNull(getChartOfAccounts())) {
            this.refreshReferenceObject("chartOfAccounts");
        }
        if (!ObjectUtils.isNull(getChartOfAccounts())) {
            return getChartOfAccounts().getFinancialCashObjectCode();
        } else {
            return null;
        }
    }
    /**
     * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        
        LinkedHashMap m = new LinkedHashMap();
        if (getUniversityFiscalYear() != null) {
            m.put("universityFiscalYear", getUniversityFiscalYear().toString());
        }
        m.put("chartOfAccountsCode", getChartOfAccountsCode());
        return m;
    }

    /**
     * Gets the organizationCode attribute.
     * 
     * @return Returns the organizationCode
     */
    public abstract String getSourceAttribute();
    
    
}
