package edu.cornell.kfs.sec.service.impl;

import org.kuali.kfs.kew.api.action.WorkflowDocumentActionsService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.sec.service.impl.AccessSecurityServiceImpl;
import org.kuali.kfs.sys.document.AccountingDocument;

public class CuAccessSecurityServiceImpl extends AccessSecurityServiceImpl {

    private WorkflowDocumentActionsService workflowDocumentActionsService;

    /**
     * Overridden to check both prior and current requests to see if the person has a routing request on the document.
     */
    @Override
    protected boolean checkForWorkflowRoutingRequests(final AccountingDocument document, final Person person) {
        return workflowDocumentActionsService.isUserInRouteLog(
                document.getDocumentNumber(), person.getPrincipalId(), false);
    }

    public void setWorkflowDocumentActionsService(WorkflowDocumentActionsService workflowDocumentActionsService) {
        this.workflowDocumentActionsService = workflowDocumentActionsService;
    }

}
