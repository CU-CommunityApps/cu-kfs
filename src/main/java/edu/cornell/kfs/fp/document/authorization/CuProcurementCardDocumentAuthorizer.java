package edu.cornell.kfs.fp.document.authorization;

import org.kuali.kfs.sys.document.authorization.AccountingDocumentAuthorizerBase;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;

public class CuProcurementCardDocumentAuthorizer extends AccountingDocumentAuthorizerBase {
	
	@Override
    public boolean canEditDocumentOverview(Document document, Person user) {
        return isAuthorizedByTemplate(document, KRADConstants.KNS_NAMESPACE,
                KimConstants.PermissionTemplateNames.EDIT_DOCUMENT, user.getPrincipalId());
    }
}
