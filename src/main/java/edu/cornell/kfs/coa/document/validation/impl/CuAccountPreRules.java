package edu.cornell.kfs.coa.document.validation.impl;

import edu.cornell.kfs.sys.CUKFSConstants;
import org.kuali.kfs.coa.document.validation.impl.AccountPreRules;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kns.document.MaintenanceDocument;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuAccountPreRules extends AccountPreRules {

  @SuppressWarnings("deprecation")
  protected boolean doCustomPreRules(final MaintenanceDocument document) {
    boolean preRulesOK = super.doCustomPreRules(document);
    preRulesOK &= checkOffCampus(document);
    return preRulesOK;
  }

    @SuppressWarnings("deprecation")
    protected boolean checkOffCampus(final MaintenanceDocument saccDoc) {
      boolean continueRules = true;
      final boolean accOffCampus = newAccount.isAccountOffCampusIndicator();

      if (accOffCampus) {
        final String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.QUESTION_ACCOUNT_OFF_CAMPUS_INDICATOR);

        final boolean leaveAsIs = super.askOrAnalyzeYesNoQuestion(CUKFSConstants.AccountDocumentConstants.OFF_CAMPUS_INDICATOR_QUESTION_ID, questionText);

        if (!leaveAsIs) {
          // return to document if the user doesn't want to clear the indicator
          super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
          continueRules = false;
        }
      }

      return continueRules;
    }
}
