package edu.cornell.kfs.pdp.document.authorization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.pdp.document.authorization.PayeeACHAccountMaintenanceDocumentAuthorizer;

public class CuPayeeACHAccountMaintenanceDocumentAuthorizer extends PayeeACHAccountMaintenanceDocumentAuthorizer {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public boolean canApprove(Document document, Person user) {

        try {
            String documentInitializerPrincipalId = document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
            if (user.getPrincipalId().equals(documentInitializerPrincipalId)) {
                return false;
            }
        } catch (Exception ex) {
            LOG.error("canApprove", ex.getMessage(), ex);
        }

        return super.canApprove(document, user);
    }

}
