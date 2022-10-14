package edu.cornell.kfs.module.ld.document.validation.impl;

import org.kuali.kfs.fp.service.AccountingDocumentPreRuleService;
import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.module.ld.document.SalaryExpenseTransferDocument;
import org.kuali.kfs.module.ld.document.validation.impl.SalaryExpenseTransferDocumentPreRules;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.krad.document.Document;

/**
 * Checks warnings and prompt conditions for ST documents.
 */
public class CUSalaryExpenseTransferDocumentPreRules extends SalaryExpenseTransferDocumentPreRules {

    /**
     * Will call methods to examine a ST Document. Includes Error Certification Statement for approval by a fiscal officer if
     * appropriate. Checks for expired accounts, per Cornell customization specs.
     *
     * @see org.kuali.kfs.module.ld.document.validation.impl.SalaryExpenseTransferDocumentPreRules#doPrompts(org.kuali.kfs.kns.document.Document)
     */
    @Override
    public boolean doPrompts(Document document) {
        boolean preRulesOK = super.doPrompts(document);

        preRulesOK &= SpringContext.getBean(AccountingDocumentPreRuleService.class).expiredAccountOverrideQuestion((AccountingDocumentBase) document, (PromptBeforeValidationBase)this);

        return preRulesOK;
    }

}
