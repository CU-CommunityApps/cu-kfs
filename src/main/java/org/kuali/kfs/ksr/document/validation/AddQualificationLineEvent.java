package org.kuali.kfs.ksr.document.validation;

import org.kuali.kfs.ksr.bo.SecurityRequestRoleQualification;
import org.kuali.kfs.ksr.document.SecurityRequestDocument;
import org.kuali.rice.krad.rules.rule.BusinessRule;
import org.kuali.rice.krad.rules.rule.event.DocumentEventBase;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x compatibility.
 * ====
 * 
 * Event object for processing rules on a new role qualification detail line
 * 
 * @author rSmart Development Team
 */
public class AddQualificationLineEvent extends DocumentEventBase {

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
