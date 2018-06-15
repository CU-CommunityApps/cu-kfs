package edu.cornell.kfs.coa.document.authorization;

import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.document.authorization.FinancialSystemMaintenanceDocumentAuthorizerBase;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.identity.Person;

public class CuObjectCodeActivationGlobalAuthorizer extends FinancialSystemMaintenanceDocumentAuthorizerBase {
    private static final long serialVersionUID = -4822839971298829022L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuObjectCodeActivationGlobalAuthorizer.class);
    
    public boolean canCopy(Document document, Person user) {
        boolean canCopy = super.canCopy(document, user);
        boolean isFinal = isDocumentFinalized(document);
        if (LOG.isDebugEnabled()) {
            LOG.debug("canCopy, document " + document.getDocumentNumber() + ", user: " + user.getPrincipalName() + ", can copy: " + canCopy + ", isFinal: " + isFinal);
        }
        return canCopy && isFinal;
    }
    
    private boolean isDocumentFinalized(Document document) {
        WorkflowDocument wd = document.getDocumentHeader().getWorkflowDocument();
        boolean isFinalized = wd.isFinal();
        return isFinalized;
    }

}
