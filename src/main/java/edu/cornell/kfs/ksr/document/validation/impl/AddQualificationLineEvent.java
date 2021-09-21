package edu.cornell.kfs.ksr.document.validation.impl;

import org.kuali.kfs.krad.rules.rule.BusinessRule;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEventBase;

import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;

public class AddQualificationLineEvent extends KualiDocumentEventBase {

    private SecurityRequestRoleQualification requestRoleQualification;

    public AddQualificationLineEvent(String errorPathPrefix, SecurityRequestDocument document, SecurityRequestRoleQualification requestRoleQualification) {
        super("creating add qualification event for document " + getDocumentId(document), errorPathPrefix, document);

        this.requestRoleQualification = requestRoleQualification;
    }

    public SecurityRequestRoleQualification getRequestRoleQualification() {
        return requestRoleQualification;
    }

    public Class<? extends BusinessRule> getRuleInterfaceClass() {
        return AddQualificationRule.class;
    }

    public boolean invokeRuleMethod(BusinessRule rule) {
        return ((AddQualificationRule) rule).processAddRoleQualification((SecurityRequestDocument) document, requestRoleQualification);
    }

}
