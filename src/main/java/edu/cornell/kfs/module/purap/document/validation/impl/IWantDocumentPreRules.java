package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.rules.PromptBeforeValidationBase;

public class IWantDocumentPreRules extends PromptBeforeValidationBase {

    @Override
    public boolean doPrompts(Document document) {
        
        return true;
    }

}
