package edu.cornell.kfs.module.purap.document.validation;

import org.kuali.rice.krad.rules.rule.BusinessRule;

import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.document.IWantDocument;

public interface AddIWantItemRule extends BusinessRule {

    boolean processAddIWantItemRules(IWantDocument document, IWantItem item, String errorPathPrefix);

}
