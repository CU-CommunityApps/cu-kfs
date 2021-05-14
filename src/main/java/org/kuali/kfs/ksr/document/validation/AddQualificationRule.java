package org.kuali.kfs.ksr.document.validation;

import org.kuali.kfs.ksr.bo.SecurityRequestRoleQualification;
import org.kuali.kfs.ksr.document.SecurityRequestDocument;
import org.kuali.rice.krad.rules.rule.BusinessRule;

/**
 * ====
 * CU Customization:
 * Remediated this class for Rice 2.x compatibility.
 * ====
 * 
 * Interface that should be implemented for checking business rules on a new
 * role qualification line
 * 
 * @author rSmart Development Team
 */
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
