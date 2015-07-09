package edu.cornell.kfs.coa.document.validation.impl;

import java.text.MessageFormat;
import java.util.List;

import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.document.validation.impl.AccountPreRules;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kns.document.MaintenanceDocument;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuAccountPreRules extends AccountPreRules {

  @SuppressWarnings("deprecation")
  protected boolean doCustomPreRules(MaintenanceDocument document) {
    boolean preRulesOK = super.doCustomPreRules(document);
    preRulesOK &= checkOffCampus(document);
    return preRulesOK;
  }

    @SuppressWarnings("deprecation")
    protected boolean checkOffCampus(MaintenanceDocument saccDoc) {
      boolean continueRules = true;
      boolean accOffCampus = newAccount.isAccountOffCampusIndicator();

      if (accOffCampus) {
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
}
