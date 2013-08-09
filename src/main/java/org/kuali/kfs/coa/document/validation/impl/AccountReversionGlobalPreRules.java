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
package org.kuali.kfs.coa.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountReversionGlobal;
import org.kuali.kfs.coa.businessobject.AccountReversionGlobalAccount;
import org.kuali.kfs.coa.businessobject.AccountReversionGlobalDetail;
import org.kuali.kfs.coa.businessobject.OrganizationReversionGlobal;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 * 
 * PreRules checks for the {@link OrganizationReversionGlobal} that needs to occur while still in the Struts processing. This includes defaults
 */
public class AccountReversionGlobalPreRules extends MaintenancePreRulesBase {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionGlobalPreRules.class);

    /**
     * This is the hook method for the {@link MaintenancePreRulesBase} to call. It calls
     * <ul>
     * <li>{@link AccountReversionGlobalPreRules#checkForContinuationAccounts(OrganizationReversionGlobal)}</li>
     * <li>{@link AccountReversionGlobalPreRules#copyKeyAttributesToCollections(OrganizationReversionGlobal)}</li>
     * </ul>
     * @see org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase#doCustomPreRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    public boolean doCustomPreRules(MaintenanceDocument maintenanceDocument) {
    	AccountReversionGlobal globalAcctReversion = (AccountReversionGlobal) maintenanceDocument.getNewMaintainableObject().getBusinessObject();
        checkForContinuationAccounts(globalAcctReversion);
        copyKeyAttributesToCollections(globalAcctReversion);
        return true;
    }

    /**
     * This method checks to see if the budget reversion or cash reversion accounts have continuation accounts.
     * 
     * @param globalOrgReversion Global Organization Reversion to check.
     */
    public void checkForContinuationAccounts(AccountReversionGlobal globalAcctReversion) {
        if (!StringUtils.isBlank(globalAcctReversion.getBudgetReversionChartOfAccountsCode()) && !StringUtils.isBlank(globalAcctReversion.getBudgetReversionAccountNumber())) {
            Account account = checkForContinuationAccount("Budget Reversion Account Number", globalAcctReversion.getBudgetReversionChartOfAccountsCode(), globalAcctReversion.getBudgetReversionAccountNumber(), "");
            if (ObjectUtils.isNotNull(account)) {
            	globalAcctReversion.setBudgetReversionChartOfAccountsCode(account.getChartOfAccountsCode());
            	globalAcctReversion.setBudgetReversionAccountNumber(account.getAccountNumber());
            }
        }
        if (!StringUtils.isBlank(globalAcctReversion.getCashReversionFinancialChartOfAccountsCode()) && !StringUtils.isBlank(globalAcctReversion.getBudgetReversionAccountNumber())) {
            Account account = checkForContinuationAccount("Cash Reversion Account Number", globalAcctReversion.getCashReversionFinancialChartOfAccountsCode(), globalAcctReversion.getCashReversionAccountNumber(), "");
            if (ObjectUtils.isNotNull(account)) {
            	globalAcctReversion.setCashReversionFinancialChartOfAccountsCode(account.getChartOfAccountsCode());
            	globalAcctReversion.setCashReversionAccountNumber(account.getAccountNumber());
            }
        }
    }

    /**
     * This method updates all children of a Global Organization Reversion so that they all are associated with the Global
     * Organization Reversion document, by upudating their primary keys.
     * 
     * @param globalOrgRev the global organization reversion document to update.
     */
    public void copyKeyAttributesToCollections(AccountReversionGlobal globalAcctRev) {
        for (AccountReversionGlobalDetail acctRevDetail : globalAcctRev.getAccountReversionGlobalDetails()) {
        	acctRevDetail.setDocumentNumber(globalAcctRev.getDocumentNumber());
        }

        for (AccountReversionGlobalAccount acctRevAcct : globalAcctRev.getAccountReversionGlobalAccounts()) {
        	acctRevAcct.setDocumentNumber(globalAcctRev.getDocumentNumber());
        }
    }

}
