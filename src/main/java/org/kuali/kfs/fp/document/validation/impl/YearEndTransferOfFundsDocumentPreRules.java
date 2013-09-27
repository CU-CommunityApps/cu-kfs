package org.kuali.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.service.AccountingDocumentPreRuleService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.rules.PromptBeforeValidationBase;

public class YearEndTransferOfFundsDocumentPreRules extends PromptBeforeValidationBase { 

    /**
     * Executes pre-rules for General Error Document
     * 
     * @param document submitted document
     * @return true if pre-rules execute successfully
     * @see org.kuali.rice.kns.rules.PromptBeforeValidationBase#doRules(org.kuali.rice.krad.document.MaintenanceDocument)
     */
    @Override
    public boolean doPrompts(Document document) {
        boolean preRulesOK = true;

        preRulesOK &= SpringContext.getBean(AccountingDocumentPreRuleService.class).accessAccountOverrideQuestion((AccountingDocumentBase) document, this, this.event);
        
        return preRulesOK;
    }
}