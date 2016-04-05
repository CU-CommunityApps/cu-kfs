package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.krad.document.Document;

@SuppressWarnings("deprecation")
public class IWantDocumentPreRules extends PromptBeforeValidationBase {

    @Override
    public boolean doPrompts(Document document) {
        
        return true;
    }

}
