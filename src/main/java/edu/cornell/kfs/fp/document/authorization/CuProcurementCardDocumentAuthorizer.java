package edu.cornell.kfs.fp.document.authorization;

import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.authorization.AccountingDocumentAuthorizerBase;

public class CuProcurementCardDocumentAuthorizer extends AccountingDocumentAuthorizerBase {
	
	@Override
    public boolean canEditDocumentOverview(Document document, Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.EDIT_DOCUMENT, user.getPrincipalId());
    }
}
