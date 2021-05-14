package org.kuali.kfs.ksr.document.authorization;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.maintenance.MaintenanceDocumentAuthorizerBase;

/**
 * ====
 * CU Customization:
 * Added new maintenance authorizer that can allow the document "close" button.
 * ====
 */
public class KSRMaintenanceDocumentAuthorizer extends MaintenanceDocumentAuthorizerBase {

    private static final long serialVersionUID = -5745063112999472666L;

    /**
     * Overridden to always return true, due to the inherited method
     * always returning false.
     * 
     * @see org.kuali.rice.krad.document.DocumentAuthorizerBase#canClose(
     * org.kuali.rice.krad.document.Document, org.kuali.rice.kim.api.identity.Person)
     */
    @Override
    public boolean canClose(Document document, Person user) {
        return true;
    }

}
