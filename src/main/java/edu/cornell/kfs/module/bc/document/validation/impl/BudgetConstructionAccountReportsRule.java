package edu.cornell.kfs.module.bc.document.validation.impl;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.bc.CUBCKeyConstants;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionAccountReports;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ACCOUNT_NUMBER;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.bc.CUBCPropertyConstants;

/**
 */
public class BudgetConstructionAccountReportsRule extends MaintenanceDocumentRuleBase {

    protected static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
            .getLogger(BudgetConstructionAccountReportsRule.class);

    protected BudgetConstructionAccountReports oldBudgetConstructionAccountReports;
    protected BudgetConstructionAccountReports newBudgetConstructionAccountReports;

    /**
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean result = super.processCustomRouteDocumentBusinessRules(document);
        
        result &= validateAccountBudgetable(newBudgetConstructionAccountReports);

        return result;
    }

    /**
     * Validates that the account budget record level is not N.
     * 
     * @param accountReports
     * @return true if valid, false otherwise
     */
    protected boolean validateAccountBudgetable(BudgetConstructionAccountReports accountReports) {
        boolean valid = true;

        accountReports.refreshReferenceObject(CUBCPropertyConstants.BudgetConstructionAccountReportsProperties.ACCOUNT);
        Account account = accountReports.getAccount();

        if (ObjectUtils.isNotNull(account) && ACCOUNT_NUMBER.BUDGET_LEVEL_NO_BUDGET.equalsIgnoreCase(account
                .getBudgetRecordingLevelCode())) {

            valid = false;
            putFieldError("accountNumber", CUBCKeyConstants.ERROR_BUDGET_ACCOUNT_REPORTS, new String[] { account.getAccountNumber() });
        }

        return valid;
    }

    /**
     * 
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#setupConvenienceObjects()
     */
    public void setupConvenienceObjects() {

        oldBudgetConstructionAccountReports = (BudgetConstructionAccountReports) super.getOldBo();
        newBudgetConstructionAccountReports = (BudgetConstructionAccountReports) super.getNewBo();
    }

}
