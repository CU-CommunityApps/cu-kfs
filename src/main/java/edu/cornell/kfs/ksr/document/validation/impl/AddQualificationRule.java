package edu.cornell.kfs.ksr.document.validation.impl;

import org.kuali.kfs.krad.rules.rule.BusinessRule;

import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;

public interface AddQualificationRule extends BusinessRule {

    /**
     * Invoked to process business rules on the given request role qualification
     * line and determine whether processing of the new line should continue
     * 
     * @param document
     *            - SecurityRequestDocument instance for which the new role
     *            qualification will be added
     * @param roleQualification
     *            - role qualification to be validated
     * @return boolean true if business rules passed and the line should be
     *         added, false if there were failures
     */
    public boolean processAddRoleQualification(SecurityRequestDocument document,
            SecurityRequestRoleQualification roleQualification);

}
