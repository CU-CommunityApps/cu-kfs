package edu.cornell.kfs.ksr.document.validation.impl;

import org.kuali.kfs.krad.rules.rule.BusinessRule;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEventBase;

import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;

public class AddQualificationLineEvent extends KualiDocumentEventBase {

    private SecurityRequestRoleQualification requestRoleQualification;

    public AddQualificationLineEvent(String errorPathPrefix, SecurityRequestDocument document,
            SecurityRequestRoleQualification requestRoleQualification) {
        super("creating add qualification event for document " + getDocumentId(document), errorPathPrefix, document);

        this.requestRoleQualification = requestRoleQualification;
    }

    /**
     * Security request role qualification object associated with the event
     * object
     * 
     * @return SecurityRequestRoleQualification instance
     */
    public SecurityRequestRoleQualification getRequestRoleQualification() {
        return requestRoleQualification;
    }

    /**
     * @see org.kuali.rice.kns.rule.event.KualiDocumentEvent#getRuleInterfaceClass()
     */
    public Class<? extends BusinessRule> getRuleInterfaceClass() {
        return AddQualificationRule.class;
    }

    /**
     * @see org.kuali.rice.kns.rule.event.KualiDocumentEvent#invokeRuleMethod(org.kuali.rice.kns.rule.BusinessRule)
     */
    public boolean invokeRuleMethod(BusinessRule rule) {
        return ((AddQualificationRule) rule).processAddRoleQualification((SecurityRequestDocument) document,
                requestRoleQualification);
    }

}
