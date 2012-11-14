package edu.cornell.kfs.module.purap.document.validation;

import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.document.IWantDocument;

public interface AddIWantItemRule {

    public boolean processAddIWantItemRules(IWantDocument document, IWantItem item, String errorPathPrefix);

}
