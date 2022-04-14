package edu.cornell.kfs.pdp.document.authorization;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.api.identity.Person;
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
            if (StringUtils.equalsIgnoreCase(user.getPrincipalId(), KFSConstants.SYSTEM_USER)) {
                return true;
            } else if (canCurrentUserBlanketApprove(document)) {
                return true;
            }

            String documentInitiatorPrincipalId = document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
            if (StringUtils.equalsIgnoreCase(user.getPrincipalId(), documentInitiatorPrincipalId)) {
                return false;
            }
        } catch (Exception ex) {
            LOG.error("canApprove" + ex.getMessage(), ex);
            return false;
        }

        return super.canApprove(document, user);
    }

    private boolean canCurrentUserBlanketApprove(Document document) {
        DocumentPresentationController documentPresentationController = KNSServiceLocator.getDocumentHelperService().getDocumentPresentationController(document);
        Set<String> documentActions = documentPresentationController.getDocumentActions(document);
        return documentActions.contains(KRADConstants.KUALI_ACTION_CAN_BLANKET_APPROVE);
    }

}
