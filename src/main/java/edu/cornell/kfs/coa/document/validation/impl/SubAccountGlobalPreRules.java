package edu.cornell.kfs.coa.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalNewAccountDetail;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class SubAccountGlobalPreRules extends MaintenancePreRulesBase{
	
	/**
	 * @see org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase#doCustomPreRules(org.kuali.kfs.kns.document.MaintenanceDocument)
	 */
	protected boolean doCustomPreRules(MaintenanceDocument maintenanceDocument) {
	    boolean preRulesOK = super.doCustomPreRules(maintenanceDocument);
	    preRulesOK &= checkOffCampus(maintenanceDocument);
	    if (preRulesOK) {
	        preRulesOK &= checkContinuationAccountsForNewSubAccounts(maintenanceDocument);
	    }
	    return preRulesOK;
	  }

    /**
     * Checks if off campus is set and prompts user question.
     * 
     * @param maintenanceDocument
     * @return true if continue, false otherwise
     */
    protected boolean checkOffCampus(MaintenanceDocument maintenanceDocument) {
        boolean continueRules = true;
        
        SubAccountGlobal subAccountGlobal = (SubAccountGlobal) maintenanceDocument.getNewMaintainableObject().getBusinessObject();
        boolean saccOffCampus = anyOffCampusIndicatorsAreSet(subAccountGlobal);

        if (saccOffCampus) {
          String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.QUESTION_A21SUBACCOUNT_OFF_CAMPUS_INDICATOR);

          boolean leaveAsIs = super.askOrAnalyzeYesNoQuestion(CUKFSConstants.A21SubAccountDocumentConstants.OFF_CAMPUS_INDICATOR_QUESTION_ID, questionText);

          if (!leaveAsIs) {
            // return to document if the user doesn't want to clear the indicator
            super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
            continueRules = false;
          }
        }

        return continueRules;
      }

    protected boolean anyOffCampusIndicatorsAreSet(SubAccountGlobal subAccountGlobal) {
        if (subAccountGlobal.getA21SubAccount().isOffCampusCode() || subAccountGlobal.isNewSubAccountOffCampusCode()) {
            return true;
        } else {
            return subAccountGlobal.getSubAccountGlobalNewAccountDetails().stream()
                    .anyMatch(SubAccountGlobalNewAccountDetail::isOffCampusCode);
        }
    }

    protected boolean checkContinuationAccountsForNewSubAccounts(MaintenanceDocument maintenanceDocument) {
        SubAccountGlobal subAccountGlobal = (SubAccountGlobal) maintenanceDocument.getNewMaintainableObject().getBusinessObject();
        subAccountGlobal.getSubAccountGlobalNewAccountDetails()
                .forEach(this::updateDetailToUseContinuationAccountIfNecessary);
        return true;
    }

    protected void updateDetailToUseContinuationAccountIfNecessary(SubAccountGlobalNewAccountDetail newAccountDetail) {
        if (StringUtils.isNotBlank(newAccountDetail.getChartOfAccountsCode())
                && StringUtils.isNotBlank(newAccountDetail.getAccountNumber())) {
            Account continuationAccount = checkForContinuationAccount(
                    CUKFSConstants.SUB_ACCOUNT_GLOBAL_NEW_SUB_ACCOUNT_LABEL, newAccountDetail.getChartOfAccountsCode(),
                    newAccountDetail.getAccountNumber(), KFSConstants.EMPTY_STRING);
            if (ObjectUtils.isNotNull(continuationAccount)) {
                newAccountDetail.setChartOfAccountsCode(continuationAccount.getChartOfAccountsCode());
                newAccountDetail.setAccountNumber(continuationAccount.getAccountNumber());
            }
        }
    }

}
