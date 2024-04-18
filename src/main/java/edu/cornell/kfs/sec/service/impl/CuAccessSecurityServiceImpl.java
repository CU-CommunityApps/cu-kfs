package edu.cornell.kfs.sec.service.impl;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.api.action.WorkflowDocumentActionsService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.sec.SecConstants;
import org.kuali.kfs.sec.service.impl.AccessSecurityServiceImpl;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.sec.CuSecParameterConstants;

public class CuAccessSecurityServiceImpl extends AccessSecurityServiceImpl {

    private WorkflowDocumentActionsService workflowDocumentActionsService;

    /**
     * Overridden so that, if allowed for a particular document type, it will instead check
     * both prior and current requests to see if the person has a routing request on the document.
     */
    @Override
    protected boolean checkForWorkflowRoutingRequests(final AccountingDocument document, final Person person) {
        if (documentAllowsViewingByPriorRecipients(document)) {
            return workflowDocumentActionsService.isUserInRouteLog(
                    document.getDocumentNumber(), person.getPrincipalId(), false);
        } else {
            return super.checkForWorkflowRoutingRequests(document, person);
        }
    }

    private boolean documentAllowsViewingByPriorRecipients(final AccountingDocument document) {
        final Collection<String> docTypesAllowingPriorRecipientViews = parameterService.getParameterValuesAsString(
                SecConstants.ACCESS_SECURITY_NAMESPACE_CODE,
                SecConstants.ALL_PARAMETER_DETAIL_COMPONENT,
                CuSecParameterConstants.DOCUMENT_TYPES_ALLOWING_VIEW_DOCUMENT_ACCESS_FOR_PRIOR_RECIPIENTS);
        final String documentType = document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName();
        return docTypesAllowingPriorRecipientViews.stream()
                .anyMatch(allowedDocumentType -> StringUtils.equals(allowedDocumentType, documentType));
    }

    public void setWorkflowDocumentActionsService(WorkflowDocumentActionsService workflowDocumentActionsService) {
        this.workflowDocumentActionsService = workflowDocumentActionsService;
    }

}
