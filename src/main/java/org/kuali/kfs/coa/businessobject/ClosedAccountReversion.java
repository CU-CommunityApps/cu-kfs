/*
 * Copyright 2009 The Kuali Foundation
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
package org.kuali.kfs.coa.businessobject;

/**
 * Wraps an OrganizationReversion detail to make sure that organization reversion returns only 
 * Closed Account details, which in turn will force R2 logic to run for all the details
 */
public class ClosedAccountReversion implements CarryForwardReversionProcessInfo {
    private CarryForwardReversionProcessInfo cfReversionProcessInfo;
    
    /**
     * Constructs a ClosedAccountOrganizationReversion
     * @param organizationReversion the organization reversion to wrap
     */
    public ClosedAccountReversion(CarryForwardReversionProcessInfo cfReversionProcessInfo) {
        this.cfReversionProcessInfo = cfReversionProcessInfo;
    }

    /**
     * Returns the budget reversion account number from the wrapped org reversion
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getBudgetReversionAccountNumber()
     */
    public String getBudgetReversionAccountNumber() {
        return cfReversionProcessInfo.getBudgetReversionAccountNumber();
    }

    /**
     * Returns the cash reversion account number from the wrapped org reversion
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getCashReversionAccountNumber()
     */
    public String getCashReversionAccountNumber() {
        return cfReversionProcessInfo.getCashReversionAccountNumber();
    }

    /**
     * Returns the chart of accounts code from the wrapped org reversion
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getChartOfAccountsCode()
     */
    public String getChartOfAccountsCode() {
        return cfReversionProcessInfo.getChartOfAccountsCode();
    }

//    /**
//     * Returns the organization code from the wrapped org reversion
//     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getOrganizationCode()
//     */
//    public String getOrganizationCode() {
//        return organizationReversion.getSourceAttribute();
//    }

    /**
     * Returns a closed account org reversion detail for the given category
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getOrganizationReversionDetail(java.lang.String)
     */
    public ReversionCategoryInfo getReversionDetail(String categoryCode) {
        ReversionCategoryInfo reversionDetail = cfReversionProcessInfo.getReversionDetail(categoryCode);
        if (reversionDetail != null) {
            return new ClosedAccountOrganizationReversionDetail(reversionDetail);
        } else {
            return null;
        }
    }

    /**
     * Returns the fiscal year from the wrapped org reversion
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getUniversityFiscalYear()
     */
    public Integer getUniversityFiscalYear() {
        return cfReversionProcessInfo.getUniversityFiscalYear();
    }

    /**
     * Returns the carry forward by object code indicator from the wrapped org reversion
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#isCarryForwardByObjectCodeIndicator()
     */
    public boolean isCarryForwardByObjectCodeIndicator() {
        return cfReversionProcessInfo.isCarryForwardByObjectCodeIndicator();
    }

    /**
     * returns the budget reversion chart of accounts code from the wrapped organization reversion
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getBudgetReversionChartOfAccountsCode()
     */
    public String getBudgetReversionChartOfAccountsCode() {
        return cfReversionProcessInfo.getBudgetReversionChartOfAccountsCode();
    }

    /**
     * returns the cash reversion chart cash object code from the wrapped organization reversion
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getCashReversionChartCashObjectCode()
     */
    public String getCashReversionChartCashObjectCode() {
        return cfReversionProcessInfo.getCashReversionChartCashObjectCode();
    }

    /**
     * returns the cash reversion chart of accounts code from the wrapped organization reversion
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getCashReversionFinancialChartOfAccountsCode()
     */
    public String getCashReversionFinancialChartOfAccountsCode() {
        return cfReversionProcessInfo.getCashReversionFinancialChartOfAccountsCode();
    }

    /**
     * returns the organization chart's cash object code from the wrapped organization reversion
     * @see org.kuali.kfs.coa.businessobject.CarryForwardReversionProcessOrganizationInfo#getOrganizationChartCashObjectCode()
     */
    public String getChartCashObjectCode() {
        return cfReversionProcessInfo.getChartCashObjectCode();
    }

    
    public String getSourceAttribute() {
        return cfReversionProcessInfo.getSourceAttribute();
    }

   
}
