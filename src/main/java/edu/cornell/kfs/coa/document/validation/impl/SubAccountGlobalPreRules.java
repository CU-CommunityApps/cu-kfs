package edu.cornell.kfs.coa.document.validation.impl;

import edu.cornell.kfs.sys.CUKFSConstants;
import org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kns.document.MaintenanceDocument;

import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class SubAccountGlobalPreRules extends MaintenancePreRulesBase{
	
	/**
	 * @see org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase#doCustomPreRules(org.kuali.kfs.kns.document.MaintenanceDocument)
	 */
	protected boolean doCustomPreRules(MaintenanceDocument maintenanceDocument) {
	    boolean preRulesOK = super.doCustomPreRules(maintenanceDocument);
	    preRulesOK &= checkOffCampus(maintenanceDocument);
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
        boolean saccOffCampus = subAccountGlobal.getA21SubAccount().isOffCampusCode();

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
}
