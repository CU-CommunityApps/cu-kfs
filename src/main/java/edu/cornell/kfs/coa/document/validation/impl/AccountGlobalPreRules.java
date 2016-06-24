package edu.cornell.kfs.coa.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.CuAccountGlobal;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class AccountGlobalPreRules extends MaintenancePreRulesBase {
	
	protected CuAccountGlobal accountGlobal;
	
    protected boolean doCustomPreRules(MaintenanceDocument maintenanceDocument) {
    	boolean preRulesOK = super.doCustomPreRules(maintenanceDocument);
    	setupConvenienceObjects(maintenanceDocument);

        checkForContinuationAccounts();        
        
        preRulesOK &= checkOffCampus();
        return preRulesOK;
    }
    
    protected void checkForContinuationAccounts() {
        LOG.debug("entering checkForContinuationAccounts()");

        if (StringUtils.isNotBlank(accountGlobal.getReportsToAccountNumber())) {
            Account account = checkForContinuationAccount("Fringe Benefit Account", accountGlobal.getReportsToChartOfAccountsCode(), accountGlobal.getReportsToAccountNumber(), "");
            if (ObjectUtils.isNotNull(account)) { // override old user inputs
            	accountGlobal.setReportsToAccountNumber(account.getAccountNumber());
            	accountGlobal.setReportsToChartOfAccountsCode(account.getChartOfAccountsCode());
            }
        }

        if (StringUtils.isNotBlank(accountGlobal.getEndowmentIncomeAccountNumber())) {
            Account account = checkForContinuationAccount("Endowment Account", accountGlobal.getEndowmentIncomeAcctFinCoaCd(), accountGlobal.getEndowmentIncomeAccountNumber(), "");
            if (ObjectUtils.isNotNull(account)) { // override old user inputs
            	accountGlobal.setEndowmentIncomeAccountNumber(account.getAccountNumber());
            	accountGlobal.setEndowmentIncomeAcctFinCoaCd(account.getChartOfAccountsCode());
            }
        }

        if (StringUtils.isNotBlank(accountGlobal.getIncomeStreamAccountNumber())) {
            Account account = checkForContinuationAccount("Income Stream Account", accountGlobal.getIncomeStreamFinancialCoaCode(), accountGlobal.getIncomeStreamAccountNumber(), "");
            if (ObjectUtils.isNotNull(account)) { // override old user inputs
            	accountGlobal.setIncomeStreamAccountNumber(account.getAccountNumber());
            	accountGlobal.setIncomeStreamFinancialCoaCode(account.getChartOfAccountsCode());
            }
        }

        if (StringUtils.isNotBlank(accountGlobal.getContractControlAccountNumber())) {
            Account account = checkForContinuationAccount("Contract Control Account", accountGlobal.getContractControlFinCoaCode(), accountGlobal.getContractControlAccountNumber(), "");
            if (ObjectUtils.isNotNull(account)) { // override old user inputs
            	accountGlobal.setContractControlAccountNumber(account.getAccountNumber());
            	accountGlobal.setContractControlFinCoaCode(account.getChartOfAccountsCode());
            }
        }

        for (IndirectCostRecoveryAccountChange icra : accountGlobal.getActiveIndirectCostRecoveryAccounts()){
            if (StringUtils.isNotBlank(icra.getIndirectCostRecoveryAccountNumber())) {
                Account account = checkForContinuationAccount("Indirect Cost Recovery Account", icra.getIndirectCostRecoveryAccountNumber(), icra.getIndirectCostRecoveryFinCoaCode(), "");
                if (ObjectUtils.isNotNull(account)) { // override old user inputs
                    icra.setIndirectCostRecoveryAccountNumber(account.getAccountNumber());
                    icra.setIndirectCostRecoveryFinCoaCode(account.getChartOfAccountsCode());
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    protected boolean checkOffCampus() {
      boolean continueRules = true;

      if (accountGlobal.getAccountOffCampusIndicator() !=null && accountGlobal.getAccountOffCampusIndicator()) {
        String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.QUESTION_ACCOUNT_OFF_CAMPUS_INDICATOR);

        boolean leaveAsIs = super.askOrAnalyzeYesNoQuestion(KFSConstants.AccountDocumentConstants.OFF_CAMPUS_INDICATOR_QUESTION_ID, questionText);

        if (!leaveAsIs) {
          // return to document if the user doesn't want to clear the indicator
          super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
          continueRules = false;
        }
      }

      return continueRules;
    }

    protected void setupConvenienceObjects(MaintenanceDocument document) {
    	 accountGlobal = (CuAccountGlobal) document.getNewMaintainableObject().getBusinessObject();
    	 accountGlobal.refreshNonUpdateableReferences();
    }

}
