package edu.cornell.kfs.pdp.document.authorization;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.document.DocumentPresentationController;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.pdp.document.authorization.PayeeACHAccountMaintenanceDocumentAuthorizer;
import org.kuali.kfs.sys.KFSConstants;
import java.util.Set;

public class CuPayeeACHAccountMaintenanceDocumentAuthorizer extends PayeeACHAccountMaintenanceDocumentAuthorizer {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public boolean canApprove(Document document, Person user) {

        try {
            WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

            if (StringUtils.equalsIgnoreCase(user.getPrincipalName(), KFSConstants.SYSTEM_USER)) {
                return true;
            } else if (canBlanketApprove(document, user)) {
                return true;
            } else if (KEWServiceLocator.getDocumentTypeService().isSuperUserForDocumentTypeId(user.getPrincipalId(), workflowDocument.getDocumentTypeId())) {
                return true;
            }

            String documentInitiatorPrincipalId = workflowDocument.getInitiatorPrincipalId();
            if (StringUtils.equalsIgnoreCase(user.getPrincipalId(), documentInitiatorPrincipalId)) {
                return false;
            }
        } catch (Exception ex) {
            LOG.error("canApprove " + ex.getMessage(), ex);
            return false;
        }

        return super.canApprove(document, user);
    }

}
