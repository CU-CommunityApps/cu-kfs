package edu.cornell.kfs.module.purap.document.validation.event;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.rule.BusinessRule;
import org.kuali.rice.kns.rule.event.KualiDocumentEvent;
import org.kuali.rice.kns.rule.event.KualiDocumentEventBase;

import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.validation.AddIWantItemRule;

public class AddIWantItemEvent extends KualiDocumentEventBase implements KualiDocumentEvent {

    private IWantItem item;

    public AddIWantItemEvent(String errorPathPrefix, Document document, IWantItem item) {

        super("adding item to document " + getDocumentId(document), errorPathPrefix, document);
        this.item = item;
    }

    public Class getRuleInterfaceClass() {
        return AddIWantItemRule.class;
    }

    public boolean invokeRuleMethod(BusinessRule rule) {
        return ((AddIWantItemRule) rule).processAddIWantItemRules((IWantDocument) document, item,
                KFSConstants.EMPTY_STRING);
    }

    public IWantItem getItem() {
        return item;
    }

    public void setItem(IWantItem item) {
        this.item = item;
    }

}
