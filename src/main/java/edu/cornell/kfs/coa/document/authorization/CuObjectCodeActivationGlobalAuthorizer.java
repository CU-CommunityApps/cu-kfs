package edu.cornell.kfs.coa.document.authorization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentAuthorizerBase;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.impl.identity.Person;


public class CuObjectCodeActivationGlobalAuthorizer extends MaintenanceDocumentAuthorizerBase {
    private static final long serialVersionUID = -4822839971298829022L;
    private static final Logger LOG = LogManager.getLogger(CuObjectCodeActivationGlobalAuthorizer.class);
    
    @Override
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
