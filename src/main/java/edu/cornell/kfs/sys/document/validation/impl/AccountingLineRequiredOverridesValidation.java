package edu.cornell.kfs.sys.document.validation.impl;

import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.impl.AccountingLineDataDictionaryValidation;

public class AccountingLineRequiredOverridesValidation extends AccountingLineDataDictionaryValidation {

    /**
     * Overridden to only check whether the needed override flags have been set.
     * 
     * @see org.kuali.kfs.sys.document.validation.impl.AccountingLineDataDictionaryValidation#validate(
     * org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        return ruleHelperService.hasRequiredOverrides(
                getAccountingLineForValidation(), getAccountingLineForValidation().getOverrideCode());
    }

}
