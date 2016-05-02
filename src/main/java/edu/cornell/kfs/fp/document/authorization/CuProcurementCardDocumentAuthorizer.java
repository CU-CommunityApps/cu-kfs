package edu.cornell.kfs.fp.document.authorization;

import org.kuali.kfs.fp.document.authorization.ProcurementCardDocumentAuthorizer;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.KRADConstants;

public class CuProcurementCardDocumentAuthorizer extends ProcurementCardDocumentAuthorizer {
	
	@Override
    public boolean canEditDocumentOverview(Document document, Person user) {
        return isAuthorizedByTemplate(document, KRADConstants.KNS_NAMESPACE,
                KimConstants.PermissionTemplateNames.EDIT_DOCUMENT, user.getPrincipalId());
    }
}
